package com.mycoffeestore.service.impl.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycoffeestore.config.AgentProperties;
import com.mycoffeestore.dto.agent.AgentChatRequestDTO;
import com.mycoffeestore.dto.agent.AgentChatRequestV2DTO;
import com.mycoffeestore.dto.agent.AgentChatResponseDTO;
import com.mycoffeestore.dto.agent.MemoryMessage;
import com.mycoffeestore.service.agent.AgentRouterService;
import com.mycoffeestore.service.agent.AgentService;
import com.mycoffeestore.service.agent.AgentServiceV2;
import com.mycoffeestore.service.memory.ConversationMemoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * AI Agent 服务实现 V2
 * <p>
 * 新功能：
 * - 智能路由：自动选择合适的 Agent
 * - 对话记忆：自动加载和保存历史消息
 * - 会话管理：自动生成和管理 sessionId
 * - 流式响应：SSE 实时推送
 *
 * @author Backend Developer
 * @since 2026-03-07
 */
@Slf4j
@org.springframework.stereotype.Service("agentServiceV2")
@RequiredArgsConstructor
public class AgentServiceV2Impl implements AgentServiceV2 {

    private final AgentService agentService;
    private final AgentRouterService agentRouterService;
    private final ConversationMemoryService conversationMemoryService;
    private final AgentProperties agentProperties;
    private final ObjectMapper objectMapper;

    @Override
    public void chatStreamV2(AgentChatRequestV2DTO request, Long userId, SseEmitter emitter) {
        try {
            // 1. 智能路由：确定实际使用的 Agent 类型
            String actualAgentType = determineAgentType(request);

            // 2. 会话管理：生成或使用现有的 sessionId
            String sessionId = resolveSessionId(request);

            // 3. 发送会话信息给前端
            sendSessionInfo(emitter, sessionId, actualAgentType);

            // 4. 加载对话历史（如果存在）
            List<MemoryMessage> history = conversationMemoryService.getHistory(sessionId);

            // 5. 保存用户消息到记忆
            saveUserMessage(sessionId, userId, actualAgentType, request.getMessage());

            // 6. 构建 V1 请求（复用现有逻辑）
            AgentChatRequestDTO v1Request = buildV1Request(request, history, actualAgentType);

            // 7. 执行流式聊天
            CompletableFuture.runAsync(() -> {
                try {
                    agentService.chatStream(v1Request, userId, emitter);
                } catch (Exception e) {
                    log.error("V2 聊天执行失败: {}", e.getMessage(), e);
                    sendError(emitter, "聊天执行失败: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            log.error("V2 聊天初始化失败: {}", e.getMessage(), e);
            sendError(emitter, "聊天初始化失败: " + e.getMessage());
            completeEmitter(emitter);
        }
    }

    /**
     * 确定 Agent 类型（智能路由或使用用户指定的类型）
     */
    private String determineAgentType(AgentChatRequestV2DTO request) {
        // 如果用户明确指定了 Agent 类型（非 auto），直接使用
        if (request.getAgentType() != null && !"auto".equals(request.getAgentType())) {
            log.debug("使用用户指定的 Agent 类型: {}", request.getAgentType());
            return request.getAgentType();
        }

        // 智能路由
        String routedAgent = agentRouterService.route(request.getMessage());
        String reason = agentRouterService.getRouteReason(request.getMessage());
        double confidence = agentRouterService.getConfidence(request.getMessage());

        log.info("智能路由结果: {} (置信度: {}, 理由: {})", routedAgent, confidence, reason);
        return routedAgent;
    }

    /**
     * 解析会话 ID
     * - 如果 forceNewSession=true 或 sessionId 为空，生成新会话
     * - 否则使用现有的 sessionId
     */
    private String resolveSessionId(AgentChatRequestV2DTO request) {
        if (Boolean.TRUE.equals(request.getForceNewSession()) ||
            request.getSessionId() == null ||
            request.getSessionId().trim().isEmpty()) {
            String newSessionId = conversationMemoryService.generateSessionId();
            log.info("生成新会话 ID: {}", newSessionId);
            return newSessionId;
        }
        return request.getSessionId();
    }

    /**
     * 发送会话信息给前端
     */
    private void sendSessionInfo(SseEmitter emitter, String sessionId, String agentType) {
        try {
            AgentChatResponseDTO sessionInfo = AgentChatResponseDTO.sessionInfo(sessionId, agentType);
            String json = objectMapper.writeValueAsString(sessionInfo);
            emitter.send(SseEmitter.event()
                    .name("session")
                    .data(json));
        } catch (Exception e) {
            log.warn("发送会话信息失败: {}", e.getMessage());
        }
    }

    /**
     * 保存用户消息到对话记忆
     */
    private void saveUserMessage(String sessionId, Long userId, String agentType, String message) {
        if (!agentProperties.isEnableMemory()) {
            log.debug("对话记忆功能已禁用，跳过保存");
            return;
        }

        try {
            MemoryMessage memoryMessage = MemoryMessage.builder()
                    .sessionId(sessionId)
                    .userId(userId)
                    .agentType(agentType)
                    .role("user")
                    .content(message)
                    .timestamp(LocalDateTime.now())
                    .build();

            conversationMemoryService.save(memoryMessage);
            log.debug("保存用户消息到记忆: sessionId={}, role={}", sessionId, "user");
        } catch (Exception e) {
            log.warn("保存用户消息失败: {}", e.getMessage());
        }
    }

    /**
     * 构建 V1 请求（复用现有聊天逻辑）
     * 将 V2 的单消息 + 历史记录转换为 V1 的消息列表格式
     */
    private AgentChatRequestDTO buildV1Request(AgentChatRequestV2DTO request,
                                               List<MemoryMessage> history,
                                               String actualAgentType) {
        // 转换历史消息为 V1 格式
        List<AgentChatRequestDTO.Message> messages = history.stream()
                .map(mem -> AgentChatRequestDTO.Message.builder()
                        .role(mem.getRole())
                        .content(mem.getContent())
                        .build())
                .toList();

        // 添加当前用户消息
        AgentChatRequestDTO.Message currentMessage = AgentChatRequestDTO.Message.builder()
                .role("user")
                .content(request.getMessage())
                .build();

        // 合并历史消息和当前消息
        List<AgentChatRequestDTO.Message> allMessages = new java.util.ArrayList<>(messages);
        allMessages.add(currentMessage);

        return AgentChatRequestDTO.builder()
                .agentType(actualAgentType)
                .messages(allMessages)
                .build();
    }

    /**
     * 发送错误消息
     */
    private void sendError(SseEmitter emitter, String errorMessage) {
        try {
            AgentChatResponseDTO errorResponse = AgentChatResponseDTO.error(errorMessage);
            String json = objectMapper.writeValueAsString(errorResponse);
            emitter.send(SseEmitter.event()
                    .name("error")
                    .data(json));
        } catch (Exception e) {
            log.warn("发送错误消息失败: {}", e.getMessage());
        }
    }

    /**
     * 完成 SSE 连接
     */
    private void completeEmitter(SseEmitter emitter) {
        try {
            emitter.complete();
        } catch (Exception e) {
            log.debug("SSE 完成异常: {}", e.getMessage());
        }
    }
}

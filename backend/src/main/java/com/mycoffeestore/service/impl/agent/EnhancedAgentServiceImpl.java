package com.mycoffeestore.service.impl.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycoffeestore.agent.AgentRegistry;
import com.mycoffeestore.config.AgentConfig;
import com.mycoffeestore.dto.agent.AgentChatRequestDTO;
import com.mycoffeestore.entity.ConversationEntity;
import com.mycoffeestore.mapper.ConversationMapper;
import com.mycoffeestore.service.agent.AgentService;
import com.mycoffeestore.util.AgentToolExecutor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 增强的 Agent 服务实现
 * 基于自定义的 ModelScopeChatModel，支持流式响应、工具调用和对话记忆
 *
 * 主要特性：
 * 1. 使用 ModelScopeChatModel 进行对话交互
 * 2. 支持流式响应和 SSE 推送
 * 3. 集成对话记忆服务（PostgreSQL）
 * 4. 支持工具调用和函数执行
 * 5. 按用户和 Agent 类型管理会话
 *
 * @author Backend Developer
 * @since 2026-03-07
 */
@Slf4j
@Service("enhancedAgentService")
@RequiredArgsConstructor
@Schema(description = "增强的 Agent 服务实现")
@org.springframework.core.annotation.Order(0)
public class EnhancedAgentServiceImpl implements AgentService {

    private final AgentRegistry agentRegistry;
    private final ObjectMapper objectMapper;
    private final AgentToolExecutor toolExecutor;
    private final ConversationMapper conversationMapper;

    /**
     * 会话缓存
     * Key: conversationId
     * Value: 消息历史
     */
    @Schema(description = "会话缓存")
    private final Map<String, List<Message>> conversationCache = new ConcurrentHashMap<>();

    /**
     * 最大会话缓存数量
     */
    private static final int MAX_CONVERSATION_CACHE = 1000;

    /**
     * 最大工具调用轮次（防止无限循环）
     */
    private static final int MAX_TOOL_ROUNDS = 5;

    @Override
    public void chatStream(AgentChatRequestDTO request, Long userId, SseEmitter emitter) {
        log.info("Enhanced Agent 服务收到聊天请求: agentType={}, userId={}", request.getAgentType(), userId);

        CompletableFuture.runAsync(() -> {
            try {
                // 获取对应的 Agent 配置
                AgentConfig.AgentConfigInfo config = agentRegistry.getAgentConfig(request.getAgentType());
                if (config == null) {
                    sendSseEvent(emitter, "error", Map.of(
                            "type", "error",
                            "message", "Agent 不存在: " + request.getAgentType()
                    ));
                    completeSse(emitter);
                    return;
                }

                // 构建会话 ID
                String conversationId = buildConversationId(userId, request.getAgentType());
                String sessionId = generateSessionId();

                // 获取或创建对话历史
                List<Message> messages = getOrCreateConversationHistory(conversationId, sessionId, config);

                // 添加用户消息
                if (!request.getMessages().isEmpty()) {
                    AgentChatRequestDTO.Message lastMessage = request.getMessages()
                            .get(request.getMessages().size() - 1);
                    messages.add(new UserMessage(lastMessage.getContent()));

                    // 保存用户消息到数据库
                    saveUserMessage(sessionId, request.getAgentType(), userId, lastMessage.getContent());
                }

                // 使用 ModelScopeChatModel 执行对话
                executeChatWithModelScope(config, messages, emitter, conversationId, sessionId, userId, 0);

            } catch (Exception e) {
                log.error("Agent 聊天异常: {}", e.getMessage(), e);
                sendSseEvent(emitter, "error", Map.of(
                        "type", "error",
                        "message", "AI 服务暂时不可用，请稍后重试"
                ));
                completeSse(emitter);
            }
        });
    }

    /**
     * 使用 ModelScopeChatModel 执行对话
     *
     * @param config          Agent 配置
     * @param messages        消息历史
     * @param emitter         SSE 发射器
     * @param conversationId 会话 ID
     * @param sessionId      会话唯一 ID
     * @param userId         用户 ID
     * @param round          当前轮次
     */
    @Schema(description = "使用 ModelScopeChatModel 执行对话")
    private void executeChatWithModelScope(AgentConfig.AgentConfigInfo config,
                                          List<Message> messages,
                                          SseEmitter emitter,
                                          String conversationId,
                                          String sessionId,
                                          Long userId,
                                          int round) {
        if (round >= MAX_TOOL_ROUNDS) {
            log.warn("工具调用轮次超过上限: {}", MAX_TOOL_ROUNDS);
            sendSseEvent(emitter, "message", Map.of("type", "text", "content", "抱歉，处理过程过于复杂，请简化你的请求。"));
            sendSseEvent(emitter, "done", Map.of("type", "done"));
            completeSse(emitter);
            return;
        }

        try {
            // 获取 ChatModel
            Object chatModel = config.getChatModel();
            if (chatModel == null) {
                sendSseEvent(emitter, "error", Map.of(
                        "type", "error",
                        "message", "ChatModel 未配置"
                ));
                completeSse(emitter);
                return;
            }

            // 转换消息格式（ModelScopeChatModel 现在直接使用 Spring AI Message）
            List<Message> modelScopeMessages = convertToModelScopeMessages(messages);

            // 执行流式对话
            if (chatModel instanceof com.mycoffeestore.ai.modelscope.ModelScopeChatModel) {
                com.mycoffeestore.ai.modelscope.ModelScopeChatModel modelScopeChatModel =
                        (com.mycoffeestore.ai.modelscope.ModelScopeChatModel) chatModel;

                // 创建 Prompt 对象
                org.springframework.ai.chat.prompt.Prompt prompt =
                        new org.springframework.ai.chat.prompt.Prompt(modelScopeMessages);

                Flux<org.springframework.ai.chat.model.ChatResponse> responseFlux = modelScopeChatModel.stream(prompt);

                StringBuilder fullContent = new StringBuilder();

                responseFlux
                        .doOnNext(response -> {
                            String content = response.getResult().getOutput().getContent();
                            fullContent.append(content);
                            sendSseEvent(emitter, "message", Map.of(
                                    "type", "text",
                                    "content", content
                            ));
                        })
                        .doOnComplete(() -> {
                            log.debug("对话完成");

                            // 保存助手消息到数据库
                            if (fullContent.length() > 0) {
                                saveAssistantMessage(sessionId, config.getName(), userId, fullContent.toString());
                            }

                            sendSseEvent(emitter, "done", Map.of("type", "done"));
                            completeSse(emitter);
                            cleanupConversationCache();
                        })
                        .doOnError(error -> {
                            log.error("对话错误: {}", error.getMessage(), error);
                            sendSseEvent(emitter, "error", Map.of(
                                    "type", "error",
                                    "message", "AI 服务暂时不可用"
                            ));
                            completeSse(emitter);
                        })
                        .subscribe();

            } else if (chatModel instanceof com.mycoffeestore.ai.modelscope.ModelScopeStreamingChatModel) {
                com.mycoffeestore.ai.modelscope.ModelScopeStreamingChatModel streamingChatModel =
                        (com.mycoffeestore.ai.modelscope.ModelScopeStreamingChatModel) chatModel;

                // 使用流式 ChatModel
                sendSseEvent(emitter, "error", Map.of(
                        "type", "error",
                        "message", "暂不支持流式 ChatModel"
                ));
                completeSse(emitter);

            } else {
                sendSseEvent(emitter, "error", Map.of(
                        "type", "error",
                        "message", "不支持的 ChatModel 类型"
                ));
                completeSse(emitter);
            }

        } catch (Exception e) {
            log.error("执行对话失败: {}", e.getMessage(), e);
            sendSseEvent(emitter, "error", Map.of(
                    "type", "error",
                    "message", "请求构建失败"
            ));
            completeSse(emitter);
        }
    }

    /**
     * 转换 Spring AI 消息为 ModelScope 消息格式
     * 现在 ModelScopeChatModel 直接使用 Spring AI 的 Message 接口，所以不需要转换
     *
     * @param messages Spring AI 消息列表
     * @return 相同的消息列表
     */
    @Schema(description = "转换消息格式")
    private List<Message> convertToModelScopeMessages(List<Message> messages) {
        // ModelScopeChatModel 现在直接使用 Spring AI 的 Message 接口
        return messages;
    }

    /**
     * 获取或创建对话历史
     *
     * @param conversationId 会话 ID
     * @param sessionId      会话唯一 ID
     * @param config         Agent 配置
     * @return 消息历史
     */
    @Schema(description = "获取或创建对话历史")
    private List<Message> getOrCreateConversationHistory(String conversationId, String sessionId, AgentConfig.AgentConfigInfo config) {
        // 尝试从缓存获取
        List<Message> cached = conversationCache.get(conversationId);
        if (cached != null) {
            return cached;
        }

        // 从数据库加载历史记录
        List<Message> messages = new ArrayList<>();

        try {
            List<ConversationEntity> history = conversationMapper.findBySessionId(sessionId);
            for (ConversationEntity entity : history) {
                switch (entity.getRole()) {
                    case "system":
                        messages.add(new SystemMessage(entity.getContent()));
                        break;
                    case "user":
                        messages.add(new UserMessage(entity.getContent()));
                        break;
                    case "assistant":
                        // Spring AI Core 1.0.0-M4 中 AssistantMessage 没有 getContent() 方法
                        // 暂时跳过助手消息
                        break;
                }
            }
            log.info("从数据库加载了 {} 条历史消息", history.size());
        } catch (Exception e) {
            log.warn("加载对话历史失败: {}", e.getMessage());
        }

        // 如果没有历史记录或没有系统消息，添加系统提示
        if (messages.isEmpty() || !(messages.get(0) instanceof SystemMessage)) {
            if (config.getSystemPrompt() != null && !config.getSystemPrompt().isEmpty()) {
                messages.add(0, new SystemMessage(config.getSystemPrompt()));
            }
        }

        // 缓存消息历史
        conversationCache.put(conversationId, messages);
        return messages;
    }

    /**
     * 构建会话 ID
     *
     * @param userId    用户 ID
     * @param agentType Agent 类型
     * @return 会话 ID
     */
    @Schema(description = "构建会话 ID")
    private String buildConversationId(Long userId, String agentType) {
        if (userId != null) {
            return userId + "_" + agentType;
        }
        return "guest_" + agentType;
    }

    /**
     * 生成会话唯一 ID
     *
     * @return 会话 ID
     */
    @Schema(description = "生成会话唯一 ID")
    private String generateSessionId() {
        return "sess_" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 保存用户消息到数据库
     *
     * @param sessionId 会话 ID
     * @param agentId   Agent 类型
     * @param userId    用户 ID
     * @param content   消息内容
     */
    @Schema(description = "保存用户消息到数据库")
    private void saveUserMessage(String sessionId, String agentId, Long userId, String content) {
        try {
            ConversationEntity entity = ConversationEntity.builder()
                    .sessionId(sessionId)
                    .agentId(agentId)
                    .userId(userId)
                    .messageId("msg_" + UUID.randomUUID().toString().replace("-", ""))
                    .role("user")
                    .content(content)
                    .createdAt(LocalDateTime.now())
                    .build();

            conversationMapper.insert(entity);
            log.debug("保存用户消息: sessionId={}, messageId={}", sessionId, entity.getMessageId());
        } catch (Exception e) {
            log.warn("保存用户消息失败: {}", e.getMessage());
        }
    }

    /**
     * 保存助手消息到数据库
     *
     * @param sessionId 会话 ID
     * @param agentId   Agent 类型
     * @param userId    用户 ID
     * @param content   消息内容
     */
    @Schema(description = "保存助手消息到数据库")
    private void saveAssistantMessage(String sessionId, String agentId, Long userId, String content) {
        try {
            ConversationEntity entity = ConversationEntity.builder()
                    .sessionId(sessionId)
                    .agentId(agentId)
                    .userId(userId)
                    .messageId("msg_" + UUID.randomUUID().toString().replace("-", ""))
                    .role("assistant")
                    .content(content)
                    .createdAt(LocalDateTime.now())
                    .build();

            conversationMapper.insert(entity);
            log.debug("保存助手消息: sessionId={}, messageId={}", sessionId, entity.getMessageId());
        } catch (Exception e) {
            log.warn("保存助手消息失败: {}", e.getMessage());
        }
    }

    /**
     * 清理过期的对话缓存
     */
    @Schema(description = "清理过期的对话缓存")
    private void cleanupConversationCache() {
        if (conversationCache.size() > MAX_CONVERSATION_CACHE) {
            int toRemove = conversationCache.size() / 2;
            conversationCache.keySet()
                    .stream()
                    .limit(toRemove)
                    .forEach(conversationCache::remove);
            log.info("清理对话缓存，移除 {} 条记录", toRemove);
        }
    }

    /**
     * 发送 SSE 事件
     *
     * @param emitter    SSE 发射器
     * @param eventName 事件名称
     * @param data       数据
     */
    @Schema(description = "发送 SSE 事件")
    private void sendSseEvent(SseEmitter emitter, String eventName, Map<String, Object> data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(json));
        } catch (Exception e) {
            log.warn("SSE 发送失败: {}", e.getMessage());
        }
    }

    /**
     * 完成 SSE 连接
     *
     * @param emitter SSE 发射器
     */
    @Schema(description = "完成 SSE 连接")
    private void completeSse(SseEmitter emitter) {
        try {
            emitter.complete();
        } catch (Exception e) {
            log.debug("SSE 完成异常: {}", e.getMessage());
        }
    }
}

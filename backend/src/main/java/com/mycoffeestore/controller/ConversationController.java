package com.mycoffeestore.controller;

import com.mycoffeestore.common.result.Result;
import com.mycoffeestore.dto.agent.MemoryMessage;
import com.mycoffeestore.service.memory.ConversationMemoryService;
import com.mycoffeestore.util.JwtUtil;
import com.mycoffeestore.vo.agent.ConversationListVO;
import com.mycoffeestore.vo.agent.ConversationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 会话管理控制器
 * <p>
 * 提供会话列表查询、会话历史获取、会话删除等功能
 * 支持已登录和未登录用户
 *
 * @author Backend Developer
 * @since 2026-03-07
 */
@Slf4j
@RestController
@RequestMapping("/v1/conversations")
@RequiredArgsConstructor
@Tag(name = "会话管理", description = "对话会话管理接口")
public class ConversationController {

    private final ConversationMemoryService conversationMemoryService;
    private final JwtUtil jwtUtil;

    /**
     * 获取用户会话列表
     * 支持已登录和未登录用户
     * 已登录用户：获取自己的所有会话
     * 未登录用户：返回空列表（暂不支持匿名会话持久化）
     *
     * @param agentType  Agent 类型过滤（可选）
     * @param httpRequest HTTP 请求（用于提取 JWT Token）
     * @return 会话列表
     */
    @GetMapping
    @Operation(summary = "获取会话列表", description = "获取用户的所有会话，支持按 Agent 类型过滤")
    public Result<List<ConversationListVO>> getConversations(
            @RequestParam(required = false) String agentType,
            HttpServletRequest httpRequest) {

        Long userId = extractUserIdOptional(httpRequest);

        if (userId == null) {
            log.info("未登录用户查询会话列表，返回空列表");
            return Result.success(List.of());
        }

        log.info("获取用户会话列表，userId: {}, agentType: {}", userId, agentType);

        try {
            // 获取用户的所有历史消息
            List<MemoryMessage> history = conversationMemoryService.getUserHistory(userId, agentType);

            // 按会话 ID 分组并转换为 VO
            List<ConversationListVO> conversations = history.stream()
                    .collect(Collectors.groupingBy(
                            MemoryMessage::getSessionId,
                            Collectors.collectingAndThen(
                                    Collectors.toList(),
                                    this::buildConversationListVO
                            )))
                    .values()
                    .stream()
                    .sorted((a, b) -> b.getLastActiveAt().compareTo(a.getLastActiveAt()))
                    .toList();

            log.info("获取会话列表成功，userId: {}, 会话数: {}", userId, conversations.size());
            return Result.success(conversations);
        } catch (Exception e) {
            log.error("获取会话列表失败，userId: {}", userId, e);
            return Result.error("获取会话列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定会话的详细历史消息
     * 支持已登录和未登录用户
     *
     * @param sessionId 会话 ID
     * @param httpRequest HTTP 请求（用于提取 JWT Token）
     * @return 会话详情（包含消息历史）
     */
    @GetMapping("/{sessionId}/messages")
    @Operation(summary = "获取会话历史", description = "获取指定会话的详细历史消息")
    public Result<ConversationVO> getConversationMessages(
            @PathVariable String sessionId,
            HttpServletRequest httpRequest) {

        Long userId = extractUserIdOptional(httpRequest);

        log.info("获取会话历史，sessionId: {}, userId: {}", sessionId, userId);

        try {
            // 获取会话历史消息
            List<MemoryMessage> messages = conversationMemoryService.getHistory(sessionId);

            if (messages.isEmpty()) {
                log.warn("会话不存在或无历史消息，sessionId: {}", sessionId);
                return Result.error("会话不存在或无历史消息");
            }

            // 构建会话详情 VO
            ConversationVO conversationVO = buildConversationVO(sessionId, messages);

            log.info("获取会话历史成功，sessionId: {}, 消息数: {}", sessionId, messages.size());
            return Result.success(conversationVO);
        } catch (Exception e) {
            log.error("获取会话历史失败，sessionId: {}", sessionId, e);
            return Result.error("获取会话历史失败: " + e.getMessage());
        }
    }

    /**
     * 删除指定会话
     * 只支持已登录用户删除自己的会话
     *
     * @param sessionId 会话 ID
     * @param httpRequest HTTP 请求（用于提取 JWT Token）
     * @return 删除结果
     */
    @DeleteMapping("/{sessionId}")
    @Operation(summary = "删除会话", description = "删除指定的会话及其所有消息")
    public Result<Void> deleteConversation(
            @PathVariable String sessionId,
            HttpServletRequest httpRequest) {

        Long userId = extractUserIdOptional(httpRequest);

        if (userId == null) {
            log.warn("未登录用户尝试删除会话，拒绝操作");
            return Result.error(401, "请先登录");
        }

        log.info("删除会话，sessionId: {}, userId: {}", sessionId, userId);

        try {
            conversationMemoryService.clearSession(sessionId);
            log.info("删除会话成功，sessionId: {}, userId: {}", sessionId, userId);
            return Result.success("会话已删除", null);
        } catch (Exception e) {
            log.error("删除会话失败，sessionId: {}, userId: {}", sessionId, userId, e);
            return Result.error("删除会话失败: " + e.getMessage());
        }
    }

    /**
     * 可选提取用户ID
     * 尝试从 Authorization header 解析 JWT，失败返回 null
     *
     * @param request HTTP 请求
     * @return 用户ID 或 null
     */
    private Long extractUserIdOptional(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (!jwtUtil.isTokenExpired(token)) {
                    return jwtUtil.getUserId(token);
                }
            }
        } catch (Exception e) {
            log.debug("JWT 解析失败，视为未登录: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 构建会话列表项 VO
     */
    private ConversationListVO buildConversationListVO(List<MemoryMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return null;
        }

        MemoryMessage firstMessage = messages.get(0);
        MemoryMessage lastMessage = messages.get(messages.size() - 1);

        String title = generateTitle(messages);

        return ConversationListVO.builder()
                .sessionId(firstMessage.getSessionId())
                .agentType(firstMessage.getAgentType())
                .agentTypeName(ConversationListVO.AgentType.getDisplayName(firstMessage.getAgentType()))
                .title(title)
                .messageCount(messages.size())
                .lastActiveAt(lastMessage.getTimestamp())
                .createdAt(firstMessage.getTimestamp())
                .lastMessagePreview(truncateMessage(lastMessage.getContent()))
                .build();
    }

    /**
     * 构建会话详情 VO
     */
    private ConversationVO buildConversationVO(String sessionId, List<MemoryMessage> messages) {
        MemoryMessage firstMessage = messages.get(0);
        MemoryMessage lastMessage = messages.get(messages.size() - 1);

        String title = generateTitle(messages);

        List<ConversationVO.MessageItem> messageItems = messages.stream()
                .map(mem -> ConversationVO.MessageItem.builder()
                        .messageId(mem.getSessionId() + "_" + mem.getTimestamp().toString())
                        .role(mem.getRole())
                        .content(mem.getContent())
                        .timestamp(mem.getTimestamp())
                        .build())
                .toList();

        return ConversationVO.builder()
                .sessionId(sessionId)
                .userId(firstMessage.getUserId())
                .agentType(firstMessage.getAgentType())
                .title(title)
                .messageCount(messages.size())
                .lastActiveAt(lastMessage.getTimestamp())
                .createdAt(firstMessage.getTimestamp())
                .messages(messageItems)
                .build();
    }

    /**
     * 生成会话标题
     */
    private String generateTitle(List<MemoryMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return "新对话";
        }

        // 找到第一条用户消息作为标题
        for (MemoryMessage message : messages) {
            if ("user".equals(message.getRole())) {
                String content = message.getContent();
                if (content != null && content.length() > 20) {
                    return content.substring(0, 20) + "...";
                }
                return content != null ? content : "新对话";
            }
        }

        return "新对话";
    }

    /**
     * 截断消息用于预览
     */
    private String truncateMessage(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        if (content.length() > 30) {
            return content.substring(0, 30) + "...";
        }
        return content;
    }
}

package com.mycoffeestore.controller;

import com.mycoffeestore.dto.agent.AgentChatRequestDTO;
import com.mycoffeestore.service.agent.AgentService;
import com.mycoffeestore.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.CompletableFuture;

/**
 * AI Agent 控制器
 * 提供 SSE 流式聊天接口
 *
 * @author zhoulu
 * @since 2026-02-27
 */
@Slf4j
@RestController
@RequestMapping("/v1/agent")
@RequiredArgsConstructor
@Tag(name = "AI Agent", description = "AI Agent 对话接口")
public class AgentController {

    private final AgentService agentService;
    private final JwtUtil jwtUtil;

    /**
     * Agent 流式聊天
     * 支持未登录和已登录用户，JWT 在内部可选解析
     *
     * @param request     聊天请求
     * @param httpRequest HTTP 请求（用于提取 JWT Token）
     * @return SSE 事件流
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Agent 流式聊天", description = "SSE 流式响应，支持多角色 Agent 和工具调用")
    public SseEmitter chat(@RequestBody @Valid AgentChatRequestDTO request,
                           HttpServletRequest httpRequest) {

        log.info("收到 Agent 聊天请求，角色: {}，消息数: {}", request.getAgentType(), request.getMessages().size());

        // 可选提取 userId（未登录为 null）
        Long userId = extractUserIdOptional(httpRequest);

        // 创建 SSE 发射器，60 秒超时
        SseEmitter emitter = new SseEmitter(60000L);

        // 设置超时和错误回调
        emitter.onTimeout(() -> log.warn("SSE 连接超时"));
        emitter.onError(e -> log.warn("SSE 连接错误: {}", e.getMessage()));

        // 异步执行聊天
        CompletableFuture.runAsync(() -> {
            agentService.chatStream(request, userId, emitter);
        });

        return emitter;
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
}

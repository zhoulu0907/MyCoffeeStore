package com.mycoffeestore.service.agent;

import com.mycoffeestore.dto.agent.AgentChatRequestV2DTO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI Agent 服务接口 V2
 * <p>
 * 支持智能路由、对话记忆、会话管理等新功能
 *
 * @author Backend Developer
 * @since 2026-03-07
 */
public interface AgentServiceV2 {

    /**
     * V2 版本流式聊天
     * 支持智能路由、对话记忆、会话管理
     *
     * @param request 聊天请求
     * @param userId  用户 ID（可为 null，表示未登录）
     * @param emitter SSE 发射器
     */
    void chatStreamV2(AgentChatRequestV2DTO request, Long userId, SseEmitter emitter);
}

package com.mycoffeestore.service.agent;

import com.mycoffeestore.dto.agent.AgentChatRequestDTO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI Agent 服务接口
 *
 * @author zhoulu
 * @since 2026-02-27
 */
public interface AgentService {

    /**
     * 流式聊天
     *
     * @param request 聊天请求
     * @param userId  用户ID（可为 null，表示未登录）
     * @param emitter SSE 发射器
     */
    void chatStream(AgentChatRequestDTO request, Long userId, SseEmitter emitter);
}

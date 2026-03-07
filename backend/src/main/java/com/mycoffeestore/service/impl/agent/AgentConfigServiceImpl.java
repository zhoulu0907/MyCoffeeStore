package com.mycoffeestore.service.impl.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycoffeestore.agent.AgentRegistry;
import com.mycoffeestore.config.AgentConfig;
import com.mycoffeestore.dto.agent.AgentChatRequestDTO;
import com.mycoffeestore.service.agent.AgentService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 基于 Agent 配置的服务实现
 * 暂时禁用，使用 AgentDirectService 实现
 *
 * @author Backend Developer
 * @since 2026-03-07
 */
@Slf4j
@Service("agentConfigService")
@RequiredArgsConstructor
@Schema(description = "基于 Agent 配置的服务实现")
@org.springframework.core.annotation.Order(1)
public class AgentConfigServiceImpl implements AgentService {

    private final AgentRegistry agentRegistry;
    private final ObjectMapper objectMapper;

    @Override
    public void chatStream(AgentChatRequestDTO request, Long userId, SseEmitter emitter) {
        log.info("AgentConfigService 收到聊天请求，但此服务暂时禁用");
        log.info("请使用 AgentDirectService (agentDirectService)");

        CompletableFuture.runAsync(() -> {
            sendSseEvent(emitter, "error", Map.of(
                    "type", "error",
                    "message", "此服务暂时禁用，请使用直接 API 调用"
            ));
            completeSse(emitter);
        });
    }

    /**
     * 发送 SSE 事件
     */
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
     */
    private void completeSse(SseEmitter emitter) {
        try {
            emitter.complete();
        } catch (Exception e) {
            log.debug("SSE 完成异常: {}", e.getMessage());
        }
    }
}

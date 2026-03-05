package com.mycoffeestore.ai.modelscope;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycoffeestore.ai.config.ModelScopeChatOptions;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ModelScope 流式 ChatModel 实现
 * 专注于流式调用场景，提供更好的流式处理性能
 *
 * @author Backend Developer
 * @since 2026-03-05
 */
@Slf4j
@Schema(description = "ModelScope 流式 ChatModel 实现")
@RequiredArgsConstructor
public class ModelScopeStreamingChatModel {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private ModelScopeChatOptions defaultOptions;

    /**
     * 设置默认聊天选项
     */
    public void setDefaultOptions(ModelScopeChatOptions options) {
        this.defaultOptions = options;
    }

    /**
     * 流式调用
     *
     * @param messages 消息列表
     * @return 响应内容流
     */
    @Schema(description = "流式调用")
    public Flux<ChatResponse> stream(List<Message> messages) {
        return stream(messages, null);
    }

    /**
     * 流式调用（带选项）
     *
     * @param messages 消息列表
     * @param options  聊天选项
     * @return 响应内容流
     */
    @Schema(description = "流式调用（带选项）")
    public Flux<ChatResponse> stream(List<Message> messages, ModelScopeChatOptions options) {
        ModelScopeChatOptions effectiveOptions = mergeOptions(options);
        effectiveOptions.setStream(true);

        return streamInternal(messages, effectiveOptions);
    }

    /**
     * 内部流式调用实现
     */
    private Flux<ChatResponse> streamInternal(List<Message> messages, ModelScopeChatOptions options) {
        try {
            // 构建请求体
            Map<String, Object> requestBody = buildRequestBody(messages, options);

            log.debug("发起 ModelScope 流式调用，消息数: {}", messages.size());

            return webClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(objectMapper.writeValueAsString(requestBody))
                    .retrieve()
                    .bodyToFlux(String.class)
                    .flatMap(this::parseStreamChunk)
                    .doOnComplete(() -> log.debug("ModelScope 流式调用完成"))
                    .doOnError(error -> log.error("ModelScope 流式调用异常", error));

        } catch (Exception e) {
            log.error("ModelScope 流式调用构建异常", e);
            return Flux.error(new RuntimeException("流式调用构建失败: " + e.getMessage(), e));
        }
    }

    /**
     * 构建请求体
     */
    private Map<String, Object> buildRequestBody(List<Message> messages, ModelScopeChatOptions options) {
        Map<String, Object> body = new HashMap<>();

        // 添加模型名称
        body.put("model", options.getModel());

        // 添加消息列表
        List<Map<String, String>> messageMaps = new ArrayList<>();
        for (Message msg : messages) {
            Map<String, String> msgMap = new HashMap<>();
            msgMap.put("role", msg.getRole());
            msgMap.put("content", msg.getContent());
            messageMaps.add(msgMap);
        }
        body.put("messages", messageMaps);

        // 添加选项参数
        body.put("stream", true);
        body.put("temperature", options.getTemperature());
        body.put("max_tokens", options.getMaxTokens());

        if (options.getTopP() != null) {
            body.put("top_p", options.getTopP());
        }
        if (options.getTopK() != null) {
            body.put("top_k", options.getTopK());
        }

        // 额外参数
        body.putAll(options.getExtraParams());

        return body;
    }

    /**
     * 解析流式响应块
     */
    private Flux<ChatResponse> parseStreamChunk(String chunk) {
        // 处理 SSE 格式
        String data = chunk.trim();
        if (data.isEmpty() || data.equals("[DONE]")) {
            return Flux.empty();
        }

        // 去掉 "data: " 前缀
        if (data.startsWith("data:")) {
            data = data.substring(5).trim();
        }
        if (data.isEmpty() || data.equals("[DONE]")) {
            return Flux.empty();
        }

        try {
            JsonNode root = objectMapper.readTree(data);
            JsonNode choices = root.get("choices");

            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode choice = choices.get(0);
                JsonNode delta = choice.get("delta");

                if (delta != null) {
                    ChatResponse response = new ChatResponse();

                    // 解析内容
                    if (delta.has("content") && !delta.get("content").isNull()) {
                        response.setContent(delta.get("content").asText());
                    }

                    // 解析角色
                    if (delta.has("role") && !delta.get("role").isNull()) {
                        response.setRole(delta.get("role").asText());
                    }

                    // 解析完成原因
                    JsonNode finishReason = choice.get("finish_reason");
                    if (finishReason != null && !finishReason.isNull()) {
                        response.setFinishReason(finishReason.asText());
                    }

                    // 解析索引
                    JsonNode index = choice.get("index");
                    if (index != null && !index.isNull()) {
                        response.setIndex(index.asInt());
                    }

                    return Flux.just(response);
                }
            }

            return Flux.empty();
        } catch (Exception e) {
            log.debug("解析流式块失败: {} - {}", data, e.getMessage());
            return Flux.empty();
        }
    }

    /**
     * 合并选项
     */
    private ModelScopeChatOptions mergeOptions(ModelScopeChatOptions options) {
        if (options == null) {
            return defaultOptions != null ? defaultOptions : ModelScopeChatOptions.defaultOptions();
        }

        return ModelScopeChatOptions.builder()
                .model(options.getModel() != null ? options.getModel() : defaultOptions.getModel())
                .temperature(options.getTemperature() != null ? options.getTemperature() : defaultOptions.getTemperature())
                .maxTokens(options.getMaxTokens() != null ? options.getMaxTokens() : defaultOptions.getMaxTokens())
                .topP(options.getTopP() != null ? options.getTopP() : defaultOptions.getTopP())
                .topK(options.getTopK() != null ? options.getTopK() : defaultOptions.getTopK())
                .stop(options.getStop() != null ? options.getStop() : defaultOptions.getStop())
                .frequencyPenalty(options.getFrequencyPenalty())
                .presencePenalty(options.getPresencePenalty())
                .stream(true)
                .extraParams(options.getExtraParams())
                .build();
    }

    /**
     * 聊天响应
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @Schema(description = "聊天响应")
    public static class ChatResponse {
        @Schema(description = "内容")
        private String content;

        @Schema(description = "角色")
        private String role;

        @Schema(description = "完成原因")
        private String finishReason;

        @Schema(description = "索引")
        private Integer index;
    }

    /**
     * 消息类
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @Schema(description = "聊天消息")
    public static class Message {
        @Schema(description = "角色", example = "user")
        private String role;

        @Schema(description = "内容", example = "你好")
        private String content;
    }
}

package com.mycoffeestore.ai.modelscope;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycoffeestore.ai.config.ModelScopeChatOptions;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ModelScope ChatModel 实现
 * 实现同步调用模式，支持流式和阻塞式调用
 *
 * @author Backend Developer
 * @since 2026-03-05
 */
@Slf4j
@Schema(description = "ModelScope ChatModel 实现")
@RequiredArgsConstructor
public class ModelScopeChatModel {

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
     * 同步调用（阻塞模式）
     *
     * @param messages 消息列表
     * @return 响应内容
     */
    @Schema(description = "同步调用")
    public String call(List<Message> messages) {
        return call(messages, null);
    }

    /**
     * 同步调用（带选项）
     *
     * @param messages 消息列表
     * @param options  聊天选项
     * @return 响应内容
     */
    @Schema(description = "同步调用（带选项）")
    public String call(List<Message> messages, ModelScopeChatOptions options) {
        ModelScopeChatOptions effectiveOptions = mergeOptions(options);
        return callInternal(messages, effectiveOptions);
    }

    /**
     * 流式调用
     *
     * @param messages 消息列表
     * @return 响应内容流
     */
    @Schema(description = "流式调用")
    public Flux<String> stream(List<Message> messages) {
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
    public Flux<String> stream(List<Message> messages, ModelScopeChatOptions options) {
        ModelScopeChatOptions effectiveOptions = mergeOptions(options);
        effectiveOptions.setStream(true);
        return streamInternal(messages, effectiveOptions);
    }

    /**
     * 内部同步调用实现
     */
    private String callInternal(List<Message> messages, ModelScopeChatOptions options) {
        try {
            // 构建请求体
            Map<String, Object> requestBody = buildRequestBody(messages, options);

            // 使用 WebClient 进行同步调用
            String response = webClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(objectMapper.writeValueAsString(requestBody))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofMillis(60000));

            // 解析响应
            return parseResponse(response);
        } catch (WebClientResponseException e) {
            log.error("ModelScope API 调用失败: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("ModelScope API 调用失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("ModelScope 调用异常", e);
            throw new RuntimeException("ModelScope 调用异常: " + e.getMessage(), e);
        }
    }

    /**
     * 内部流式调用实现
     */
    private Flux<String> streamInternal(List<Message> messages, ModelScopeChatOptions options) {
        try {
            // 构建请求体
            Map<String, Object> requestBody = buildRequestBody(messages, options);

            return webClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(objectMapper.writeValueAsString(requestBody))
                    .retrieve()
                    .bodyToFlux(String.class)
                    .flatMap(this::parseStreamLine)
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
        Map<String, Object> params = options.toMap();
        body.putAll(params);

        // 移除 model（已添加）和 extraParams（不需要发送）
        body.remove("extraParams");

        return body;
    }

    /**
     * 解析同步响应
     */
    private String parseResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode choices = root.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode message = choices.get(0).get("message");
                if (message != null) {
                    JsonNode content = message.get("content");
                    if (content != null) {
                        return content.asText();
                    }
                }
            }
            return "";
        } catch (Exception e) {
            log.error("解析响应失败", e);
            throw new RuntimeException("解析响应失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析流式响应行
     */
    private Mono<String> parseStreamLine(String line) {
        // 处理 SSE 格式
        String data = line.trim();
        if (data.isEmpty() || data.equals("[DONE]")) {
            return Mono.empty();
        }

        // 去掉 "data: " 前缀
        if (data.startsWith("data:")) {
            data = data.substring(5).trim();
        }
        if (data.isEmpty() || data.equals("[DONE]")) {
            return Mono.empty();
        }

        try {
            JsonNode root = objectMapper.readTree(data);
            JsonNode choices = root.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode delta = choices.get(0).get("delta");
                if (delta != null && delta.has("content")) {
                    String content = delta.get("content").asText();
                    if (!content.isEmpty()) {
                        return Mono.just(content);
                    }
                }
            }
            return Mono.empty();
        } catch (Exception e) {
            log.debug("解析流式行失败: {} - {}", data, e.getMessage());
            return Mono.empty();
        }
    }

    /**
     * 合并选项
     */
    private ModelScopeChatOptions mergeOptions(ModelScopeChatOptions options) {
        if (options == null) {
            return defaultOptions != null ? defaultOptions : ModelScopeChatOptions.defaultOptions();
        }

        // 合并默认选项和用户选项
        String model = options.getModel() != null ? options.getModel() : defaultOptions.getModel();
        Double temperature = options.getTemperature() != null ? options.getTemperature() : defaultOptions.getTemperature();
        Integer maxTokens = options.getMaxTokens() != null ? options.getMaxTokens() : defaultOptions.getMaxTokens();
        Double topP = options.getTopP() != null ? options.getTopP() : defaultOptions.getTopP();
        Integer topK = options.getTopK() != null ? options.getTopK() : defaultOptions.getTopK();
        String[] stop = options.getStop() != null ? options.getStop() : defaultOptions.getStop();
        Boolean stream = options.getStream() != null ? options.getStream() : defaultOptions.getStream();

        // 合并额外参数
        Map<String, Object> mergedExtraParams = new HashMap<>(defaultOptions.getExtraParams());
        mergedExtraParams.putAll(options.getExtraParams());

        return ModelScopeChatOptions.builder()
                .model(model)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .topP(topP)
                .topK(topK)
                .stop(stop)
                .frequencyPenalty(options.getFrequencyPenalty())
                .presencePenalty(options.getPresencePenalty())
                .stream(stream)
                .extraParams(mergedExtraParams)
                .build();
    }

    /**
     * 消息类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "聊天消息")
    public static class Message {
        @Schema(description = "角色", example = "user")
        private String role;

        @Schema(description = "内容", example = "你好")
        private String content;
    }
}

package com.mycoffeestore.ai.modelscope;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycoffeestore.ai.config.ModelScopeChatOptions;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
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
 * 实现 Spring AI ChatModel 接口
 *
 * @author Backend Developer
 * @since 2026-03-05
 */
@Slf4j
@Schema(description = "ModelScope ChatModel 实现")
public class ModelScopeChatModel implements ChatModel {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private ModelScopeChatOptions defaultOptions;

    public ModelScopeChatModel(WebClient webClient, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    /**
     * 设置默认聊天选项
     */
    public void setDefaultOptions(ModelScopeChatOptions options) {
        this.defaultOptions = options;
    }

    @Override
    public ChatOptions getDefaultOptions() {
        return (ChatOptions) defaultOptions;
    }

    // ==================== ChatModel 接口实现 ====================

    @Override
    public ChatResponse call(Prompt prompt) {
        List<Message> messages = prompt.getInstructions();
        String content = callInternal(messages, null);
        AssistantMessage assistantMessage = new AssistantMessage(content);
        List<Generation> generations = List.of(new Generation(assistantMessage));
        return new ChatResponse(generations);
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        List<Message> messages = prompt.getInstructions();
        return streamInternal(messages, null)
                .map(content -> {
                    AssistantMessage assistantMessage = new AssistantMessage(content);
                    List<Generation> generations = List.of(new Generation(assistantMessage));
                    return new ChatResponse(generations);
                });
    }

    // ==================== 内部实现方法 ====================

    /**
     * 内部同步调用实现
     */
    private String callInternal(List<Message> messages, ModelScopeChatOptions options) {
        try {
            ModelScopeChatOptions effectiveOptions = options != null ? options :
                (defaultOptions != null ? defaultOptions : ModelScopeChatOptions.defaultOptions());

            // 构建请求体
            Map<String, Object> requestBody = buildRequestBody(messages, effectiveOptions);

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
            ModelScopeChatOptions effectiveOptions = options != null ? options :
                (defaultOptions != null ? defaultOptions : ModelScopeChatOptions.defaultOptions());

            // 对于流式调用，强制设置 stream 为 true
            ModelScopeChatOptions streamOptions = ModelScopeChatOptions.builder()
                    .model(effectiveOptions.getModel())
                    .temperature(effectiveOptions.getTemperature())
                    .maxTokens(effectiveOptions.getMaxTokens())
                    .topP(effectiveOptions.getTopP())
                    .topK(effectiveOptions.getTopK())
                    .stop(effectiveOptions.getStop())
                    .frequencyPenalty(effectiveOptions.getFrequencyPenalty())
                    .presencePenalty(effectiveOptions.getPresencePenalty())
                    .stream(true)
                    .extraParams(effectiveOptions.getExtraParams())
                    .build();

            // 构建请求体
            Map<String, Object> requestBody = buildRequestBody(messages, streamOptions);

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
            msgMap.put("role", msg.getMessageType().getValue());
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
     * 转换 ChatOptions 到 ModelScopeChatOptions
     */
    private ModelScopeChatOptions convertOptions(ChatOptions options) {
        if (options == null) {
            return defaultOptions != null ? defaultOptions : ModelScopeChatOptions.defaultOptions();
        }

        if (options instanceof ModelScopeChatOptions) {
            return (ModelScopeChatOptions) options;
        }

        // 从通用 ChatOptions 转换
        ModelScopeChatOptions.Builder builder = ModelScopeChatOptions.builder();

        if (options.getTemperature() != null) {
            builder.temperature(options.getTemperature().doubleValue());
        } else if (defaultOptions != null) {
            builder.temperature(defaultOptions.getTemperature());
        }

        if (options.getMaxTokens() != null) {
            builder.maxTokens(options.getMaxTokens());
        } else if (defaultOptions != null) {
            builder.maxTokens(defaultOptions.getMaxTokens());
        }

        if (defaultOptions != null) {
            builder.model(defaultOptions.getModel());
            builder.topP(defaultOptions.getTopP());
            builder.topK(defaultOptions.getTopK());
            builder.stop(defaultOptions.getStop());
            builder.frequencyPenalty(defaultOptions.getFrequencyPenalty());
            builder.presencePenalty(defaultOptions.getPresencePenalty());
            builder.extraParams(defaultOptions.getExtraParams());
        }

        return builder.build();
    }
}

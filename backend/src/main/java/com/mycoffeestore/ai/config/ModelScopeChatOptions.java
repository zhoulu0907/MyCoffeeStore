package com.mycoffeestore.ai.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * ModelScope 聊天选项
 * 定义 ModelScope API 调用的可配置参数
 *
 * @author Backend Developer
 * @since 2026-03-05
 */
@Data
@Schema(description = "ModelScope 聊天选项")
public class ModelScopeChatOptions {

    /**
     * 模型名称
     */
    @Schema(description = "模型名称", example = "kimih/K2.5-Instruct")
    private String model;

    /**
     * 温度参数（0-1，越高越随机）
     */
    @Schema(description = "温度参数", example = "0.7")
    private Double temperature = 0.7;

    /**
     * 最大 Token 数
     */
    @Schema(description = "最大 Token 数", example = "4096")
    private Integer maxTokens = 4096;

    /**
     * Top-P 采样参数
     */
    @Schema(description = "Top-P 采样参数", example = "0.9")
    private Double topP;

    /**
     * Top-K 采样参数
     */
    @Schema(description = "Top-K 采样参数", example = "50")
    private Integer topK;

    /**
     * 停止序列
     */
    @Schema(description = "停止序列")
    private String[] stop;

    /**
     * 频率惩罚（-2.0 到 2.0）
     */
    @Schema(description = "频率惩罚", example = "0.0")
    private Double frequencyPenalty = 0.0;

    /**
     * 存在惩罚（-2.0 到 2.0）
     */
    @Schema(description = "存在惩罚", example = "0.0")
    private Double presencePenalty = 0.0;

    /**
     * 是否启用流式输出
     */
    @Schema(description = "是否启用流式输出", example = "true")
    private Boolean stream = true;

    /**
     * 额外的请求参数
     */
    @Schema(description = "额外的请求参数")
    private Map<String, Object> extraParams = new HashMap<>();

    /**
     * 默认构造函数
     */
    public ModelScopeChatOptions() {
        this.temperature = 0.7;
        this.maxTokens = 4096;
        this.frequencyPenalty = 0.0;
        this.presencePenalty = 0.0;
        this.stream = true;
        this.extraParams = new HashMap<>();
    }

    /**
     * 创建 Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 创建默认选项
     */
    public static ModelScopeChatOptions defaultOptions() {
        return new ModelScopeChatOptions();
    }

    /**
     * 转换为 API 请求参数 Map
     */
    public Map<String, Object> toMap() {
        Map<String, Object> params = new HashMap<>();
        params.put("model", model);
        params.put("temperature", temperature);
        params.put("max_tokens", maxTokens);
        params.put("stream", stream);

        if (topP != null) {
            params.put("top_p", topP);
        }
        if (topK != null) {
            params.put("top_k", topK);
        }
        if (stop != null && stop.length > 0) {
            params.put("stop", stop);
        }
        params.put("frequency_penalty", frequencyPenalty);
        params.put("presence_penalty", presencePenalty);

        // 合并额外参数
        params.putAll(extraParams);

        return params;
    }

    /**
     * Builder 类
     */
    public static class Builder {
        private String model;
        private Double temperature = 0.7;
        private Integer maxTokens = 4096;
        private Double topP;
        private Integer topK;
        private String[] stop;
        private Double frequencyPenalty = 0.0;
        private Double presencePenalty = 0.0;
        private Boolean stream = true;
        private Map<String, Object> extraParams = new HashMap<>();

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder temperature(Double temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder maxTokens(Integer maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        public Builder topP(Double topP) {
            this.topP = topP;
            return this;
        }

        public Builder topK(Integer topK) {
            this.topK = topK;
            return this;
        }

        public Builder stop(String[] stop) {
            this.stop = stop;
            return this;
        }

        public Builder frequencyPenalty(Double frequencyPenalty) {
            this.frequencyPenalty = frequencyPenalty;
            return this;
        }

        public Builder presencePenalty(Double presencePenalty) {
            this.presencePenalty = presencePenalty;
            return this;
        }

        public Builder stream(Boolean stream) {
            this.stream = stream;
            return this;
        }

        public Builder extraParams(Map<String, Object> extraParams) {
            this.extraParams = extraParams;
            return this;
        }

        public ModelScopeChatOptions build() {
            ModelScopeChatOptions options = new ModelScopeChatOptions();
            options.setModel(model);
            options.setTemperature(temperature);
            options.setMaxTokens(maxTokens);
            options.setTopP(topP);
            options.setTopK(topK);
            options.setStop(stop);
            options.setFrequencyPenalty(frequencyPenalty);
            options.setPresencePenalty(presencePenalty);
            options.setStream(stream);
            options.setExtraParams(extraParams != null ? extraParams : new HashMap<>());
            return options;
        }
    }
}

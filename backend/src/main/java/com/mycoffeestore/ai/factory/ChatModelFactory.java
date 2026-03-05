package com.mycoffeestore.ai.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycoffeestore.ai.config.LlmProperties;
import com.mycoffeestore.ai.core.LlmProvider;
import com.mycoffeestore.ai.modelscope.ModelScopeChatModel;
import com.mycoffeestore.ai.modelscope.ModelScopeStreamingChatModel;
import io.swagger.v3.oas.annotations.media.Schema;
import io.netty.channel.ChannelOption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ChatModel 工厂类
 * 根据 LLM 提供商创建对应的 ChatModel 实例，支持动态切换
 *
 * @author Backend Developer
 * @since 2026-03-05
 */
@Slf4j
@RequiredArgsConstructor
@Schema(description = "ChatModel 工厂类")
public class ChatModelFactory {

    private final LlmProperties llmProperties;
    private final ObjectMapper objectMapper;

    /**
     * 缓存已创建的 WebClient 实例
     */
    private final Map<String, WebClient> webClientCache = new ConcurrentHashMap<>();

    /**
     * 缓存已创建的 ChatModel 实例
     */
    private final Map<String, ModelScopeChatModel> chatModelCache = new ConcurrentHashMap<>();

    /**
     * 缓存已创建的 StreamingChatModel 实例
     */
    private final Map<String, ModelScopeStreamingChatModel> streamingChatModelCache = new ConcurrentHashMap<>();

    /**
     * 创建 ChatModel（使用默认提供商）
     *
     * @return ChatModel 实例
     */
    @Schema(description = "创建 ChatModel（使用默认提供商）")
    public ModelScopeChatModel createChatModel() {
        return createChatModel(llmProperties.getProvider());
    }

    /**
     * 创建 ChatModel（指定提供商）
     *
     * @param provider LLM 提供商
     * @return ChatModel 实例
     */
    @Schema(description = "创建 ChatModel（指定提供商）")
    public ModelScopeChatModel createChatModel(LlmProvider provider) {
        return createChatModel(provider, null);
    }

    /**
     * 创建 ChatModel（指定提供商和模型名称）
     *
     * @param provider LLM 提供商
     * @param model    模型名称
     * @return ChatModel 实例
     */
    @Schema(description = "创建 ChatModel（指定提供商和模型名称）")
    public ModelScopeChatModel createChatModel(LlmProvider provider, String model) {
        String cacheKey = buildCacheKey(provider, model);

        // 从缓存获取
        ModelScopeChatModel cachedModel = chatModelCache.get(cacheKey);
        if (cachedModel != null) {
            log.debug("从缓存获取 ChatModel: {}", cacheKey);
            return cachedModel;
        }

        // 创建新实例
        log.info("创建 ChatModel: provider={}, model={}", provider, model);
        ModelScopeChatModel chatModel = doCreateChatModel(provider, model);
        chatModelCache.put(cacheKey, chatModel);

        return chatModel;
    }

    /**
     * 创建流式 ChatModel（使用默认提供商）
     *
     * @return StreamingChatModel 实例
     */
    @Schema(description = "创建流式 ChatModel（使用默认提供商）")
    public ModelScopeStreamingChatModel createStreamingChatModel() {
        return createStreamingChatModel(llmProperties.getProvider());
    }

    /**
     * 创建流式 ChatModel（指定提供商）
     *
     * @param provider LLM 提供商
     * @return StreamingChatModel 实例
     */
    @Schema(description = "创建流式 ChatModel（指定提供商）")
    public ModelScopeStreamingChatModel createStreamingChatModel(LlmProvider provider) {
        return createStreamingChatModel(provider, null);
    }

    /**
     * 创建流式 ChatModel（指定提供商和模型名称）
     *
     * @param provider LLM 提供商
     * @param model    模型名称
     * @return StreamingChatModel 实例
     */
    @Schema(description = "创建流式 ChatModel（指定提供商和模型名称）")
    public ModelScopeStreamingChatModel createStreamingChatModel(LlmProvider provider, String model) {
        String cacheKey = buildCacheKey(provider, model);

        // 从缓存获取
        ModelScopeStreamingChatModel cachedModel = streamingChatModelCache.get(cacheKey);
        if (cachedModel != null) {
            log.debug("从缓存获取 StreamingChatModel: {}", cacheKey);
            return cachedModel;
        }

        // 创建新实例
        log.info("创建 StreamingChatModel: provider={}, model={}", provider, model);
        ModelScopeStreamingChatModel streamingModel = doCreateStreamingChatModel(provider, model);
        streamingChatModelCache.put(cacheKey, streamingModel);

        return streamingModel;
    }

    /**
     * 切换默认提供商
     *
     * @param provider 新的提供商
     */
    @Schema(description = "切换默认提供商")
    public void switchProvider(LlmProvider provider) {
        log.info("切换默认提供商: {} -> {}", llmProperties.getProvider(), provider);
        llmProperties.setProvider(provider);
    }

    /**
     * 清除缓存
     */
    @Schema(description = "清除缓存")
    public void clearCache() {
        log.info("清除 ChatModel 缓存");
        chatModelCache.clear();
        streamingChatModelCache.clear();
        webClientCache.clear();
    }

    /**
     * 获取缓存统计信息
     */
    @Schema(description = "获取缓存统计信息")
    public Map<String, Integer> getCacheStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("chatModelCount", chatModelCache.size());
        stats.put("streamingModelCount", streamingChatModelCache.size());
        stats.put("webClientCount", webClientCache.size());
        return stats;
    }

    /**
     * 实际创建 ChatModel
     */
    private ModelScopeChatModel doCreateChatModel(LlmProvider provider, String model) {
        switch (provider) {
            case MODELSCOPE:
                WebClient webClient = getOrCreateWebClient(provider);
                ModelScopeChatModel chatModel = new ModelScopeChatModel(webClient, objectMapper);

                // 配置默认选项
                String modelName = model != null ? model : llmProperties.getModelscope().getModel();
                chatModel.setDefaultOptions(
                        com.mycoffeestore.ai.config.ModelScopeChatOptions.builder()
                                .model(modelName)
                                .temperature(llmProperties.getModelscope().getTemperature())
                                .maxTokens(llmProperties.getCommon().getMaxTokens())
                                .stream(llmProperties.getCommon().getStream())
                                .build()
                );

                return chatModel;

            case DASHSCOPE:
                throw new UnsupportedOperationException("DashScope 尚未实现");

            case OPENAI:
                throw new UnsupportedOperationException("OpenAI 尚未实现");

            default:
                throw new IllegalArgumentException("不支持的提供商: " + provider);
        }
    }

    /**
     * 实际创建 StreamingChatModel
     */
    private ModelScopeStreamingChatModel doCreateStreamingChatModel(LlmProvider provider, String model) {
        switch (provider) {
            case MODELSCOPE:
                WebClient webClient = getOrCreateWebClient(provider);
                ModelScopeStreamingChatModel streamingModel = new ModelScopeStreamingChatModel(webClient, objectMapper);

                // 配置默认选项
                String modelName = model != null ? model : llmProperties.getModelscope().getModel();
                streamingModel.setDefaultOptions(
                        com.mycoffeestore.ai.config.ModelScopeChatOptions.builder()
                                .model(modelName)
                                .temperature(llmProperties.getModelscope().getTemperature())
                                .maxTokens(llmProperties.getCommon().getMaxTokens())
                                .stream(true)
                                .build()
                );

                return streamingModel;

            case DASHSCOPE:
                throw new UnsupportedOperationException("DashScope 尚未实现");

            case OPENAI:
                throw new UnsupportedOperationException("OpenAI 尚未实现");

            default:
                throw new IllegalArgumentException("不支持的提供商: " + provider);
        }
    }

    /**
     * 获取或创建 WebClient
     */
    private WebClient getOrCreateWebClient(LlmProvider provider) {
        String cacheKey = provider.getCode();

        return webClientCache.computeIfAbsent(cacheKey, key -> {
            log.debug("创建 WebClient: {}", key);

            switch (provider) {
                case MODELSCOPE:
                    return createModelScopeWebClient();

                case DASHSCOPE:
                    return createDashScopeWebClient();

                case OPENAI:
                    return createOpenAIWebClient();

                default:
                    throw new IllegalArgumentException("不支持的提供商: " + provider);
            }
        });
    }

    /**
     * 创建 ModelScope WebClient
     */
    private WebClient createModelScopeWebClient() {
        LlmProperties.ModelScopeConfig config = llmProperties.getModelscope();

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getTimeout().intValue())
                .responseTimeout(Duration.ofMillis(config.getTimeout()));

        return WebClient.builder()
                .baseUrl(config.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + config.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();
    }

    /**
     * 创建 DashScope WebClient
     */
    private WebClient createDashScopeWebClient() {
        LlmProperties.DashScopeConfig config = llmProperties.getDashscope();

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getTimeout().intValue())
                .responseTimeout(Duration.ofMillis(config.getTimeout()));

        return WebClient.builder()
                .baseUrl("https://dashscope.aliyuncs.com/api/v1")
                .defaultHeader("Authorization", "Bearer " + config.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    /**
     * 创建 OpenAI WebClient
     */
    private WebClient createOpenAIWebClient() {
        LlmProperties.OpenAIConfig config = llmProperties.getOpenai();

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getTimeout().intValue())
                .responseTimeout(Duration.ofMillis(config.getTimeout()));

        return WebClient.builder()
                .baseUrl(config.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + config.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    /**
     * 构建缓存键
     */
    private String buildCacheKey(LlmProvider provider, String model) {
        return provider.getCode() + ":" + (model != null ? model : "default");
    }

    /**
     * 测试模型连接
     *
     * @param provider LLM 提供商
     * @return 是否连接成功
     */
    @Schema(description = "测试模型连接")
    public boolean testModelConnection(String provider) {
        try {
            LlmProvider llmProvider = LlmProvider.fromCode(provider);
            WebClient webClient = getOrCreateWebClient(llmProvider);

            // 发送一个简单的测试请求
            switch (llmProvider) {
                case MODELSCOPE:
                    return testModelScopeConnection(webClient);
                case DASHSCOPE:
                    return testDashScopeConnection(webClient);
                case OPENAI:
                    return testOpenAIConnection(webClient);
                default:
                    return false;
            }
        } catch (Exception e) {
            log.error("测试模型连接失败: provider={}", provider, e);
            return false;
        }
    }

    /**
     * 测试 ModelScope 连接
     */
    private boolean testModelScopeConnection(WebClient webClient) {
        try {
            // 发送一个简单的请求来测试连接
            webClient.get()
                    .uri("/")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(5));
            return true;
        } catch (Exception e) {
            log.debug("ModelScope 连接测试失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 测试 DashScope 连接
     */
    private boolean testDashScopeConnection(WebClient webClient) {
        try {
            webClient.get()
                    .uri("/")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(5));
            return true;
        } catch (Exception e) {
            log.debug("DashScope 连接测试失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 测试 OpenAI 连接
     */
    private boolean testOpenAIConnection(WebClient webClient) {
        try {
            webClient.get()
                    .uri("/")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(5));
            return true;
        } catch (Exception e) {
            log.debug("OpenAI 连接测试失败: {}", e.getMessage());
            return false;
        }
    }
}

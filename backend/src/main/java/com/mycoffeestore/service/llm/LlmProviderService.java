package com.mycoffeestore.service.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycoffeestore.dto.llm.ConnectionTestRequestDTO;
import com.mycoffeestore.entity.LlmModelConfig;
import com.mycoffeestore.entity.LlmProvider;
import com.mycoffeestore.entity.LlmUserConfig;
import com.mycoffeestore.mapper.LlmModelConfigMapper;
import com.mycoffeestore.mapper.LlmProviderMapper;
import com.mycoffeestore.mapper.LlmUserConfigMapper;
import com.mycoffeestore.security.EncryptionService;
import com.mycoffeestore.vo.llm.ConnectionTestResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mycoffeestore.entity.table.LlmUserConfigTableDef.LLM_USER_CONFIG;

/**
 * LLM 提供商服务
 * 负责提供商相关的业务逻辑，包括连接测试
 *
 * @author Backend Developer
 * @since 2026-03-05
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmProviderService {

    private final LlmProviderMapper providerMapper;
    private final LlmModelConfigMapper modelConfigMapper;
    private final LlmUserConfigMapper userConfigMapper;
    private final EncryptionService encryptionService;
    private final ObjectMapper objectMapper;

    /**
     * 测试连接
     *
     * @param userId  用户ID
     * @param request 测试请求
     * @return 测试结果
     */
    public ConnectionTestResultVO testConnection(Long userId, ConnectionTestRequestDTO request) {
        // 获取提供商信息
        LlmProvider provider = providerMapper.selectOneById(request.getProviderId());
        if (provider == null) {
            return ConnectionTestResultVO.failure(null, "提供商不存在");
        }

        // 获取 API Key
        String apiKey;
        if (request.getApiKey() != null && !request.getApiKey().isEmpty()) {
            // 使用请求中的临时 API Key
            apiKey = request.getApiKey();
        } else {
            // 使用已保存的配置
            if (userId == null) {
                return ConnectionTestResultVO.failure(provider.getProviderName(), "请先登录或提供 API Key");
            }

            LlmUserConfig config = userConfigMapper.selectOneByCondition(
                    LLM_USER_CONFIG.USER_ID.eq(userId)
                            .and(LLM_USER_CONFIG.PROVIDER_ID.eq(request.getProviderId()))
            );

            if (config == null) {
                return ConnectionTestResultVO.failure(provider.getProviderName(), "未找到该提供商的配置，请先配置 API Key");
            }

            apiKey = encryptionService.decrypt(config.getApiKeyEncrypted(), config.getApiKeyIv());
        }

        // 获取模型信息
        String modelId = null;
        String modelName = null;
        if (request.getModelId() != null) {
            LlmModelConfig model = modelConfigMapper.selectOneById(request.getModelId());
            if (model != null && model.getProviderId().equals(request.getProviderId())) {
                modelId = model.getModelId();
                modelName = model.getModelName();
            }
        }

        // 如果没有指定模型，使用提供商的默认模型
        if (modelId == null) {
            List<LlmModelConfig> models = modelConfigMapper.selectListByCondition(
                    com.mycoffeestore.entity.table.LlmModelConfigTableDef.LLM_MODEL_CONFIG
                            .PROVIDER_ID.eq(request.getProviderId())
                            .and(com.mycoffeestore.entity.table.LlmModelConfigTableDef.LLM_MODEL_CONFIG.STATUS.eq(1))
            );
            if (!models.isEmpty()) {
                modelId = models.get(0).getModelId();
                modelName = models.get(0).getModelName();
            }
        }

        if (modelId == null) {
            return ConnectionTestResultVO.failure(provider.getProviderName(), "该提供商没有可用的模型");
        }

        // 执行连接测试
        return doTestConnection(provider, modelId, modelName, apiKey);
    }

    /**
     * 执行连接测试
     */
    private ConnectionTestResultVO doTestConnection(LlmProvider provider, String modelId, String modelName, String apiKey) {
        long startTime = System.currentTimeMillis();

        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelId);
            requestBody.put("messages", List.of(
                    Map.of("role", "user", "content", "Hi")
            ));
            requestBody.put("max_tokens", 10);

            // 构建 WebClient
            WebClient webClient = WebClient.builder()
                    .baseUrl(provider.getApiEndpoint())
                    .defaultHeader("Authorization", "Bearer " + apiKey)
                    .build();

            // 发送请求
            String response = webClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(objectMapper.writeValueAsString(requestBody))
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(new RuntimeException("API 调用失败: " + body)))
                    )
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            long responseTime = System.currentTimeMillis() - startTime;

            // 解析响应
            JsonNode jsonNode = objectMapper.readTree(response);
            String content = jsonNode.path("choices")
                    .path(0)
                    .path("message")
                    .path("content")
                    .asText("");

            log.info("连接测试成功，提供商: {}，模型: {}，响应时间: {}ms",
                    provider.getProviderName(), modelName, responseTime);

            return ConnectionTestResultVO.success(
                    provider.getProviderName(),
                    modelName,
                    responseTime,
                    content
            );

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("连接测试失败，提供商: {}，错误: {}", provider.getProviderName(), e.getMessage(), e);

            return ConnectionTestResultVO.failure(
                    provider.getProviderName(),
                    "连接失败: " + e.getMessage()
            );
        }
    }
}

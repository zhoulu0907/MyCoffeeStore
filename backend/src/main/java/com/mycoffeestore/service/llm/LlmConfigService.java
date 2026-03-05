package com.mycoffeestore.service.llm;

import com.mycoffeestore.dto.llm.ConnectionTestRequestDTO;
import com.mycoffeestore.dto.llm.LlmConfigUpdateRequestDTO;
import com.mycoffeestore.entity.LlmModelConfig;
import com.mycoffeestore.entity.LlmProvider;
import com.mycoffeestore.entity.LlmUserConfig;
import com.mycoffeestore.entity.table.LlmModelConfigTableDef;
import com.mycoffeestore.entity.table.LlmProviderTableDef;
import com.mycoffeestore.entity.table.LlmUserConfigTableDef;
import com.mycoffeestore.mapper.LlmModelConfigMapper;
import com.mycoffeestore.mapper.LlmProviderMapper;
import com.mycoffeestore.mapper.LlmUserConfigMapper;
import com.mycoffeestore.security.EncryptionService;
import com.mycoffeestore.vo.llm.ConnectionTestResultVO;
import com.mycoffeestore.vo.llm.LlmModelVO;
import com.mycoffeestore.vo.llm.LlmProviderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.mycoffeestore.entity.table.LlmModelConfigTableDef.LLM_MODEL_CONFIG;
import static com.mycoffeestore.entity.table.LlmProviderTableDef.LLM_PROVIDER;
import static com.mycoffeestore.entity.table.LlmUserConfigTableDef.LLM_USER_CONFIG;

/**
 * LLM 配置服务
 * 负责管理 LLM 提供商、模型配置和用户密钥配置
 *
 * @author Backend Developer
 * @since 2026-03-05
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmConfigService {

    private final LlmProviderMapper providerMapper;
    private final LlmModelConfigMapper modelConfigMapper;
    private final LlmUserConfigMapper userConfigMapper;
    private final LlmProviderService providerService;
    private final EncryptionService encryptionService;

    /**
     * 获取所有启用的提供商
     *
     * @return 提供商列表
     */
    public List<LlmProviderVO> getActiveProviders() {
        return getProviders(null);
    }

    /**
     * 获取提供商列表（可按状态筛选）
     *
     * @param status 状态筛选（null 表示全部）
     * @return 提供商列表
     */
    public List<LlmProviderVO> getProviders(Integer status) {
        List<LlmProvider> providers;

        if (status == null) {
            providers = providerMapper.selectAll();
        } else {
            providers = providerMapper.selectListByCondition(
                    LLM_PROVIDER.STATUS.eq(status)
            );
        }

        return providers.stream()
                .map(this::toProviderVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取提供商详情
     *
     * @param providerId 提供商ID
     * @return 提供商信息
     */
    public LlmProviderVO getProvider(Long providerId) {
        LlmProvider provider = providerMapper.selectOneById(providerId);
        if (provider == null) {
            throw new IllegalArgumentException("提供商不存在");
        }
        return toProviderVO(provider);
    }

    /**
     * 获取提供商的模型列表
     *
     * @param providerId 提供商ID
     * @return 模型列表
     */
    public List<LlmModelVO> getModelsByProvider(Long providerId) {
        List<LlmModelConfig> models = modelConfigMapper.selectListByCondition(
                LLM_MODEL_CONFIG.PROVIDER_ID.eq(providerId)
                        .and(LLM_MODEL_CONFIG.STATUS.eq(1))
        );

        LlmProvider provider = providerMapper.selectOneById(providerId);

        return models.stream()
                .map(model -> toModelVO(model, provider))
                .collect(Collectors.toList());
    }

    /**
     * 获取所有启用的模型
     *
     * @return 模型列表
     */
    public List<LlmModelVO> getActiveModels() {
        List<LlmModelConfig> models = modelConfigMapper.selectListByCondition(
                LLM_MODEL_CONFIG.STATUS.eq(1)
        );

        return models.stream()
                .map(model -> {
                    LlmProvider provider = providerMapper.selectOneById(model.getProviderId());
                    return toModelVO(model, provider);
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取模型详情
     *
     * @param modelId 模型ID
     * @return 模型信息
     */
    public LlmModelVO getModel(Long modelId) {
        LlmModelConfig model = modelConfigMapper.selectOneById(modelId);
        if (model == null) {
            throw new IllegalArgumentException("模型不存在");
        }

        LlmProvider provider = providerMapper.selectOneById(model.getProviderId());
        return toModelVO(model, provider);
    }

    /**
     * 更新用户配置（加密存储 API Key）
     *
     * @param userId  用户ID
     * @param request 配置更新请求
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateUserConfig(Long userId, LlmConfigUpdateRequestDTO request) {
        // 验证提供商存在
        LlmProvider provider = providerMapper.selectOneById(request.getProviderId());
        if (provider == null) {
            throw new IllegalArgumentException("提供商不存在");
        }

        // 加密 API Key
        var encryptionResult = encryptionService.encrypt(request.getApiKey());

        // 查找现有配置
        LlmUserConfig existingConfig = userConfigMapper.selectOneByCondition(
                LLM_USER_CONFIG.USER_ID.eq(userId)
                        .and(LLM_USER_CONFIG.PROVIDER_ID.eq(request.getProviderId()))
        );

        if (existingConfig != null) {
            // 更新现有配置
            existingConfig.setApiKeyEncrypted(encryptionResult.encryptedData());
            existingConfig.setApiKeyIv(encryptionResult.iv());
            existingConfig.setConfigJson(request.getConfigJson());
            userConfigMapper.update(existingConfig);
            log.info("更新用户配置成功，用户ID: {}，提供商: {}", userId, provider.getProviderCode());
        } else {
            // 创建新配置
            LlmUserConfig newConfig = LlmUserConfig.builder()
                    .userId(userId)
                    .providerId(request.getProviderId())
                    .apiKeyEncrypted(encryptionResult.encryptedData())
                    .apiKeyIv(encryptionResult.iv())
                    .configJson(request.getConfigJson())
                    .status(1)
                    .build();
            userConfigMapper.insert(newConfig);
            log.info("创建用户配置成功，用户ID: {}，提供商: {}", userId, provider.getProviderCode());
        }
    }

    /**
     * 获取用户的 API Key（解密）
     *
     * @param userId     用户ID
     * @param providerId 提供商ID
     * @return 解密后的 API Key
     */
    public String getUserApiKey(Long userId, Long providerId) {
        LlmUserConfig config = userConfigMapper.selectOneByCondition(
                LLM_USER_CONFIG.USER_ID.eq(userId)
                        .and(LLM_USER_CONFIG.PROVIDER_ID.eq(providerId))
                        .and(LLM_USER_CONFIG.STATUS.eq(1))
        );

        if (config == null) {
            throw new IllegalArgumentException("未找到该提供商的配置");
        }

        return encryptionService.decrypt(config.getApiKeyEncrypted(), config.getApiKeyIv());
    }

    /**
     * 删除用户配置
     *
     * @param userId     用户ID
     * @param providerId 提供商ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteUserConfig(Long userId, Long providerId) {
        userConfigMapper.deleteByCondition(
                LLM_USER_CONFIG.USER_ID.eq(userId)
                        .and(LLM_USER_CONFIG.PROVIDER_ID.eq(providerId))
        );
        log.info("删除用户配置成功，用户ID: {}，提供商ID: {}", userId, providerId);
    }

    /**
     * 测试连接
     *
     * @param userId  用户ID
     * @param request 测试请求
     * @return 测试结果
     */
    public ConnectionTestResultVO testConnection(Long userId, ConnectionTestRequestDTO request) {
        return providerService.testConnection(userId, request);
    }

    /**
     * 转换为 Provider VO
     */
    private LlmProviderVO toProviderVO(LlmProvider provider) {
        return LlmProviderVO.builder()
                .id(provider.getId())
                .providerCode(provider.getProviderCode())
                .providerName(provider.getProviderName())
                .providerType(provider.getProviderType())
                .apiEndpoint(provider.getApiEndpoint())
                .description(provider.getDescription())
                .status(provider.getStatus())
                .sortOrder(provider.getSortOrder())
                .hasConfigured(false) // 需要传入 userId 才能查询
                .build();
    }

    /**
     * 转换为 Model VO
     */
    private LlmModelVO toModelVO(LlmModelConfig model, LlmProvider provider) {
        return LlmModelVO.builder()
                .id(model.getId())
                .providerId(model.getProviderId())
                .providerCode(provider != null ? provider.getProviderCode() : null)
                .providerName(provider != null ? provider.getProviderName() : null)
                .modelCode(model.getModelCode())
                .modelName(model.getModelName())
                .modelId(model.getModelId())
                .modelType(model.getModelType())
                .contextWindow(model.getContextWindow())
                .maxOutputTokens(model.getMaxOutputTokens())
                .supportStreaming(model.getSupportStreaming() == 1)
                .supportFunctionCalling(model.getSupportFunctionCalling() == 1)
                .pricingInput(model.getPricingInput())
                .pricingOutput(model.getPricingOutput())
                .status(model.getStatus())
                .sortOrder(model.getSortOrder())
                .build();
    }
}

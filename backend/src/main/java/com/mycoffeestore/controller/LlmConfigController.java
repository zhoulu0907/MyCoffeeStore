package com.mycoffeestore.controller;

import com.mycoffeestore.common.result.Result;
import com.mycoffeestore.dto.llm.ConnectionTestRequestDTO;
import com.mycoffeestore.dto.llm.LlmConfigUpdateRequestDTO;
import com.mycoffeestore.service.llm.LlmConfigService;
import com.mycoffeestore.util.JwtUtil;
import com.mycoffeestore.vo.llm.ConnectionTestResultVO;
import com.mycoffeestore.vo.llm.LlmModelVO;
import com.mycoffeestore.vo.llm.LlmProviderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * LLM 配置控制器
 * 提供 LLM 提供商、模型配置和用户密钥管理的 API
 *
 * @author Backend Developer
 * @since 2026-03-05
 */
@Slf4j
@RestController
@RequestMapping("/v1/llm")
@RequiredArgsConstructor
@Tag(name = "LLM 配置管理", description = "LLM 提供商和模型配置管理接口")
public class LlmConfigController {

    private final LlmConfigService llmConfigService;
    private final JwtUtil jwtUtil;

    /**
     * 获取提供商列表
     *
     * @param status 状态筛选（可选）
     * @return 提供商列表
     */
    @GetMapping("/providers")
    @Operation(summary = "获取提供商列表", description = "获取所有 LLM 提供商信息，可按状态筛选")
    public Result<List<LlmProviderVO>> getProviders(
            @Parameter(description = "状态：0-禁用，1-启用（不传则返回全部）")
            @RequestParam(required = false) Integer status) {
        List<LlmProviderVO> providers = llmConfigService.getProviders(status);
        return Result.success(providers);
    }

    /**
     * 获取提供商详情
     *
     * @param providerId 提供商ID
     * @return 提供商详情
     */
    @GetMapping("/providers/{providerId}")
    @Operation(summary = "获取提供商详情", description = "根据 ID 获取提供商详细信息")
    public Result<LlmProviderVO> getProvider(
            @Parameter(description = "提供商ID", required = true)
            @PathVariable Long providerId) {
        LlmProviderVO provider = llmConfigService.getProvider(providerId);
        return Result.success(provider);
    }

    /**
     * 获取模型列表
     *
     * @param providerId 提供商ID（可选，不传则返回所有模型）
     * @return 模型列表
     */
    @GetMapping("/models")
    @Operation(summary = "获取模型列表", description = "获取 LLM 模型信息，可按提供商筛选")
    public Result<List<LlmModelVO>> getModels(
            @Parameter(description = "提供商ID（不传则返回所有启用的模型）")
            @RequestParam(required = false) Long providerId) {
        List<LlmModelVO> models;
        if (providerId != null) {
            models = llmConfigService.getModelsByProvider(providerId);
        } else {
            models = llmConfigService.getActiveModels();
        }
        return Result.success(models);
    }

    /**
     * 获取模型详情
     *
     * @param modelId 模型ID
     * @return 模型详情
     */
    @GetMapping("/models/{modelId}")
    @Operation(summary = "获取模型详情", description = "根据 ID 获取模型详细信息")
    public Result<LlmModelVO> getModel(
            @Parameter(description = "模型ID", required = true)
            @PathVariable Long modelId) {
        LlmModelVO model = llmConfigService.getModel(modelId);
        return Result.success(model);
    }

    /**
     * 更新用户配置（保存 API Key）
     *
     * @param request 配置更新请求
     * @param httpRequest HTTP 请求
     * @return 操作结果
     */
    @PostMapping("/config")
    @Operation(summary = "更新用户配置", description = "保存或更新用户的 API Key 配置（加密存储）")
    public Result<Void> updateConfig(
            @RequestBody @Valid LlmConfigUpdateRequestDTO request,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        llmConfigService.updateUserConfig(userId, request);
        return Result.success("配置更新成功", null);
    }

    /**
     * 测试连接
     *
     * @param request 测试请求
     * @param httpRequest HTTP 请求
     * @return 测试结果
     */
    @PostMapping("/test")
    @Operation(summary = "测试连接", description = "测试 LLM 提供商连接是否正常")
    public Result<ConnectionTestResultVO> testConnection(
            @RequestBody @Valid ConnectionTestRequestDTO request,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        ConnectionTestResultVO result = llmConfigService.testConnection(userId, request);
        return Result.success(result);
    }

    /**
     * 从请求中提取用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (!jwtUtil.isTokenExpired(token)) {
                    return jwtUtil.getUserId(token);
                }
            }
        } catch (Exception e) {
            log.debug("JWT 解析失败: {}", e.getMessage());
        }
        throw new IllegalArgumentException("请先登录");
    }
}

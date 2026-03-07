package com.mycoffeestore.agent;

import com.mycoffeestore.config.AgentConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Agent 注册中心
 * 管理 Agent 配置信息，提供 Agent 查询功能
 * 兼容 Spring AI Alibaba 1.0.0-M2 版本
 *
 * @author Backend Developer
 * @since 2026-03-07
 */
@Slf4j
@Component
@Schema(description = "Agent 注册中心")
public class AgentRegistry {

    /**
     * Agent 配置注册表
     * Key: Agent 名称
     * Value: Agent 配置信息
     */
    @Schema(description = "Agent 配置注册表")
    private final Map<String, AgentConfig.AgentConfigInfo> agentConfigs = new ConcurrentHashMap<>();

    /**
     * Agent 类型映射
     * Key: Agent 类型别名
     * Value: Agent 名称
     */
    @Schema(description = "Agent 类型映射")
    private final Map<String, String> agentAliases = new ConcurrentHashMap<>();

    /**
     * 注册 Agent 配置
     *
     * @param name   Agent 名称
     * @param config Agent 配置信息
     */
    @Schema(description = "注册 Agent 配置")
    public void register(String name, AgentConfig.AgentConfigInfo config) {
        log.info("注册 Agent 配置: name={}, description={}", name, config.getDescription());
        agentConfigs.put(name, config);

        // 添加常用别名
        addAliases(name);
    }

    /**
     * 注册 Agent 配置（直接使用配置中的名称）
     *
     * @param config Agent 配置信息
     */
    @Schema(description = "注册 Agent 配置（直接使用配置中的名称）")
    public void register(AgentConfig.AgentConfigInfo config) {
        register(config.getName(), config);
    }

    /**
     * 获取 Agent 配置
     *
     * @param name Agent 名称
     * @return Agent 配置信息，如果不存在返回 null
     */
    @Schema(description = "获取 Agent 配置")
    public AgentConfig.AgentConfigInfo getAgentConfig(String name) {
        String resolvedName = agentAliases.getOrDefault(name, name);
        AgentConfig.AgentConfigInfo config = agentConfigs.get(resolvedName);
        if (config == null) {
            log.warn("Agent 配置不存在: {}", name);
        }
        return config;
    }

    /**
     * 获取 Agent 配置（带默认值）
     *
     * @param name            Agent 名称
     * @param defaultConfig 默认配置
     * @return Agent 配置信息
     */
    @Schema(description = "获取 Agent 配置（带默认值）")
    public AgentConfig.AgentConfigInfo getAgentConfigOrDefault(String name, AgentConfig.AgentConfigInfo defaultConfig) {
        AgentConfig.AgentConfigInfo config = getAgentConfig(name);
        return config != null ? config : defaultConfig;
    }

    /**
     * 检查 Agent 配置是否存在
     *
     * @param name Agent 名称
     * @return 是否存在
     */
    @Schema(description = "检查 Agent 配置是否存在")
    public boolean contains(String name) {
        String resolvedName = agentAliases.getOrDefault(name, name);
        return agentConfigs.containsKey(resolvedName);
    }

    /**
     * 获取所有 Agent 名称
     *
     * @return Agent 名称集合
     */
    @Schema(description = "获取所有 Agent 名称")
    public Set<String> getAllAgentNames() {
        return new HashSet<>(agentConfigs.keySet());
    }

    /**
     * 获取所有 Agent 配置
     *
     * @return Agent 配置映射
     */
    @Schema(description = "获取所有 Agent 配置")
    public Map<String, AgentConfig.AgentConfigInfo> getAllAgentConfigs() {
        return new HashMap<>(agentConfigs);
    }

    /**
     * 获取 Agent 数量
     *
     * @return Agent 数量
     */
    @Schema(description = "获取 Agent 数量")
    public int getAgentCount() {
        return agentConfigs.size();
    }

    /**
     * 注销 Agent 配置
     *
     * @param name Agent 名称
     * @return 被注销的 Agent 配置，如果不存在返回 null
     */
    @Schema(description = "注销 Agent 配置")
    public AgentConfig.AgentConfigInfo unregister(String name) {
        log.info("注销 Agent 配置: {}", name);
        String resolvedName = agentAliases.getOrDefault(name, name);
        removeAliases(name);
        return agentConfigs.remove(resolvedName);
    }

    /**
     * 清空所有 Agent 配置
     */
    @Schema(description = "清空所有 Agent 配置")
    public void clear() {
        log.info("清空所有 Agent 配置");
        agentConfigs.clear();
        agentAliases.clear();
    }

    /**
     * 根据 Agent 类别获取推荐 Agent 配置
     *
     * @param category Agent 类别（如 "coffee", "order", "service"）
     * @return 推荐的 Agent 配置，如果没有匹配返回 null
     */
    @Schema(description = "根据 Agent 类别获取推荐 Agent 配置")
    public AgentConfig.AgentConfigInfo getRecommendedAgentConfig(String category) {
        String agentName = switch (category.toLowerCase()) {
            case "coffee", "advisor", "recommend" -> "coffee_advisor";
            case "order", "cart", "purchase" -> "order_assistant";
            case "service", "support", "help" -> "customer_service";
            default -> "general_chat";
        };
        return getAgentConfig(agentName);
    }

    /**
     * 获取推荐的 Agent 名称
     *
     * @param category Agent 类别
     * @return Agent 名称
     */
    @Schema(description = "获取推荐的 Agent 名称")
    public String getRecommendedAgentName(String category) {
        AgentConfig.AgentConfigInfo config = getRecommendedAgentConfig(category);
        return config != null ? config.getName() : "general_chat";
    }

    /**
     * 添加 Agent 别名
     *
     * @param name Agent 名称
     */
    @Schema(description = "添加 Agent 别名")
    private void addAliases(String name) {
        switch (name) {
            case "coffee_advisor" -> {
                agentAliases.put("coffee", name);
                agentAliases.put("advisor", name);
                agentAliases.put("recommend", name);
            }
            case "order_assistant" -> {
                agentAliases.put("order", name);
                agentAliases.put("cart", name);
                agentAliases.put("purchase", name);
            }
            case "customer_service" -> {
                agentAliases.put("service", name);
                agentAliases.put("support", name);
                agentAliases.put("help", name);
            }
            case "general_chat" -> {
                agentAliases.put("general", name);
                agentAliases.put("chat", name);
                agentAliases.put("default", name);
            }
        }
    }

    /**
     * 移除 Agent 别名
     *
     * @param name Agent 名称
     */
    @Schema(description = "移除 Agent 别名")
    private void removeAliases(String name) {
        agentAliases.entrySet().removeIf(entry -> entry.getValue().equals(name));
    }

    /**
     * 获取注册中心统计信息
     *
     * @return 统计信息
     */
    @Schema(description = "获取注册中心统计信息")
    public RegistryStats getStats() {
        return RegistryStats.builder()
                .totalAgents(agentConfigs.size())
                .agentNames(new ArrayList<>(agentConfigs.keySet()))
                .aliasesCount(agentAliases.size())
                .build();
    }

    /**
     * 注册中心统计信息
     */
    @Schema(description = "注册中心统计信息")
    @lombok.Builder
    @lombok.Data
    public static class RegistryStats {
        /**
         * Agent 总数
         */
        @Schema(description = "Agent 总数")
        private int totalAgents;

        /**
         * Agent 名称列表
         */
        @Schema(description = "Agent 名称列表")
        private List<String> agentNames;

        /**
         * 别名数量
         */
        @Schema(description = "别名数量")
        private int aliasesCount;
    }
}

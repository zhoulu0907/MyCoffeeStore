package com.mycoffeestore.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Agent 配置属性
 * 控制 Agent 的启用和配置
 *
 * @author Backend Developer
 * @since 2026-03-07
 */
@Data
@Component
@Schema(description = "Agent 配置属性")
@ConfigurationProperties(prefix = "agent")
public class AgentProperties {

    /**
     * 是否启用 Agent
     */
    @Schema(description = "是否启用 Agent")
    private boolean enabled = true;

    /**
     * 使用的实现类型
     * - direct: 直接 API 调用（原有实现）
     * - react: ReactAgent 实现（新实现）
     */
    @Schema(description = "Agent 实现类型")
    private ImplementationType implementationType = ImplementationType.REACT;

    /**
     * 最大对话历史轮次
     */
    @Schema(description = "最大对话历史轮次")
    private int maxChatHistory = 10;

    /**
     * 会话超时时间（秒）
     */
    @Schema(description = "会话超时时间（秒）")
    private long sessionTimeout = 1800;

    /**
     * 是否启用对话记忆
     */
    @Schema(description = "是否启用对话记忆")
    private boolean enableMemory = true;

    /**
     * 是否启用流式响应
     */
    @Schema(description = "是否启用流式响应")
    private boolean enableStreaming = true;

    /**
     * 最大工具调用轮次
     */
    @Schema(description = "最大工具调用轮次")
    private int maxToolRounds = 5;

    /**
     * Agent 专用配置
     */
    @Schema(description = "Agent 专用配置")
    private AgentConfig agents = new AgentConfig();

    /**
     * 实现类型枚举
     */
    @Schema(description = "实现类型枚举")
    public enum ImplementationType {
        /**
         * 直接 API 调用
         */
        DIRECT,
        /**
         * ReactAgent 实现
         */
        REACT
    }

    /**
     * Agent 专用配置
     */
    @Data
    @Schema(description = "Agent 专用配置")
    public static class AgentConfig {
        /**
         * 咖啡顾问 Agent 配置
         */
        @Schema(description = "咖啡顾问 Agent 配置")
        private CoffeeAdvisorConfig coffeeAdvisor = new CoffeeAdvisorConfig();

        /**
         * 订单助手 Agent 配置
         */
        @Schema(description = "订单助手 Agent 配置")
        private OrderAssistantConfig orderAssistant = new OrderAssistantConfig();

        /**
         * 客服 Agent 配置
         */
        @Schema(description = "客服 Agent 配置")
        private CustomerServiceConfig customerService = new CustomerServiceConfig();
    }

    /**
     * 咖啡顾问 Agent 配置
     */
    @Data
    @Schema(description = "咖啡顾问 Agent 配置")
    public static class CoffeeAdvisorConfig {
        /**
         * 温度参数
         */
        @Schema(description = "温度参数")
        private double temperature = 0.7;

        /**
         * 最大推荐数量
         */
        @Schema(description = "最大推荐数量")
        private int maxRecommendations = 3;
    }

    /**
     * 订单助手 Agent 配置
     */
    @Data
    @Schema(description = "订单助手 Agent 配置")
    public static class OrderAssistantConfig {
        /**
         * 温度参数
         */
        @Schema(description = "温度参数")
        private double temperature = 0.6;

        /**
         * 是否需要登录确认
         */
        @Schema(description = "是否需要登录确认")
        private boolean requireLogin = true;
    }

    /**
     * 客服 Agent 配置
     */
    @Data
    @Schema(description = "客服 Agent 配置")
    public static class CustomerServiceConfig {
        /**
         * 温度参数
         */
        @Schema(description = "温度参数")
        private double temperature = 0.5;

        /**
         * 是否显示门店信息
         */
        @Schema(description = "是否显示门店信息")
        private boolean showStoreInfo = true;
    }
}

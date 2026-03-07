package com.mycoffeestore.agent.routing;

import com.mycoffeestore.ai.factory.ChatModelFactory;
import com.mycoffeestore.agent.AgentRegistry;
import com.mycoffeestore.agent.tools.AgentToolRegistrar;
import com.mycoffeestore.config.AgentConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能路由 Agent 配置类
 * 使用 Spring AI Alibaba 的 LlmRoutingAgent 实现智能意图识别和路由
 *
 * 功能：
 * 1. 意图识别：根据用户输入识别意图类型
 * 2. 智能路由：根据意图类型将请求路由到合适的子 Agent
 * 3. 顺序执行：支持多个 Agent 按顺序执行（如：推荐 → 下单 → 营销）
 * 4. 并行执行：支持多个 Agent 并行执行（如：同时查询多个信息源）
 *
 * @author Backend Developer
 * @since 2026-03-07
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@Schema(description = "智能路由 Agent 配置类")
@ConditionalOnProperty(prefix = "agent.routing", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AgentRoutingConfig {

    private final ChatModelFactory chatModelFactory;
    private final AgentToolRegistrar toolRegistrar;
    private final AgentRegistry agentRegistry;

    /**
     * 意图类型枚举
     * 定义所有支持的意图类型及其描述
     */
    @Schema(description = "意图类型枚举")
    public enum IntentType {
        /**
         * 购买订单意图 - 用户想要购买咖啡
         */
        ORDER_PURCHASE("order_purchase", "购买咖啡订单", ExecutionStrategy.SEQUENTIAL),

        /**
         * 投诉处理意图 - 用户有投诉或问题
         */
        COMPLAINT("complaint", "投诉处理", ExecutionStrategy.SEQUENTIAL),

        /**
         * 咨询意图 - 用户咨询咖啡相关问题
         */
        CONSULT("consult", "咖啡咨询", ExecutionStrategy.SINGLE),

        /**
         * 订单查询意图 - 用户查询订单状态
         */
        ORDER_QUERY("order_query", "订单查询", ExecutionStrategy.SINGLE),

        /**
         * 一般对话意图 - 通用聊天
         */
        GENERAL("general", "一般对话", ExecutionStrategy.SINGLE);

        private final String code;
        private final String description;
        private final ExecutionStrategy strategy;

        IntentType(String code, String description, ExecutionStrategy strategy) {
            this.code = code;
            this.description = description;
            this.strategy = strategy;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public ExecutionStrategy getStrategy() {
            return strategy;
        }
    }

    /**
     * 执行策略枚举
     * 定义 Agent 的执行策略
     */
    @Schema(description = "执行策略枚举")
    public enum ExecutionStrategy {
        /**
         * 单 Agent 执行 - 只执行一个 Agent
         */
        SINGLE,

        /**
         * 顺序执行 - 多个 Agent 按顺序依次执行
         */
        SEQUENTIAL,

        /**
         * 并行执行 - 多个 Agent 并行同时执行
         */
        PARALLEL
    }

    /**
     * 路由规则配置
     * 定义每个意图类型对应的 Agent 执行链
     */
    @Schema(description = "路由规则配置")
    @lombok.Builder
    @lombok.Data
    public static class RouteRule {
        /**
         * 意图类型
         */
        @Schema(description = "意图类型")
        private IntentType intentType;

        /**
         * 执行策略
         */
        @Schema(description = "执行策略")
        private ExecutionStrategy strategy;

        /**
         * Agent 执行链（按执行顺序排列）
         */
        @Schema(description = "Agent 执行链")
        private List<String> agentChain;

        /**
         * 优先级（数字越小优先级越高）
         */
        @Schema(description = "优先级")
        private Integer priority;

        /**
         * 是否需要在执行后收集所有 Agent 的结果
         */
        @Schema(description = "是否聚合结果")
        private Boolean aggregateResults;

        /**
         * 上下文传递配置
         */
        @Schema(description = "上下文传递配置")
        private ContextPassingConfig contextPassing;
    }

    /**
     * 上下文传递配置
     * 定义如何在 Agent 之间传递上下文
     */
    @Schema(description = "上下文传递配置")
    @lombok.Builder
    @lombok.Data
    public static class ContextPassingConfig {
        /**
         * 是否传递用户消息历史
         */
        @Schema(description = "是否传递消息历史")
        private Boolean passMessageHistory;

        /**
         * 是否传递工具执行结果
         */
        @Schema(description = "是否传递工具结果")
        private Boolean passToolResults;

        /**
         * 是否传递 Agent 推理过程
         */
        @Schema(description = "是否传递推理过程")
        private Boolean passReasoning;

        /**
         * 最大传递历史轮数
         */
        @Schema(description = "最大传递历史轮数")
        private Integer maxHistoryRounds;
    }

    /**
     * 创建默认路由规则配置
     * 定义系统内置的路由规则
     *
     * @return 路由规则映射
     */
    @Bean(name = "defaultRouteRules")
    @Schema(description = "默认路由规则配置")
    public Map<IntentType, RouteRule> defaultRouteRules() {
        log.info("初始化默认路由规则配置");

        Map<IntentType, RouteRule> rules = new EnumMap<>(IntentType.class);

        // 规则 1: 购买订单意图 - 顺序执行
        // coffee_advisor（推荐） → order_assistant（下单） → customer_service（营销）
        rules.put(IntentType.ORDER_PURCHASE, RouteRule.builder()
                .intentType(IntentType.ORDER_PURCHASE)
                .strategy(ExecutionStrategy.SEQUENTIAL)
                .agentChain(Arrays.asList(
                        "coffee_advisor",
                        "order_assistant",
                        "customer_service"
                ))
                .priority(1)
                .aggregateResults(true)
                .contextPassing(ContextPassingConfig.builder()
                        .passMessageHistory(true)
                        .passToolResults(true)
                        .passReasoning(false)
                        .maxHistoryRounds(5)
                        .build())
                .build());

        // 规则 2: 投诉处理意图 - 顺序执行
        // customer_service（初步接待） → customer_service（深度处理）
        rules.put(IntentType.COMPLAINT, RouteRule.builder()
                .intentType(IntentType.COMPLAINT)
                .strategy(ExecutionStrategy.SEQUENTIAL)
                .agentChain(Arrays.asList(
                        "customer_service",
                        "customer_service"
                ))
                .priority(2)
                .aggregateResults(false)
                .contextPassing(ContextPassingConfig.builder()
                        .passMessageHistory(true)
                        .passToolResults(true)
                        .passReasoning(true)
                        .maxHistoryRounds(10)
                        .build())
                .build());

        // 规则 3: 咨询意图 - 单 Agent 执行
        // coffee_advisor（咖啡顾问）
        rules.put(IntentType.CONSULT, RouteRule.builder()
                .intentType(IntentType.CONSULT)
                .strategy(ExecutionStrategy.SINGLE)
                .agentChain(Collections.singletonList("coffee_advisor"))
                .priority(3)
                .aggregateResults(false)
                .contextPassing(ContextPassingConfig.builder()
                        .passMessageHistory(true)
                        .passToolResults(true)
                        .passReasoning(false)
                        .maxHistoryRounds(3)
                        .build())
                .build());

        // 规则 4: 订单查询意图 - 单 Agent 执行
        // order_assistant（订单助手）
        rules.put(IntentType.ORDER_QUERY, RouteRule.builder()
                .intentType(IntentType.ORDER_QUERY)
                .strategy(ExecutionStrategy.SINGLE)
                .agentChain(Collections.singletonList("order_assistant"))
                .priority(4)
                .aggregateResults(false)
                .contextPassing(ContextPassingConfig.builder()
                        .passMessageHistory(true)
                        .passToolResults(true)
                        .passReasoning(false)
                        .maxHistoryRounds(3)
                        .build())
                .build());

        // 规则 5: 一般对话意图 - 单 Agent 执行
        // general_chat（通用聊天）
        rules.put(IntentType.GENERAL, RouteRule.builder()
                .intentType(IntentType.GENERAL)
                .strategy(ExecutionStrategy.SINGLE)
                .agentChain(Collections.singletonList("general_chat"))
                .priority(5)
                .aggregateResults(false)
                .contextPassing(ContextPassingConfig.builder()
                        .passMessageHistory(true)
                        .passToolResults(false)
                        .passReasoning(false)
                        .maxHistoryRounds(5)
                        .build())
                .build());

        log.info("默认路由规则配置初始化完成，共 {} 条规则", rules.size());
        return rules;
    }

    /**
     * 创建意图识别专用 ChatModel
     * 使用较低的温度和较少的 token 数量
     *
     * @return 意图识别 ChatModel
     */
    @Bean(name = "intentRecognitionChatModel")
    @Schema(description = "意图识别专用 ChatModel")
    public org.springframework.ai.chat.model.ChatModel intentRecognitionChatModel() {
        log.info("初始化意图识别专用 ChatModel");
        return chatModelFactory.createChatModel();
    }

    /**
     * 创建意图识别 Agent 配置
     * 专门用于识别用户意图
     *
     * @return 意图识别 Agent 配置
     */
    @Bean(name = "intentRecognitionAgentConfig")
    @Schema(description = "意图识别 Agent 配置")
    public AgentConfig.AgentConfigInfo intentRecognitionAgentConfig() {
        log.info("初始化意图识别 Agent 配置");

        // 构建意图类型描述
        String intentDescriptions = Arrays.stream(IntentType.values())
                .map(intent -> String.format("- %s: %s", intent.getCode(), intent.getDescription()))
                .collect(Collectors.joining("\n"));

        AgentConfig.AgentConfigInfo config = AgentConfig.AgentConfigInfo.builder()
                .name("intent_recognition")
                .description("智能意图识别 Agent")
                .systemPrompt(String.format("""
                        你是 Haight Ashbury Coffee 的意图识别助手，负责分析用户输入并识别其意图类型。

                        ## 支持的意图类型
                        %s

                        ## 识别规则
                        1. 用户明确提到"买"、"下单"、"订购"、"支付"等 → order_purchase
                        2. 用户提到"投诉"、"问题"、"不满"、"质量差"等 → complaint
                        3. 用户询问咖啡相关问题（口味、推荐、价格） → consult
                        4. 用户询问"订单"、"我的订单"、"订单状态" → order_query
                        5. 其他一般性对话或无法明确分类 → general

                        ## 输出格式
                        仅返回意图类型的 code，不要包含其他内容。

                        ## 示例
                        - "我想买一杯美式咖啡" → order_purchase
                        - "你们的服务太差了" → complaint
                        - "哪种咖啡不太酸？" → consult
                        - "我的订单到哪了？" → order_query
                        - "你好" → general
                        """, intentDescriptions))
                .tools(new ArrayList<>()) // 意图识别不需要工具
                .chatModel(chatModelFactory.createChatModel())
                .temperature(0.3) // 使用较低温度以提高识别准确性
                .maxTokens(50) // 意图识别输出很短
                .build();

        log.info("意图识别 Agent 配置初始化完成");
        return config;
    }

    /**
     * 创建路由 Agent 配置
     * 主路由 Agent，负责协调子 Agent 的执行
     *
     * @return 路由 Agent 配置
     */
    @Bean(name = "routingAgentConfig")
    @Schema(description = "路由 Agent 配置")
    public AgentConfig.AgentConfigInfo routingAgentConfig() {
        log.info("初始化路由 Agent 配置");

        AgentConfig.AgentConfigInfo config = AgentConfig.AgentConfigInfo.builder()
                .name("routing_agent")
                .description("智能路由 Agent，协调子 Agent 执行")
                .systemPrompt("""
                        你是 Haight Ashbury Coffee 的智能路由协调器。

                        ## 职责
                        1. 根据识别的用户意图，选择合适的子 Agent 处理请求
                        2. 协调多个 Agent 的执行（顺序或并行）
                        3. 收集并整合子 Agent 的执行结果
                        4. 向用户提供统一、连贯的回复

                        ## 可用子 Agent
                        - coffee_advisor: 咖啡顾问，推荐和咨询
                        - order_assistant: 订单助手，处理订单相关操作
                        - customer_service: 客服助手，处理服务和投诉
                        - general_chat: 通用聊天，处理一般对话

                        ## 执行策略
                        - SINGLE: 单 Agent 执行
                        - SEQUENTIAL: 顺序执行多个 Agent，将前一个 Agent 的结果传递给下一个
                        - PARALLEL: 并行执行多个 Agent，同时收集所有结果

                        ## 上下文传递
                        - 在顺序执行时，将前一个 Agent 的输出作为后一个 Agent 的输入
                        - 记录每个 Agent 的执行过程，便于追踪和调试
                        - 根据需要聚合多个 Agent 的结果

                        ## 回复原则
                        - 保持回复的自然和连贯
                        - 如果涉及多个 Agent，清晰说明每个环节的处理结果
                        - 对于复杂请求，逐步引导用户完成
                        """)
                .tools(toolRegistrar.getRoutingTools())
                .chatModel(chatModelFactory.createChatModel())
                .temperature(0.6)
                .build();

        log.info("路由 Agent 配置初始化完成");
        return config;
    }

    /**
     * 获取意图类型列表
     *
     * @return 所有意图类型
     */
    @Schema(description = "获取所有意图类型")
    public List<IntentType> getIntentTypes() {
        return Arrays.asList(IntentType.values());
    }

    /**
     * 根据代码获取意图类型
     *
     * @param code 意图代码
     * @return 意图类型，如果不存在返回 null
     */
    @Schema(description = "根据代码获取意图类型")
    public IntentType getIntentTypeByCode(String code) {
        return Arrays.stream(IntentType.values())
                .filter(intent -> intent.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }

    /**
     * 验证 Agent 执行链的有效性
     * 检查执行链中的所有 Agent 是否都已注册
     *
     * @param agentChain Agent 执行链
     * @return 是否有效
     */
    @Schema(description = "验证 Agent 执行链的有效性")
    public boolean validateAgentChain(List<String> agentChain) {
        return agentChain.stream()
                .allMatch(agentRegistry::contains);
    }

    /**
     * 获取路由规则统计信息
     *
     * @param rules 路由规则映射
     * @return 统计信息
     */
    @Schema(description = "获取路由规则统计信息")
    public RouteRulesStats getRouteRulesStats(Map<IntentType, RouteRule> rules) {
        return RouteRulesStats.builder()
                .totalRules(rules.size())
                .sequentialCount((int) rules.values().stream()
                        .filter(rule -> rule.getStrategy() == ExecutionStrategy.SEQUENTIAL)
                        .count())
                .parallelCount((int) rules.values().stream()
                        .filter(rule -> rule.getStrategy() == ExecutionStrategy.PARALLEL)
                        .count())
                .singleCount((int) rules.values().stream()
                        .filter(rule -> rule.getStrategy() == ExecutionStrategy.SINGLE)
                        .count())
                .build();
    }

    /**
     * 路由规则统计信息
     */
    @Schema(description = "路由规则统计信息")
    @lombok.Builder
    @lombok.Data
    public static class RouteRulesStats {
        /**
         * 总规则数
         */
        @Schema(description = "总规则数")
        private int totalRules;

        /**
         * 顺序执行规则数
         */
        @Schema(description = "顺序执行规则数")
        private int sequentialCount;

        /**
         * 并行执行规则数
         */
        @Schema(description = "并行执行规则数")
        private int parallelCount;

        /**
         * 单 Agent 执行规则数
         */
        @Schema(description = "单 Agent 执行规则数")
        private int singleCount;
    }
}

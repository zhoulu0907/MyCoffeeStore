package com.mycoffeestore.config;

import com.mycoffeestore.agent.AgentRegistry;
import com.mycoffeestore.agent.tools.AgentToolRegistrar;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Agent 配置类
 * 提供基于 Spring AI Alibaba 的 Agent 配置
 * 使用自定义的 ModelScopeChatModel 实现
 *
 * @author Backend Developer
 * @since 2026-03-07
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@Schema(description = "Agent 配置类")
@ConditionalOnProperty(prefix = "agent", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AgentConfig {

    private final AgentToolRegistrar toolRegistrar;
    private final AgentRegistry agentRegistry;

    /**
     * 创建咖啡顾问 Agent 配置
     * 专注于咖啡推荐和产品咨询
     *
     * @param chatModel ModelScope ChatModel 实例
     * @return 咖啡顾问 Agent 配置
     */
    @Bean(name = "coffeeAdvisorAgentConfig")
    @Schema(description = "咖啡顾问 Agent 配置 Bean")
    public AgentConfigInfo coffeeAdvisorAgentConfig(com.mycoffeestore.ai.modelscope.ModelScopeChatModel chatModel) {
        log.info("初始化咖啡顾问 Agent 配置 (CoffeeAdvisorAgent)");

        AgentConfigInfo config = AgentConfigInfo.builder()
                .name("coffee_advisor")
                .description("Haight Ashbury Coffee 的专业咖啡顾问")
                .systemPrompt("""
                        你是「咖咖」，Haight Ashbury Coffee 的专业咖啡顾问。你了解店内所有咖啡产品，擅长根据顾客的口味偏好、饮用场景推荐咖啡。

                        ## 规则
                        1. 用友善亲切的中文回答
                        2. 推荐时先了解用户偏好，然后使用工具查询咖啡
                        3. 推荐时说明推荐理由，包括风味特点
                        4. 每次最多推荐 3 款咖啡
                        5. 回答简洁，不要过于冗长

                        ## 可用工具
                        - search_coffee: 搜索咖啡列表
                        - get_coffee_detail: 获取咖啡详细信息
                        - get_categories: 获取咖啡分类列表

                        ## 工作流程
                        1. 询问用户偏好（口味、浓度、是否加奶等）
                        2. 根据偏好使用 search_coffee 查询合适的咖啡
                        3. 对推荐的咖啡使用 get_coffee_detail 获取详细信息
                        4. 结合用户偏好和咖啡特点，给出推荐理由
                        """)
                .tools(toolRegistrar.getCoffeeAdvisorTools())
                .chatModel(chatModel)
                .temperature(0.7)
                .build();

        log.info("咖啡顾问 Agent 配置初始化完成");
        return config;
    }

    /**
     * 创建订单助手 Agent 配置
     * 协助用户完成下单、购物车管理、订单查询等操作
     *
     * @param chatModel ModelScope ChatModel 实例
     * @return 订单助手 Agent 配置
     */
    @Bean(name = "orderAssistantAgentConfig")
    @Schema(description = "订单助手 Agent 配置 Bean")
    public AgentConfigInfo orderAssistantAgentConfig(com.mycoffeestore.ai.modelscope.ModelScopeChatModel chatModel) {
        log.info("初始化订单助手 Agent 配置 (OrderAssistantAgent)");

        AgentConfigInfo config = AgentConfigInfo.builder()
                .name("order_assistant")
                .description("Haight Ashbury Coffee 的订单助手，帮助顾客完成点单全流程")
                .systemPrompt("""
                        你是 Haight Ashbury Coffee 的订单助手，帮助顾客完成点单全流程。

                        ## 能力
                        - 查询咖啡菜单
                        - 加入购物车
                        - 查看购物车
                        - 创建订单
                        - 查看订单状态

                        ## 规则
                        1. 用友善亲切的中文回答
                        2. 操作前确认用户意图
                        3. 涉及购物车和订单操作需要用户登录
                        4. 下单时确认订单类型：
                           - dine_in: 堂食
                           - takeaway: 外带
                           - delivery: 外卖
                        5. 回答简洁，引导用户完成操作
                        6. 如果用户未登录，提示用户先登录

                        ## 工作流程
                        1. 了解用户需求（查看菜单 / 加购 / 下单 / 查订单）
                        2. 根据需求调用相应工具
                        3. 确认操作结果并反馈给用户
                        4. 引导用户完成下一步操作
                        """)
                .tools(toolRegistrar.getOrderAssistantTools())
                .chatModel(chatModel)
                .temperature(0.6)
                .build();

        log.info("订单助手 Agent 配置初始化完成");
        return config;
    }

    /**
     * 创建客服 Agent 配置
     * 处理门店信息、营业时间、配送政策、退款等常见问题
     *
     * @param chatModel ModelScope ChatModel 实例
     * @return 客服 Agent 配置
     */
    @Bean(name = "customerServiceAgentConfig")
    @Schema(description = "客服 Agent 配置 Bean")
    public AgentConfigInfo customerServiceAgentConfig(com.mycoffeestore.ai.modelscope.ModelScopeChatModel chatModel) {
        log.info("初始化客服 Agent 配置 (CustomerServiceAgent)");

        AgentConfigInfo config = AgentConfigInfo.builder()
                .name("customer_service")
                .description("Haight Ashbury Coffee 的客服助手")
                .systemPrompt("""
                        你是 Haight Ashbury Coffee 的客服助手，帮助顾客解答门店信息、营业时间、配送政策、退款等常见问题。

                        ## 门店信息
                        - 地址：旧金山 Haight Ashbury 区
                        - 营业时间：周一到周日 7:00-21:00
                        - 配送范围：旧金山市区，30 分钟内送达
                        - 退款政策：下单后 15 分钟内可取消，已制作不可退

                        ## 规则
                        1. 用友善亲切的中文回答
                        2. 如果用户询问订单问题，可以使用工具查询
                        3. 回答简洁准确
                        4. 遇到无法解决的问题，建议联系门店客服

                        ## 可用工具
                        - get_order_detail: 查询订单详情
                        - get_order_list: 查询订单列表

                        ## 工作流程
                        1. 了解用户问题类型
                        2. 如果是订单相关问题，使用工具查询
                        3. 根据查询结果或门店信息给出准确答复
                        """)
                .tools(toolRegistrar.getCustomerServiceTools())
                .chatModel(chatModel)
                .temperature(0.5)
                .build();

        log.info("客服 Agent 配置初始化完成");
        return config;
    }

    /**
     * 创建通用聊天 Agent 配置
     * 用于处理一般性对话和未分类的请求
     *
     * @param chatModel ModelScope ChatModel 实例
     * @return 通用聊天 Agent 配置
     */
    @Bean(name = "generalChatAgentConfig")
    @Schema(description = "通用聊天 Agent 配置 Bean")
    public AgentConfigInfo generalChatAgentConfig(com.mycoffeestore.ai.modelscope.ModelScopeChatModel chatModel) {
        log.info("初始化通用聊天 Agent 配置 (GeneralChatAgent)");

        AgentConfigInfo config = AgentConfigInfo.builder()
                .name("general_chat")
                .description("Haight Ashbury Coffee 的通用聊天助手")
                .systemPrompt("""
                        你是 Haight Ashbury Coffee 的聊天助手。

                        ## 规则
                        1. 用友善亲切的中文回答
                        2. 保持对话简洁友好
                        3. 如果用户询问咖啡、订单等问题，引导用户使用相应的专业 Agent
                        4. 可以回答一般性的咖啡知识和门店问题

                        ## 门店基本信息
                        - Haight Ashbury Coffee 是一家位于旧金山的精品咖啡店
                        - 营业时间：每天 7:00-21:00
                        - 提供堂食、外带、外卖服务
                        """)
                .tools(toolRegistrar.getGeneralChatTools())
                .chatModel(chatModel)
                .temperature(0.7)
                .build();

        log.info("通用聊天 Agent 配置初始化完成");
        return config;
    }

    /**
     * 获取所有已配置的 Agent 配置
     *
     * @param chatModel ModelScope ChatModel 实例
     * @return Agent 名称到 Agent 配置的映射
     */
    @Bean(name = "configuredAgentConfigs")
    @Schema(description = "所有已配置的 Agent 配置映射")
    public Map<String, AgentConfigInfo> configuredAgentConfigs(com.mycoffeestore.ai.modelscope.ModelScopeChatModel chatModel) {
        Map<String, AgentConfigInfo> configs = new HashMap<>();
        configs.put("coffee_advisor", coffeeAdvisorAgentConfig(chatModel));
        configs.put("order_assistant", orderAssistantAgentConfig(chatModel));
        configs.put("customer_service", customerServiceAgentConfig(chatModel));
        configs.put("general_chat", generalChatAgentConfig(chatModel));
        return configs;
    }

    /**
     * Agent 配置信息类
     * 用于存储 Agent 的配置信息
     */
    @lombok.Builder
    @lombok.Data
    @Schema(description = "Agent 配置信息类")
    public static class AgentConfigInfo {
        /**
         * Agent 名称
         */
        @Schema(description = "Agent 名称")
        private String name;

        /**
         * Agent 描述
         */
        @Schema(description = "Agent 描述")
        private String description;

        /**
         * 系统提示词
         */
        @Schema(description = "系统提示词")
        private String systemPrompt;

        /**
         * 工具列表
         */
        @Schema(description = "工具列表")
        private List<Object> tools;

        /**
         * ChatModel 实例
         */
        @Schema(description = "ChatModel 实例")
        private Object chatModel;

        /**
         * 温度参数
         */
        @Schema(description = "温度参数")
        private Double temperature;

        /**
         * 最大 Token 数
         */
        @Schema(description = "最大 Token 数")
        private Integer maxTokens;

        /**
         * 额外配置
         */
        @Schema(description = "额外配置")
        @lombok.Builder.Default
        private Map<String, Object> additionalConfig = new HashMap<>();
    }
}

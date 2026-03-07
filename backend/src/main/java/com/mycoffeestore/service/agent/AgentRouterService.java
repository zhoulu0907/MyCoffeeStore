package com.mycoffeestore.service.agent;

/**
 * Agent 智能路由服务接口
 * <p>
 * 根据用户意图自动选择最合适的 Agent 类型
 *
 * @author Backend Developer
 * @since 2026-03-07
 */
public interface AgentRouterService {

    /**
     * 支持的 Agent 类型
     */
    String AGENT_COFFEE_ADVISOR = "coffee_advisor";
    String AGENT_CUSTOMER_SERVICE = "customer_service";
    String AGENT_ORDER_ASSISTANT = "order_assistant";

    /**
     * 根据用户消息智能路由到合适的 Agent
     *
     * @param userMessage 用户消息
     * @return Agent 类型
     */
    String route(String userMessage);

    /**
     * 获取路由的置信度
     *
     * @param userMessage 用户消息
     * @return 置信度（0.0-1.0）
     */
    double getConfidence(String userMessage);

    /**
     * 获取路由理由（用于调试和日志）
     *
     * @param userMessage 用户消息
     * @return 路由理由
     */
    String getRouteReason(String userMessage);
}

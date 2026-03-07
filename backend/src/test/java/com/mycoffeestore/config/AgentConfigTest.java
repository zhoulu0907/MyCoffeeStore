package com.mycoffeestore.config;

import com.mycoffeestore.agent.AgentRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Agent 配置测试
 * 验证所有 Agent 配置是否正确创建和注册
 *
 * @author Backend Developer
 * @since 2026-03-07
 */
@SpringBootTest
@ActiveProfiles("test")
class AgentConfigTest {

    @Autowired(required = false)
    @Qualifier("coffeeAdvisorAgentConfig")
    private AgentConfig.AgentConfigInfo coffeeAdvisorAgentConfig;

    @Autowired(required = false)
    @Qualifier("orderAssistantAgentConfig")
    private AgentConfig.AgentConfigInfo orderAssistantAgentConfig;

    @Autowired(required = false)
    @Qualifier("customerServiceAgentConfig")
    private AgentConfig.AgentConfigInfo customerServiceAgentConfig;

    @Autowired(required = false)
    @Qualifier("generalChatAgentConfig")
    private AgentConfig.AgentConfigInfo generalChatAgentConfig;

    @Autowired(required = false)
    @Qualifier("configuredAgentConfigs")
    private Map<String, AgentConfig.AgentConfigInfo> configuredAgentConfigs;

    @Autowired(required = false)
    private AgentRegistry agentRegistry;

    /**
     * 测试咖啡顾问 Agent 配置创建
     */
    @Test
    void testCoffeeAdvisorAgentConfigCreated() {
        assertNotNull(coffeeAdvisorAgentConfig, "咖啡顾问 Agent 配置应该被创建");
        assertEquals("coffee_advisor", coffeeAdvisorAgentConfig.getName());
        assertNotNull(coffeeAdvisorAgentConfig.getDescription());
        assertNotNull(coffeeAdvisorAgentConfig.getSystemPrompt());
        assertNotNull(coffeeAdvisorAgentConfig.getChatModel());
        assertEquals(0.7, coffeeAdvisorAgentConfig.getTemperature());
    }

    /**
     * 测试订单助手 Agent 配置创建
     */
    @Test
    void testOrderAssistantAgentConfigCreated() {
        assertNotNull(orderAssistantAgentConfig, "订单助手 Agent 配置应该被创建");
        assertEquals("order_assistant", orderAssistantAgentConfig.getName());
        assertNotNull(orderAssistantAgentConfig.getDescription());
        assertNotNull(orderAssistantAgentConfig.getSystemPrompt());
        assertNotNull(orderAssistantAgentConfig.getChatModel());
        assertEquals(0.6, orderAssistantAgentConfig.getTemperature());
    }

    /**
     * 测试客服 Agent 配置创建
     */
    @Test
    void testCustomerServiceAgentConfigCreated() {
        assertNotNull(customerServiceAgentConfig, "客服 Agent 配置应该被创建");
        assertEquals("customer_service", customerServiceAgentConfig.getName());
        assertNotNull(customerServiceAgentConfig.getDescription());
        assertNotNull(customerServiceAgentConfig.getSystemPrompt());
        assertNotNull(customerServiceAgentConfig.getChatModel());
        assertEquals(0.5, customerServiceAgentConfig.getTemperature());
    }

    /**
     * 测试通用聊天 Agent 配置创建
     */
    @Test
    void testGeneralChatAgentConfigCreated() {
        assertNotNull(generalChatAgentConfig, "通用聊天 Agent 配置应该被创建");
        assertEquals("general_chat", generalChatAgentConfig.getName());
        assertNotNull(generalChatAgentConfig.getDescription());
        assertNotNull(generalChatAgentConfig.getSystemPrompt());
        assertNotNull(generalChatAgentConfig.getChatModel());
        assertEquals(0.7, generalChatAgentConfig.getTemperature());
    }

    /**
     * 测试所有 Agent 配置映射创建
     */
    @Test
    void testConfiguredAgentConfigsMap() {
        assertNotNull(configuredAgentConfigs, "配置的 Agent 映射应该存在");
        assertEquals(4, configuredAgentConfigs.size(), "应该有 4 个配置的 Agent");
        assertTrue(configuredAgentConfigs.containsKey("coffee_advisor"));
        assertTrue(configuredAgentConfigs.containsKey("order_assistant"));
        assertTrue(configuredAgentConfigs.containsKey("customer_service"));
        assertTrue(configuredAgentConfigs.containsKey("general_chat"));
    }

    /**
     * 测试 Agent 注册中心
     */
    @Test
    void testAgentRegistry() {
        assertNotNull(agentRegistry, "Agent 注册中心应该被创建");

        // 注意：由于我们使用的是配置模式，需要在启动时手动注册
        // 这里测试注册中心的基本功能

        // 测试推荐功能
        String coffeeAgentName = agentRegistry.getRecommendedAgentName("coffee");
        assertEquals("coffee_advisor", coffeeAgentName);

        String orderAgentName = agentRegistry.getRecommendedAgentName("order");
        assertEquals("order_assistant", orderAgentName);

        String serviceAgentName = agentRegistry.getRecommendedAgentName("service");
        assertEquals("customer_service", serviceAgentName);

        String defaultAgentName = agentRegistry.getRecommendedAgentName("unknown");
        assertEquals("general_chat", defaultAgentName);
    }

    /**
     * 测试注册中心统计信息
     */
    @Test
    void testRegistryStats() {
        assertNotNull(agentRegistry, "Agent 注册中心应该被创建");

        AgentRegistry.RegistryStats stats = agentRegistry.getStats();
        assertNotNull(stats);
        // 注意：由于使用配置模式，初始状态可能为 0
        // 实际使用时需要在应用启动时注册配置
        assertNotNull(stats.getAgentNames());
    }
}

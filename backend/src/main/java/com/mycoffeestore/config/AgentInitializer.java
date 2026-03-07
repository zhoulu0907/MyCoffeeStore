package com.mycoffeestore.config;

import com.mycoffeestore.agent.AgentRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;

/**
 * Agent 初始化器
 * 在应用启动时注册所有 Agent 配置到注册中心
 *
 * @author Backend Developer
 * @since 2026-03-07
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AgentInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final AgentConfig.AgentConfigInfo coffeeAdvisorAgentConfig;
    private final AgentConfig.AgentConfigInfo orderAssistantAgentConfig;
    private final AgentConfig.AgentConfigInfo customerServiceAgentConfig;
    private final AgentConfig.AgentConfigInfo generalChatAgentConfig;
    private final AgentRegistry agentRegistry;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("开始注册 Agent 配置到注册中心");

        // 注册所有 Agent 配置
        agentRegistry.register(coffeeAdvisorAgentConfig);
        log.info("已注册咖啡顾问 Agent");

        agentRegistry.register(orderAssistantAgentConfig);
        log.info("已注册订单助手 Agent");

        agentRegistry.register(customerServiceAgentConfig);
        log.info("已注册客服 Agent");

        agentRegistry.register(generalChatAgentConfig);
        log.info("已注册通用聊天 Agent");

        // 输出注册统计信息
        AgentRegistry.RegistryStats stats = agentRegistry.getStats();
        log.info("Agent 注册完成，总计: {} 个", stats.getTotalAgents());
        log.info("可用的 Agent: {}", stats.getAgentNames());
    }
}

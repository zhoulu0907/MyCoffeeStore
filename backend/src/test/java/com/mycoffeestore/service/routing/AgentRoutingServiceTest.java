package com.mycoffeestore.service.routing;

import com.mycoffeestore.agent.routing.AgentRoutingConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 智能路由服务测试
 * 验证意图识别和路由配置功能
 *
 * @author Backend Developer
 * @since 2026-03-07
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("智能路由服务测试")
public class AgentRoutingServiceTest {

    @Autowired(required = false)
    private IntentRecognitionService intentRecognitionService;

    @Autowired(required = false)
    private Map<AgentRoutingConfig.IntentType, AgentRoutingConfig.RouteRule> routeRules;

    @Test
    @DisplayName("测试意图识别服务注入")
    public void testIntentRecognitionServiceInjection() {
        assertThat(intentRecognitionService).isNotNull();
    }

    @Test
    @DisplayName("测试路由规则注入")
    public void testRouteRulesInjection() {
        assertThat(routeRules).isNotNull();
        assertThat(routeRules).hasSize(5); // 应该有 5 条路由规则
    }

    @Test
    @DisplayName("测试购买意图识别")
    public void testOrderPurchaseIntentRecognition() {
        if (intentRecognitionService == null) {
            return; // 跳过测试如果服务未注入
        }

        // 测试购买相关消息
        List<String> purchaseMessages = List.of(
                "我想买一杯美式咖啡",
                "下单一个拿铁",
                "我要订购三杯咖啡",
                "结账，我要支付"
        );

        for (String message : purchaseMessages) {
            IntentRecognitionService.IntentRecognitionResult result =
                    intentRecognitionService.recognizeIntent(message);

            assertThat(result).isNotNull();
            assertThat(result.getIntentType()).isEqualTo(AgentRoutingConfig.IntentType.ORDER_PURCHASE);
            assertThat(result.getConfidence()).isGreaterThan(0.5);
            assertThat(result.getRecommendedAgents()).contains("coffee_advisor", "order_assistant");
        }
    }

    @Test
    @DisplayName("测试投诉意图识别")
    public void testComplaintIntentRecognition() {
        if (intentRecognitionService == null) {
            return;
        }

        List<String> complaintMessages = List.of(
                "你们的服务太差了",
                "我要投诉",
                "咖啡质量有问题",
                "态度太差了"
        );

        for (String message : complaintMessages) {
            IntentRecognitionService.IntentRecognitionResult result =
                    intentRecognitionService.recognizeIntent(message);

            assertThat(result).isNotNull();
            assertThat(result.getIntentType()).isEqualTo(AgentRoutingConfig.IntentType.COMPLAINT);
        }
    }

    @Test
    @DisplayName("测试咨询意图识别")
    public void testConsultIntentRecognition() {
        if (intentRecognitionService == null) {
            return;
        }

        List<String> consultMessages = List.of(
                "推荐一款咖啡",
                "哪种咖啡不太酸？",
                "有什么浓一点的咖啡吗？",
                "新手适合喝什么咖啡"
        );

        for (String message : consultMessages) {
            IntentRecognitionService.IntentRecognitionResult result =
                    intentRecognitionService.recognizeIntent(message);

            assertThat(result).isNotNull();
            assertThat(result.getIntentType()).isEqualTo(AgentRoutingConfig.IntentType.CONSULT);
        }
    }

    @Test
    @DisplayName("测试订单查询意图识别")
    public void testOrderQueryIntentRecognition() {
        if (intentRecognitionService == null) {
            return;
        }

        List<String> queryMessages = List.of(
                "我的订单到哪了？",
                "查询一下订单状态",
                "看一下我的订单",
                "订单进度怎么样了"
        );

        for (String message : queryMessages) {
            IntentRecognitionService.IntentRecognitionResult result =
                    intentRecognitionService.recognizeIntent(message);

            assertThat(result).isNotNull();
            assertThat(result.getIntentType()).isEqualTo(AgentRoutingConfig.IntentType.ORDER_QUERY);
        }
    }

    @Test
    @DisplayName("测试一般对话意图识别")
    public void testGeneralIntentRecognition() {
        if (intentRecognitionService == null) {
            return;
        }

        List<String> generalMessages = List.of(
                "你好",
                "在吗",
                "早上好",
                "谢谢"
        );

        for (String message : generalMessages) {
            IntentRecognitionService.IntentRecognitionResult result =
                    intentRecognitionService.recognizeIntent(message);

            assertThat(result).isNotNull();
            assertThat(result.getIntentType()).isEqualTo(AgentRoutingConfig.IntentType.GENERAL);
        }
    }

    @Test
    @DisplayName("测试路由规则配置")
    public void testRouteRulesConfiguration() {
        if (routeRules == null) {
            return;
        }

        // 验证购买订单意图的规则
        AgentRoutingConfig.RouteRule purchaseRule = routeRules.get(AgentRoutingConfig.IntentType.ORDER_PURCHASE);
        assertThat(purchaseRule).isNotNull();
        assertThat(purchaseRule.getStrategy()).isEqualTo(AgentRoutingConfig.ExecutionStrategy.SEQUENTIAL);
        assertThat(purchaseRule.getAgentChain()).containsExactly("coffee_advisor", "order_assistant", "customer_service");

        // 验证投诉意图的规则
        AgentRoutingConfig.RouteRule complaintRule = routeRules.get(AgentRoutingConfig.IntentType.COMPLAINT);
        assertThat(complaintRule).isNotNull();
        assertThat(complaintRule.getStrategy()).isEqualTo(AgentRoutingConfig.ExecutionStrategy.SEQUENTIAL);

        // 验证咨询意图的规则
        AgentRoutingConfig.RouteRule consultRule = routeRules.get(AgentRoutingConfig.IntentType.CONSULT);
        assertThat(consultRule).isNotNull();
        assertThat(consultRule.getStrategy()).isEqualTo(AgentRoutingConfig.ExecutionStrategy.SINGLE);
        assertThat(consultRule.getAgentChain()).containsExactly("coffee_advisor");
    }

    @Test
    @DisplayName("测试所有意图类型覆盖")
    public void testAllIntentTypesCovered() {
        if (routeRules == null) {
            return;
        }

        // 确保所有意图类型都有对应的路由规则
        for (AgentRoutingConfig.IntentType intentType : AgentRoutingConfig.IntentType.values()) {
            AgentRoutingConfig.RouteRule rule = routeRules.get(intentType);
            assertThat(rule).isNotNull();
            assertThat(rule.getIntentType()).isEqualTo(intentType);
            assertThat(rule.getAgentChain()).isNotEmpty();
        }
    }
}

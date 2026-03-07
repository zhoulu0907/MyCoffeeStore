package com.mycoffeestore.service.impl.agent;

import com.mycoffeestore.service.agent.AgentRouterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Agent 智能路由服务实现
 * <p>
 * 基于规则的关键词匹配，根据用户消息自动选择最合适的 Agent
 * 优先级：订单助手 > 咖啡顾问 > 客服助手
 *
 * @author Backend Developer
 * @since 2026-03-07
 */
@Slf4j
@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class AgentRouterServiceImpl implements AgentRouterService {

    /**
     * 订单助手关键词（最高优先级）
     */
    private static final List<String> ORDER_KEYWORDS = Arrays.asList(
            "下单", "点单", "购买", "买", "订单", "购物车", "支付", "结算",
            "加购", "要一杯", "要一单", "我要", "来一杯", "来一单",
            "order", "cart", "buy", "purchase"
    );

    /**
     * 咖啡顾问关键词
     */
    private static final List<String> COFFEE_KEYWORDS = Arrays.asList(
            "推荐", "建议", "什么咖啡", "哪种", "哪款", "口味", "风味",
            "酸", "苦", "甜", "香", "浓", "淡", "推荐款", "推荐咖啡",
            "recommend", "suggest", "coffee", "flavor", "taste"
    );

    /**
     * 客服助手关键词
     */
    private static final List<String> SERVICE_KEYWORDS = Arrays.asList(
            "营业时间", "地址", "位置", "门店", "配送", "送达", "退款",
            "投诉", "联系", "电话", "营业", "配送范围", "配送费",
            "hours", "address", "location", "store", "delivery", "refund"
    );

    /**
     * 咖啡产品关键词（辅助判断）
     */
    private static final List<String> COFFEE_PRODUCT_KEYWORDS = Arrays.asList(
            "美式", "拿铁", "卡布奇诺", "摩卡", "浓缩", "玛奇朵",
            "latte", "cappuccino", "mocha", "espresso", "macchiato"
    );

    @Override
    public String route(String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            log.debug("用户消息为空，默认路由到咖啡顾问");
            return AGENT_COFFEE_ADVISOR;
        }

        String message = userMessage.toLowerCase();

        // 优先级 1: 订单相关
        if (containsAnyKeyword(message, ORDER_KEYWORDS)) {
            log.debug("路由到订单助手: {}", userMessage);
            return AGENT_ORDER_ASSISTANT;
        }

        // 优先级 2: 咖啡推荐相关
        if (containsAnyKeyword(message, COFFEE_KEYWORDS) ||
            containsAnyKeyword(message, COFFEE_PRODUCT_KEYWORDS)) {
            log.debug("路由到咖啡顾问: {}", userMessage);
            return AGENT_COFFEE_ADVISOR;
        }

        // 优先级 3: 客服相关
        if (containsAnyKeyword(message, SERVICE_KEYWORDS)) {
            log.debug("路由到客服助手: {}", userMessage);
            return AGENT_CUSTOMER_SERVICE;
        }

        // 默认路由到咖啡顾问
        log.debug("未匹配到明确意图，默认路由到咖啡顾问: {}", userMessage);
        return AGENT_COFFEE_ADVISOR;
    }

    @Override
    public double getConfidence(String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return 0.3;
        }

        String message = userMessage.toLowerCase();
        int orderCount = countKeywords(message, ORDER_KEYWORDS);
        int coffeeCount = countKeywords(message, COFFEE_KEYWORDS) + countKeywords(message, COFFEE_PRODUCT_KEYWORDS);
        int serviceCount = countKeywords(message, SERVICE_KEYWORDS);

        int maxCount = Math.max(Math.max(orderCount, coffeeCount), serviceCount);
        if (maxCount == 0) {
            return 0.3;
        }

        // 置信度计算：匹配关键词数量 / 5，最高 0.95
        return Math.min(0.95, 0.3 + maxCount * 0.13);
    }

    @Override
    public String getRouteReason(String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return "用户消息为空，使用默认路由";
        }

        String message = userMessage.toLowerCase();
        int orderCount = countKeywords(message, ORDER_KEYWORDS);
        int coffeeCount = countKeywords(message, COFFEE_KEYWORDS) + countKeywords(message, COFFEE_PRODUCT_KEYWORDS);
        int serviceCount = countKeywords(message, SERVICE_KEYWORDS);

        if (orderCount > 0) {
            return String.format("检测到订单相关关键词（%d个），路由到订单助手", orderCount);
        } else if (coffeeCount > 0) {
            return String.format("检测到咖啡推荐相关关键词（%d个），路由到咖啡顾问", coffeeCount);
        } else if (serviceCount > 0) {
            return String.format("检测到客服相关关键词（%d个），路由到客服助手", serviceCount);
        } else {
            return "未检测到明确意图，使用默认路由（咖啡顾问）";
        }
    }

    /**
     * 检查消息是否包含任一关键词
     */
    private boolean containsAnyKeyword(String message, List<String> keywords) {
        return keywords.stream().anyMatch(keyword -> message.contains(keyword.toLowerCase()));
    }

    /**
     * 统计消息中包含的关键词数量
     */
    private int countKeywords(String message, List<String> keywords) {
        return (int) keywords.stream()
                .filter(keyword -> message.contains(keyword.toLowerCase()))
                .count();
    }
}

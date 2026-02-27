package com.mycoffeestore.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycoffeestore.dto.cart.CartAddDTO;
import com.mycoffeestore.dto.order.OrderCreateDTO;
import com.mycoffeestore.dto.order.OrderItemDTO;
import com.mycoffeestore.enums.OrderStatus;
import com.mycoffeestore.enums.OrderType;
import com.mycoffeestore.service.cart.CartService;
import com.mycoffeestore.service.coffee.CoffeeService;
import com.mycoffeestore.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Agent 工具执行器
 * 负责将模型的 function calling 分发到已有 Service 层执行
 *
 * @author zhoulu
 * @since 2026-02-27
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AgentToolExecutor {

    private final CoffeeService coffeeService;
    private final CartService cartService;
    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    // 角色对应的可用工具
    private static final Map<String, List<String>> ROLE_TOOLS = Map.of(
        "coffee_advisor", List.of("search_coffee", "get_coffee_detail", "get_categories"),
        "customer_service", List.of("get_order_detail", "get_order_list"),
        "order_assistant", List.of("search_coffee", "add_to_cart", "get_cart", "create_order", "get_order_list", "get_order_detail")
    );

    /**
     * 执行工具调用
     *
     * @param toolName 工具名
     * @param argsJson 参数 JSON 字符串
     * @param userId   用户ID（可为 null，表示未登录）
     * @return 结果 JSON 字符串
     */
    public String executeTool(String toolName, String argsJson, Long userId) {
        log.info("执行工具调用: {} 参数: {} userId: {}", toolName, argsJson, userId);
        try {
            JsonNode args = objectMapper.readTree(argsJson != null ? argsJson : "{}");
            Object result = switch (toolName) {
                case "search_coffee" -> {
                    String category = args.has("category") ? args.get("category").asText() : null;
                    int page = args.has("page") ? args.get("page").asInt() : 1;
                    int size = args.has("size") ? args.get("size").asInt() : 10;
                    yield coffeeService.list(category, page, size);
                }
                case "get_coffee_detail" -> {
                    long coffeeId = args.get("coffeeId").asLong();
                    yield coffeeService.detail(coffeeId);
                }
                case "get_categories" -> coffeeService.categories();
                case "add_to_cart" -> {
                    if (userId == null) yield Map.of("error", "该操作需要登录，请先登录");
                    CartAddDTO dto = CartAddDTO.builder()
                            .coffeeId(args.get("coffeeId").asLong())
                            .quantity(args.get("quantity").asInt())
                            .build();
                    cartService.add(userId, dto);
                    yield Map.of("success", true, "message", "已添加到购物车");
                }
                case "get_cart" -> {
                    if (userId == null) yield Map.of("error", "该操作需要登录，请先登录");
                    yield cartService.list(userId);
                }
                case "create_order" -> {
                    if (userId == null) yield Map.of("error", "该操作需要登录，请先登录");
                    String orderTypeStr = args.has("orderType") ? args.get("orderType").asText() : "dine_in";
                    OrderType orderType = OrderType.fromCode(orderTypeStr);
                    String remark = args.has("remark") ? args.get("remark").asText() : null;

                    // 从 items 参数构造订单项
                    List<OrderItemDTO> items = new ArrayList<>();
                    if (args.has("items") && args.get("items").isArray()) {
                        for (JsonNode itemNode : args.get("items")) {
                            OrderItemDTO item = OrderItemDTO.builder()
                                    .coffeeId(itemNode.get("coffeeId").asLong())
                                    .quantity(itemNode.get("quantity").asInt())
                                    .price(new BigDecimal(itemNode.get("price").asText()))
                                    .build();
                            items.add(item);
                        }
                    }

                    if (items.isEmpty()) {
                        yield Map.of("error", "订单项不能为空，请至少添加一个商品");
                    }

                    OrderCreateDTO dto = OrderCreateDTO.builder()
                            .orderType(orderType)
                            .items(items)
                            .remark(remark)
                            .build();
                    yield orderService.create(userId, dto);
                }
                case "get_order_list" -> {
                    if (userId == null) yield Map.of("error", "该操作需要登录，请先登录");
                    String statusStr = args.has("status") ? args.get("status").asText() : null;
                    OrderStatus status = statusStr != null ? OrderStatus.fromCode(statusStr) : null;
                    int page = args.has("page") ? args.get("page").asInt() : 1;
                    int size = args.has("size") ? args.get("size").asInt() : 10;
                    yield orderService.list(userId, status, page, size);
                }
                case "get_order_detail" -> {
                    if (userId == null) yield Map.of("error", "该操作需要登录，请先登录");
                    String orderId = args.get("orderId").asText();
                    yield orderService.detail(userId, orderId);
                }
                default -> Map.of("error", "未知工具: " + toolName);
            };
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            log.error("工具执行失败: {} - {}", toolName, e.getMessage(), e);
            try {
                return objectMapper.writeValueAsString(Map.of("error", "工具执行失败: " + e.getMessage()));
            } catch (JsonProcessingException ex) {
                return "{\"error\":\"工具执行失败\"}";
            }
        }
    }

    /**
     * 获取指定角色的工具定义列表（OpenAI function calling 格式）
     *
     * @param agentType 角色类型
     * @return 工具定义列表
     */
    public List<Map<String, Object>> getToolDefinitions(String agentType) {
        List<String> toolNames = ROLE_TOOLS.getOrDefault(agentType, List.of());
        List<Map<String, Object>> tools = new ArrayList<>();

        for (String name : toolNames) {
            Map<String, Object> tool = buildToolDefinition(name);
            if (tool != null) {
                tools.add(tool);
            }
        }
        return tools;
    }

    private Map<String, Object> buildToolDefinition(String name) {
        return switch (name) {
            case "search_coffee" -> buildFunction("search_coffee", "搜索咖啡列表，支持按分类筛选",
                Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "category", Map.of("type", "string", "description", "咖啡分类名称（可选）"),
                        "page", Map.of("type", "integer", "description", "页码，默认1"),
                        "size", Map.of("type", "integer", "description", "每页数量，默认10")
                    ),
                    "required", List.of()
                ));
            case "get_coffee_detail" -> buildFunction("get_coffee_detail", "获取某款咖啡的详细信息",
                Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "coffeeId", Map.of("type", "integer", "description", "咖啡ID")
                    ),
                    "required", List.of("coffeeId")
                ));
            case "get_categories" -> buildFunction("get_categories", "获取所有咖啡分类列表",
                Map.of("type", "object", "properties", Map.of(), "required", List.of()));
            case "add_to_cart" -> buildFunction("add_to_cart", "将咖啡添加到用户购物车（需要用户已登录）",
                Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "coffeeId", Map.of("type", "integer", "description", "咖啡ID"),
                        "quantity", Map.of("type", "integer", "description", "数量")
                    ),
                    "required", List.of("coffeeId", "quantity")
                ));
            case "get_cart" -> buildFunction("get_cart", "获取用户购物车内容（需要用户已登录）",
                Map.of("type", "object", "properties", Map.of(), "required", List.of()));
            case "create_order" -> buildFunction("create_order", "为用户创建订单（需要用户已登录）",
                Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "orderType", Map.of("type", "string", "description", "订单类型：dine_in（堂食）/ takeaway（外带）/ delivery（外卖）"),
                        "items", Map.of("type", "array", "description", "订单项列表",
                            "items", Map.of("type", "object",
                                "properties", Map.of(
                                    "coffeeId", Map.of("type", "integer", "description", "咖啡ID"),
                                    "quantity", Map.of("type", "integer", "description", "数量"),
                                    "price", Map.of("type", "number", "description", "单价")
                                ),
                                "required", List.of("coffeeId", "quantity", "price"))),
                        "remark", Map.of("type", "string", "description", "备注（可选）")
                    ),
                    "required", List.of("orderType", "items")
                ));
            case "get_order_list" -> buildFunction("get_order_list", "查询用户订单列表（需要用户已登录）",
                Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "status", Map.of("type", "string", "description", "订单状态筛选：pending/confirmed/preparing/ready/completed/cancelled（可选）"),
                        "page", Map.of("type", "integer", "description", "页码，默认1"),
                        "size", Map.of("type", "integer", "description", "每页数量，默认10")
                    ),
                    "required", List.of()
                ));
            case "get_order_detail" -> buildFunction("get_order_detail", "查询订单详情（需要用户已登录）",
                Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "orderId", Map.of("type", "string", "description", "订单号")
                    ),
                    "required", List.of("orderId")
                ));
            default -> null;
        };
    }

    /**
     * 构建 OpenAI function 工具定义
     */
    private Map<String, Object> buildFunction(String name, String description, Map<String, Object> parameters) {
        return Map.of(
            "type", "function",
            "function", Map.of(
                "name", name,
                "description", description,
                "parameters", parameters
            )
        );
    }
}

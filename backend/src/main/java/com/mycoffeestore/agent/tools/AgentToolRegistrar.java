package com.mycoffeestore.agent.tools;

import com.mycoffeestore.util.AgentToolExecutor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Agent 工具注册类
 * 统一管理所有工具定义，支持动态注册
 *
 * @author Backend Developer
 * @since 2026-03-07
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Schema(description = "Agent 工具注册类")
public class AgentToolRegistrar {

    private final AgentToolExecutor toolExecutor;

    /**
     * 获取咖啡顾问 Agent 的工具列表
     *
     * @return 工具列表
     */
    @Schema(description = "获取咖啡顾问 Agent 的工具列表")
    public List<Object> getCoffeeAdvisorTools() {
        log.debug("加载咖啡顾问工具列表");

        List<Object> tools = new ArrayList<>();

        // 搜索咖啡工具
        tools.add(createTool(
                "search_coffee",
                "搜索咖啡列表，支持按分类筛选",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "category", Map.of("type", "string", "description", "咖啡分类名称（可选）"),
                                "page", Map.of("type", "integer", "description", "页码，默认1"),
                                "size", Map.of("type", "integer", "description", "每页数量，默认10")
                        ),
                        "required", List.of()
                ),
                this::executeSearchCoffee
        ));

        // 获取咖啡详情工具
        tools.add(createTool(
                "get_coffee_detail",
                "获取某款咖啡的详细信息",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "coffeeId", Map.of("type", "integer", "description", "咖啡ID")
                        ),
                        "required", List.of("coffeeId")
                ),
                this::executeGetCoffeeDetail
        ));

        // 获取分类工具
        tools.add(createTool(
                "get_categories",
                "获取所有咖啡分类列表",
                Map.of(
                        "type", "object",
                        "properties", Map.of(),
                        "required", List.of()
                ),
                args -> executeTool("get_categories", args)
        ));

        log.info("咖啡顾问工具加载完成，共 {} 个工具", tools.size());
        return tools;
    }

    /**
     * 获取订单助手 Agent 的工具列表
     *
     * @return 工具列表
     */
    @Schema(description = "获取订单助手 Agent 的工具列表")
    public List<Object> getOrderAssistantTools() {
        log.debug("加载订单助手工具列表");

        List<Object> tools = new ArrayList<>();

        // 咖啡查询工具
        tools.add(createTool(
                "search_coffee",
                "搜索咖啡列表",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "category", Map.of("type", "string", "description", "咖啡分类名称（可选）"),
                                "page", Map.of("type", "integer", "description", "页码，默认1"),
                                "size", Map.of("type", "integer", "description", "每页数量，默认10")
                        ),
                        "required", List.of()
                ),
                this::executeSearchCoffee
        ));

        // 添加购物车工具
        tools.add(createTool(
                "add_to_cart",
                "将咖啡添加到用户购物车（需要用户已登录）",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "coffeeId", Map.of("type", "integer", "description", "咖啡ID"),
                                "quantity", Map.of("type", "integer", "description", "数量")
                        ),
                        "required", List.of("coffeeId", "quantity")
                ),
                args -> executeTool("add_to_cart", args)
        ));

        // 获取购物车工具
        tools.add(createTool(
                "get_cart",
                "获取用户购物车内容（需要用户已登录）",
                Map.of(
                        "type", "object",
                        "properties", Map.of(),
                        "required", List.of()
                ),
                args -> executeTool("get_cart", args)
        ));

        // 创建订单工具
        tools.add(createTool(
                "create_order",
                "为用户创建订单（需要用户已登录）",
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
                ),
                args -> executeTool("create_order", args)
        ));

        // 查询订单列表工具
        tools.add(createTool(
                "get_order_list",
                "查询用户订单列表（需要用户已登录）",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "status", Map.of("type", "string", "description", "订单状态筛选：pending/confirmed/preparing/ready/completed/cancelled（可选）"),
                                "page", Map.of("type", "integer", "description", "页码，默认1"),
                                "size", Map.of("type", "integer", "description", "每页数量，默认10")
                        ),
                        "required", List.of()
                ),
                args -> executeTool("get_order_list", args)
        ));

        // 查询订单详情工具
        tools.add(createTool(
                "get_order_detail",
                "查询订单详情（需要用户已登录）",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "orderId", Map.of("type", "string", "description", "订单号")
                        ),
                        "required", List.of("orderId")
                ),
                args -> executeTool("get_order_detail", args)
        ));

        log.info("订单助手工具加载完成，共 {} 个工具", tools.size());
        return tools;
    }

    /**
     * 获取客服 Agent 的工具列表
     *
     * @return 工具列表
     */
    @Schema(description = "获取客服 Agent 的工具列表")
    public List<Object> getCustomerServiceTools() {
        log.debug("加载客服工具列表");

        List<Object> tools = new ArrayList<>();

        // 查询订单详情工具
        tools.add(createTool(
                "get_order_detail",
                "查询订单详情（需要用户已登录）",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "orderId", Map.of("type", "string", "description", "订单号")
                        ),
                        "required", List.of("orderId")
                ),
                args -> executeTool("get_order_detail", args)
        ));

        // 查询订单列表工具
        tools.add(createTool(
                "get_order_list",
                "查询用户订单列表（需要用户已登录）",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "status", Map.of("type", "string", "description", "订单状态筛选：pending/confirmed/preparing/ready/completed/cancelled（可选）"),
                                "page", Map.of("type", "integer", "description", "页码，默认1"),
                                "size", Map.of("type", "integer", "description", "每页数量，默认10")
                        ),
                        "required", List.of()
                ),
                args -> executeTool("get_order_list", args)
        ));

        log.info("客服工具加载完成，共 {} 个工具", tools.size());
        return tools;
    }

    /**
     * 获取通用聊天 Agent 的工具列表
     *
     * @return 工具列表
     */
    @Schema(description = "获取通用聊天 Agent 的工具列表")
    public List<Object> getGeneralChatTools() {
        log.debug("加载通用聊天工具列表");

        // 通用聊天不需要工具
        return List.of();
    }

    /**
     * 获取路由 Agent 的工具列表
     * 路由 Agent 用于协调子 Agent 的执行，本身不需要工具
     *
     * @return 工具列表
     */
    @Schema(description = "获取路由 Agent 的工具列表")
    public List<Object> getRoutingTools() {
        log.debug("加载路由工具列表");

        // 路由 Agent 不需要工具，它通过服务调用协调其他 Agent
        return List.of();
    }

    /**
     * 执行工具调用
     *
     * @param toolName 工具名称
     * @param args     参数
     * @return 执行结果
     */
    @Schema(description = "执行工具调用")
    private String executeTool(String toolName, Map<String, Object> args) {
        try {
            String argsJson = convertArgsToJson(args);
            return toolExecutor.executeTool(toolName, argsJson, null);
        } catch (Exception e) {
            log.error("工具执行失败: {} - {}", toolName, e.getMessage(), e);
            return "{\"error\":\"工具执行失败: " + e.getMessage() + "\"}";
        }
    }

    /**
     * 执行搜索咖啡工具
     *
     * @param args 参数
     * @return 执行结果
     */
    @Schema(description = "执行搜索咖啡工具")
    private String executeSearchCoffee(Map<String, Object> args) {
        return executeTool("search_coffee", args);
    }

    /**
     * 执行获取咖啡详情工具
     *
     * @param args 参数
     * @return 执行结果
     */
    @Schema(description = "执行获取咖啡详情工具")
    private String executeGetCoffeeDetail(Map<String, Object> args) {
        return executeTool("get_coffee_detail", args);
    }

    /**
     * 转换参数为 JSON 字符串
     *
     * @param args 参数映射
     * @return JSON 字符串
     */
    @Schema(description = "转换参数为 JSON 字符串")
    private String convertArgsToJson(Map<String, Object> args) {
        if (args == null || args.isEmpty()) {
            return "{}";
        }

        try {
            StringBuilder json = new StringBuilder("{");
            boolean first = true;

            for (Map.Entry<String, Object> entry : args.entrySet()) {
                if (!first) {
                    json.append(",");
                }
                json.append("\"").append(entry.getKey()).append("\":");

                Object value = entry.getValue();
                if (value instanceof String) {
                    json.append("\"").append(value).append("\"");
                } else if (value instanceof Number || value instanceof Boolean) {
                    json.append(value);
                } else {
                    json.append("\"").append(value != null ? value.toString() : "").append("\"");
                }

                first = false;
            }

            json.append("}");
            return json.toString();
        } catch (Exception e) {
            log.error("参数转换失败: {}", e.getMessage());
            return "{}";
        }
    }

    /**
     * 创建工具定义
     *
     * @param name        工具名称
     * @param description 工具描述
     * @param parameters  参数定义
     * @param function    工具函数
     * @return 工具对象
     */
    @Schema(description = "创建工具定义")
    private Object createTool(String name,
                             String description,
                             Map<String, Object> parameters,
                             Function<Map<String, Object>, String> function) {
        // 返回一个包含工具信息的对象
        // Spring AI Alibaba 会自动将这些转换为工具定义
        return ToolDefinition.builder()
                .name(name)
                .description(description)
                .parameters(parameters)
                .function(function)
                .build();
    }

    /**
     * 工具定义内部类
     */
    @Schema(description = "工具定义内部类")
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    @lombok.Data
    @lombok.Builder
    public static class ToolDefinition {
        /**
         * 工具名称
         */
        @Schema(description = "工具名称")
        private String name;

        /**
         * 工具描述
         */
        @Schema(description = "工具描述")
        private String description;

        /**
         * 参数定义
         */
        @Schema(description = "参数定义")
        private Map<String, Object> parameters;

        /**
         * 工具函数
         */
        @Schema(description = "工具函数")
        private Function<Map<String, Object>, String> function;
    }
}

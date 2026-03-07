package com.mycoffeestore.service.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycoffeestore.dto.cart.CartAddDTO;
import com.mycoffeestore.dto.order.OrderCreateDTO;
import com.mycoffeestore.dto.order.OrderItemDTO;
import com.mycoffeestore.enums.OrderStatus;
import com.mycoffeestore.enums.OrderType;
import com.mycoffeestore.service.cart.CartService;
import com.mycoffeestore.service.coffee.CoffeeService;
import com.mycoffeestore.service.order.OrderService;
import com.mycoffeestore.util.AgentToolExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Agent 工具执行器测试
 * 测试工具定义、参数解析、结果返回等功能
 *
 * @author Backend Developer
 * @since 2026-03-07
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Agent 工具执行器测试")
public class AgentToolExecutorTest {

    @Mock
    private CoffeeService coffeeService;

    @Mock
    private CartService cartService;

    @Mock
    private OrderService orderService;

    private ObjectMapper objectMapper;
    private AgentToolExecutor toolExecutor;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        toolExecutor = new AgentToolExecutor(
                coffeeService,
                cartService,
                orderService,
                objectMapper
        );
    }

    // ==================== 工具定义测试 ====================

    @Test
    @DisplayName("工具定义 - 咖啡顾问工具列表")
    void testCoffeeAdvisorToolDefinitions() {
        // When
        List<Map<String, Object>> tools = toolExecutor.getToolDefinitions("coffee_advisor");

        // Then
        assertThat(tools).isNotNull();
        assertThat(tools).hasSize(3);

        List<String> toolNames = tools.stream()
                .map(t -> ((Map<String, Object>) t.get("function")).get("name").toString())
                .toList();

        assertThat(toolNames).containsExactlyInAnyOrder(
                "search_coffee",
                "get_coffee_detail",
                "get_categories"
        );
    }

    @Test
    @DisplayName("工具定义 - 订单助手工具列表")
    void testOrderAssistantToolDefinitions() {
        // When
        List<Map<String, Object>> tools = toolExecutor.getToolDefinitions("order_assistant");

        // Then
        assertThat(tools).isNotNull();
        assertThat(tools.size()).isGreaterThanOrEqualTo(6);

        List<String> toolNames = tools.stream()
                .map(t -> ((Map<String, Object>) t.get("function")).get("name").toString())
                .toList();

        assertThat(toolNames).contains(
                "search_coffee",
                "add_to_cart",
                "get_cart",
                "create_order",
                "get_order_list",
                "get_order_detail"
        );
    }

    @Test
    @DisplayName("工具定义 - 客服助手工具列表")
    void testCustomerServiceToolDefinitions() {
        // When
        List<Map<String, Object>> tools = toolExecutor.getToolDefinitions("customer_service");

        // Then
        assertThat(tools).isNotNull();
        assertThat(tools).hasSize(2);

        List<String> toolNames = tools.stream()
                .map(t -> ((Map<String, Object>) t.get("function")).get("name").toString())
                .toList();

        assertThat(toolNames).containsExactlyInAnyOrder(
                "get_order_detail",
                "get_order_list"
        );
    }

    @Test
    @DisplayName("工具定义 - 通用聊天无工具")
    void testGeneralChatToolDefinitions() {
        // When
        List<Map<String, Object>> tools = toolExecutor.getToolDefinitions("general_chat");

        // Then
        assertThat(tools).isNotNull();
        assertThat(tools).isEmpty();
    }

    // ==================== 咖啡相关工具测试 ====================

    @Test
    @DisplayName("工具执行 - search_coffee 搜索咖啡")
    void testExecuteSearchCoffee() throws Exception {
        // Given
        String toolName = "search_coffee";
        String argsJson = """
                {"category":"美式","page":1,"size":10}
                """;

        Map<String, Object> mockResult = Map.of(
                "list", List.of(
                        Map.of("id", 1, "name", "美式咖啡", "price", 25.0)
                ),
                "total", 1
        );

        when(coffeeService.list(eq("美式"), eq(1), eq(10)))
                .thenReturn(mockResult);

        // When
        String result = toolExecutor.executeTool(toolName, argsJson, null);

        // Then
        assertThat(result).isNotNull();
        Map<String, Object> resultMap = objectMapper.readValue(result, Map.class);
        assertThat(resultMap.get("list")).isNotNull();

        verify(coffeeService).list(eq("美式"), eq(1), eq(10));
    }

    @Test
    @DisplayName("工具执行 - search_coffee 默认参数")
    void testExecuteSearchCoffeeWithDefaults() throws Exception {
        // Given
        String toolName = "search_coffee";
        String argsJson = """
                {}
                """;

        when(coffeeService.list(isNull(), eq(1), eq(10)))
                .thenReturn(Map.of("list", List.of(), "total", 0));

        // When
        String result = toolExecutor.executeTool(toolName, argsJson, null);

        // Then
        assertThat(result).isNotNull();
        verify(coffeeService).list(isNull(), eq(1), eq(10));
    }

    @Test
    @DisplayName("工具执行 - get_coffee_detail 获取咖啡详情")
    void testExecuteGetCoffeeDetail() throws Exception {
        // Given
        String toolName = "get_coffee_detail";
        String argsJson = """
                {"coffeeId":1}
                """;

        Map<String, Object> mockDetail = Map.of(
                "id", 1,
                "name", "拿铁",
                "description", "经典意式拿铁",
                "price", 28.0
        );

        when(coffeeService.detail(1L)).thenReturn(mockDetail);

        // When
        String result = toolExecutor.executeTool(toolName, argsJson, null);

        // Then
        assertThat(result).isNotNull();
        Map<String, Object> resultMap = objectMapper.readValue(result, Map.class);
        assertThat(resultMap.get("name")).isEqualTo("拿铁");

        verify(coffeeService).detail(1L);
    }

    @Test
    @DisplayName("工具执行 - get_categories 获取分类列表")
    void testExecuteGetCategories() throws Exception {
        // Given
        String toolName = "get_categories";
        String argsJson = """
                {}
                """;

        List<String> mockCategories = List.of("美式", "拿铁", "卡布奇诺", "摩卡");
        when(coffeeService.categories()).thenReturn(mockCategories);

        // When
        String result = toolExecutor.executeTool(toolName, argsJson, null);

        // Then
        assertThat(result).isNotNull();
        Map<String, Object> resultMap = objectMapper.readValue(result, Map.class);
        assertThat(resultMap.get("categories")).isEqualTo(mockCategories);

        verify(coffeeService).categories();
    }

    // ==================== 购物车工具测试 ====================

    @Test
    @DisplayName("工具执行 - add_to_cart 添加到购物车（已登录）")
    void testExecuteAddToCartLoggedIn() throws Exception {
        // Given
        Long userId = 1L;
        String toolName = "add_to_cart";
        String argsJson = """
                {"coffeeId":1,"quantity":2}
                """;

        doNothing().when(cartService).add(eq(userId), any(CartAddDTO.class));

        // When
        String result = toolExecutor.executeTool(toolName, argsJson, userId);

        // Then
        assertThat(result).isNotNull();
        Map<String, Object> resultMap = objectMapper.readValue(result, Map.class);
        assertThat(resultMap.get("success")).isEqualTo(true);

        verify(cartService).add(eq(userId), any(CartAddDTO.class));
    }

    @Test
    @DisplayName("工具执行 - add_to_cart 未登录返回错误")
    void testExecuteAddToCartNotLoggedIn() throws Exception {
        // Given
        String toolName = "add_to_cart";
        String argsJson = """
                {"coffeeId":1,"quantity":2}
                """;

        // When
        String result = toolExecutor.executeTool(toolName, argsJson, null);

        // Then
        assertThat(result).isNotNull();
        Map<String, Object> resultMap = objectMapper.readValue(result, Map.class);
        assertThat(resultMap.get("error")).isNotNull();

        verify(cartService, never()).add(any(), any());
    }

    @Test
    @DisplayName("工具执行 - get_cart 获取购物车（已登录）")
    void testExecuteGetCartLoggedIn() throws Exception {
        // Given
        Long userId = 1L;
        String toolName = "get_cart";
        String argsJson = """
                {}
                """;

        Map<String, Object> mockCart = Map.of(
                "items", List.of(
                        Map.of("coffeeId", 1, "quantity", 2, "name", "拿铁")
                ),
                "total", 56.0
        );

        when(cartService.list(userId)).thenReturn(mockCart);

        // When
        String result = toolExecutor.executeTool(toolName, argsJson, userId);

        // Then
        assertThat(result).isNotNull();
        Map<String, Object> resultMap = objectMapper.readValue(result, Map.class);
        assertThat(resultMap.get("items")).isNotNull();

        verify(cartService).list(userId);
    }

    @Test
    @DisplayName("工具执行 - get_cart 未登录返回错误")
    void testExecuteGetCartNotLoggedIn() throws Exception {
        // Given
        String toolName = "get_cart";
        String argsJson = """
                {}
                """;

        // When
        String result = toolExecutor.executeTool(toolName, argsJson, null);

        // Then
        assertThat(result).isNotNull();
        Map<String, Object> resultMap = objectMapper.readValue(result, Map.class);
        assertThat(resultMap.get("error")).isNotNull();

        verify(cartService, never()).list(any());
    }

    // ==================== 订单工具测试 ====================

    @Test
    @DisplayName("工具执行 - create_order 创建订单（已登录）")
    void testExecuteCreateOrderLoggedIn() throws Exception {
        // Given
        Long userId = 1L;
        String toolName = "create_order";
        String argsJson = """
                {
                    "orderType": "dine_in",
                    "items": [
                        {"coffeeId": 1, "quantity": 2, "price": 28.0}
                    ],
                    "remark": "少糖"
                }
                """;

        when(orderService.create(eq(userId), any(OrderCreateDTO.class)))
                .thenReturn(Map.of("success", true, "orderId", "ORD123"));

        // When
        String result = toolExecutor.executeTool(toolName, argsJson, userId);

        // Then
        assertThat(result).isNotNull();
        Map<String, Object> resultMap = objectMapper.readValue(result, Map.class);
        assertThat(resultMap.get("success")).isEqualTo(true);

        verify(orderService).create(eq(userId), any(OrderCreateDTO.class));
    }

    @Test
    @DisplayName("工具执行 - create_order 未登录返回错误")
    void testExecuteCreateOrderNotLoggedIn() throws Exception {
        // Given
        String toolName = "create_order";
        String argsJson = """
                {}
                """;

        // When
        String result = toolExecutor.executeTool(toolName, argsJson, null);

        // Then
        assertThat(result).isNotNull();
        Map<String, Object> resultMap = objectMapper.readValue(result, Map.class);
        assertThat(resultMap.get("error")).isNotNull();

        verify(orderService, never()).create(any(), any());
    }

    @Test
    @DisplayName("工具执行 - create_order 订单项为空返回错误")
    void testExecuteCreateOrderEmptyItems() throws Exception {
        // Given
        Long userId = 1L;
        String toolName = "create_order";
        String argsJson = """
                {"orderType":"dine_in","items":[]}
                """;

        // When
        String result = toolExecutor.executeTool(toolName, argsJson, userId);

        // Then
        assertThat(result).isNotNull();
        Map<String, Object> resultMap = objectMapper.readValue(result, Map.class);
        assertThat(resultMap.get("error")).isNotNull();

        verify(orderService, never()).create(any(), any());
    }

    @Test
    @DisplayName("工具执行 - get_order_list 查询订单列表（已登录）")
    void testExecuteGetOrderListLoggedIn() throws Exception {
        // Given
        Long userId = 1L;
        String toolName = "get_order_list";
        String argsJson = """
                {"status":"pending","page":1,"size":10}
                """;

        Map<String, Object> mockOrders = Map.of(
                "list", List.of(
                        Map.of("orderId", "ORD123", "status", "pending")
                ),
                "total", 1
        );

        when(orderService.list(eq(userId), eq("user"), eq(OrderStatus.PENDING), eq(1), eq(10)))
                .thenReturn(mockOrders);

        // When
        String result = toolExecutor.executeTool(toolName, argsJson, userId);

        // Then
        assertThat(result).isNotNull();
        Map<String, Object> resultMap = objectMapper.readValue(result, Map.class);
        assertThat(resultMap.get("list")).isNotNull();

        verify(orderService).list(eq(userId), eq("user"), eq(OrderStatus.PENDING), eq(1), eq(10));
    }

    @Test
    @DisplayName("工具执行 - get_order_list 默认参数")
    void testExecuteGetOrderListWithDefaults() throws Exception {
        // Given
        Long userId = 1L;
        String toolName = "get_order_list";
        String argsJson = """
                {}
                """;

        when(orderService.list(eq(userId), eq("user"), isNull(), eq(1), eq(10)))
                .thenReturn(Map.of("list", List.of(), "total", 0));

        // When
        String result = toolExecutor.executeTool(toolName, argsJson, userId);

        // Then
        assertThat(result).isNotNull();
        verify(orderService).list(eq(userId), eq("user"), isNull(), eq(1), eq(10));
    }

    @Test
    @DisplayName("工具执行 - get_order_detail 查询订单详情（已登录）")
    void testExecuteGetOrderDetailLoggedIn() throws Exception {
        // Given
        Long userId = 1L;
        String toolName = "get_order_detail";
        String argsJson = """
                {"orderId":"ORD123"}
                """;

        Map<String, Object> mockDetail = Map.of(
                "orderId", "ORD123",
                "status", "confirmed",
                "items", List.of()
        );

        when(orderService.detail(userId, "ORD123")).thenReturn(mockDetail);

        // When
        String result = toolExecutor.executeTool(toolName, argsJson, userId);

        // Then
        assertThat(result).isNotNull();
        Map<String, Object> resultMap = objectMapper.readValue(result, Map.class);
        assertThat(resultMap.get("orderId")).isEqualTo("ORD123");

        verify(orderService).detail(userId, "ORD123");
    }

    @Test
    @DisplayName("工具执行 - get_order_detail 未登录返回错误")
    void testExecuteGetOrderDetailNotLoggedIn() throws Exception {
        // Given
        String toolName = "get_order_detail";
        String argsJson = """
                {"orderId":"ORD123"}
                """;

        // When
        String result = toolExecutor.executeTool(toolName, argsJson, null);

        // Then
        assertThat(result).isNotNull();
        Map<String, Object> resultMap = objectMapper.readValue(result, Map.class);
        assertThat(resultMap.get("error")).isNotNull();

        verify(orderService, never()).detail(any(), any());
    }

    // ==================== 错误处理测试 ====================

    @Test
    @DisplayName("错误处理 - 未知工具名称")
    void testExecuteUnknownTool() throws Exception {
        // Given
        String toolName = "unknown_tool";
        String argsJson = """
                {}
                """;

        // When
        String result = toolExecutor.executeTool(toolName, argsJson, null);

        // Then
        assertThat(result).isNotNull();
        Map<String, Object> resultMap = objectMapper.readValue(result, Map.class);
        assertThat(resultMap.get("error")).isNotNull();
        assertThat(resultMap.get("error").toString()).contains("未知工具");
    }

    @Test
    @DisplayName("错误处理 - 无效的 JSON 参数")
    void testExecuteInvalidJson() throws Exception {
        // Given
        String toolName = "search_coffee";
        String argsJson = "invalid json";

        // When
        String result = toolExecutor.executeTool(toolName, argsJson, null);

        // Then
        assertThat(result).isNotNull();
        Map<String, Object> resultMap = objectMapper.readValue(result, Map.class);
        assertThat(resultMap.get("error")).isNotNull();
    }

    @Test
    @DisplayName("错误处理 - 工具执行异常")
    void testExecuteToolException() throws Exception {
        // Given
        String toolName = "get_coffee_detail";
        String argsJson = """
                {"coffeeId":999}
                """;

        when(coffeeService.detail(999L))
                .thenThrow(new RuntimeException("咖啡不存在"));

        // When
        String result = toolExecutor.executeTool(toolName, argsJson, null);

        // Then
        assertThat(result).isNotNull();
        Map<String, Object> resultMap = objectMapper.readValue(result, Map.class);
        assertThat(resultMap.get("error")).isNotNull();
    }

    // ==================== 工具参数验证测试 ====================

    @Test
    @DisplayName("工具参数 - 验证 search_coffee 参数结构")
    void testSearchCoffeeParametersStructure() {
        // When
        List<Map<String, Object>> tools = toolExecutor.getToolDefinitions("coffee_advisor");
        Map<String, Object> searchTool = tools.stream()
                .filter(t -> ((Map<String, Object>) t.get("function")).get("name").equals("search_coffee"))
                .findFirst()
                .orElseThrow();

        Map<String, Object> function = (Map<String, Object>) searchTool.get("function");
        Map<String, Object> parameters = (Map<String, Object>) function.get("parameters");

        // Then
        assertThat(parameters.get("type")).isEqualTo("object");
        Map<String, Object> props = (Map<String, Object>) parameters.get("properties");

        assertThat(props).containsKeys("category", "page", "size");

        Map<String, Object> categoryParam = (Map<String, Object>) props.get("category");
        assertThat(categoryParam.get("type")).isEqualTo("string");
        assertThat(categoryParam.get("description")).isNotNull();

        assertThat(parameters.get("required")).isEqualTo(List.of());
    }

    @Test
    @DisplayName("工具参数 - 验证 create_order 参数结构")
    void testCreateOrderParametersStructure() {
        // When
        List<Map<String, Object>> tools = toolExecutor.getToolDefinitions("order_assistant");
        Map<String, Object> createOrderTool = tools.stream()
                .filter(t -> ((Map<String, Object>) t.get("function")).get("name").equals("create_order"))
                .findFirst()
                .orElseThrow();

        Map<String, Object> function = (Map<String, Object>) createOrderTool.get("function");
        Map<String, Object> parameters = (Map<String, Object>) function.get("parameters");

        // Then
        assertThat(parameters.get("type")).isEqualTo("object");
        Map<String, Object> props = (Map<String, Object>) parameters.get("properties");

        assertThat(props).containsKeys("orderType", "items", "remark");

        Map<String, Object> itemsParam = (Map<String, Object>) props.get("items");
        assertThat(itemsParam.get("type")).isEqualTo("array");

        List<String> required = (List<String>) parameters.get("required");
        assertThat(required).containsExactlyInAnyOrder("orderType", "items");
    }
}

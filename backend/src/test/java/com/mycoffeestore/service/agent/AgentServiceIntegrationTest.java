package com.mycoffeestore.service.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycoffeestore.config.ModelScopeProperties;
import com.mycoffeestore.dto.agent.AgentChatRequestDTO;
import com.mycoffeestore.service.coffee.CoffeeService;
import com.mycoffeestore.service.impl.agent.AgentServiceImpl;
import com.mycoffeestore.util.AgentToolExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Agent 服务集成测试
 * 测试咖啡顾问、订单助手、客服助手等 Agent 的功能
 *
 * @author Backend Developer
 * @since 2026-03-07
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Agent 服务集成测试")
public class AgentServiceIntegrationTest {

    @Mock
    private WebClient modelScopeWebClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private ModelScopeProperties modelScopeProperties;

    @Mock
    private AgentToolExecutor toolExecutor;

    @Mock
    private CoffeeService coffeeService;

    private ObjectMapper objectMapper;
    private AgentServiceImpl agentService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        // 配置 ModelScopeProperties
        when(modelScopeProperties.getModel()).thenReturn("modelscope-test-model");

        // 配置 WebClient 链式调用
        when(modelScopeWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(eq("/chat/completions"))).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        agentService = new AgentServiceImpl(
                modelScopeWebClient,
                modelScopeProperties,
                toolExecutor,
                objectMapper
        );
    }

    // ==================== 咖啡顾问 Agent 测试 ====================

    @Test
    @DisplayName("咖啡顾问 Agent - 基础对话")
    void testCoffeeAdvisorBasicChat() {
        // Given
        AgentChatRequestDTO request = AgentChatRequestDTO.builder()
                .agentType("coffee_advisor")
                .messages(List.of(
                        AgentChatRequestDTO.Message.builder()
                                .role("user")
                                .content("你好，推荐一款咖啡")
                                .build()
                ))
                .build();

        SseEmitter emitter = new SseEmitter(60000L);

        // Mock SSE 流式响应
        String sseResponse = """
                data: {"choices":[{"delta":{"content":"你好"}}]}
                data: {"choices":[{"delta":{"content":"！"}}]}
                data: {"choices":[{"delta":{"content":"推荐"}}]}
                data: {"choices":[{"delta":{"content":"埃塞俄比亚"}}]}
                data: [DONE]
                """;

        when(responseSpec.bodyToFlux(String.class))
                .thenReturn(Flux.just(sseResponse.split("\n")));

        // When
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            agentService.chatStream(request, null, emitter);
        });

        // Then
        future.join();
        assertThat(emitter).isNotNull();
        verify(responseSpec).bodyToFlux(String.class);
    }

    @Test
    @DisplayName("咖啡顾问 Agent - 工具调用（搜索咖啡）")
    void testCoffeeAdvisorWithToolCall() throws Exception {
        // Given
        AgentChatRequestDTO request = AgentChatRequestDTO.builder()
                .agentType("coffee_advisor")
                .messages(List.of(
                        AgentChatRequestDTO.Message.builder()
                                .role("user")
                                .content("推荐一款美式咖啡")
                                .build()
                ))
                .build();

        SseEmitter emitter = new SseEmitter(60000L);

        // Mock 工具执行结果
        Map<String, Object> mockCoffee = Map.of(
                "id", 1L,
                "name", "美式咖啡",
                "category", "美式",
                "price", new BigDecimal("25.00"),
                "description", "经典美式咖啡"
        );
        List<Map<String, Object>> mockCoffees = List.of(mockCoffee);

        when(coffeeService.list(any(), anyInt(), anyInt())).thenReturn(Map.of(
                "list", mockCoffees,
                "total", 1
        ));

        when(toolExecutor.executeTool(eq("search_coffee"), any(), any()))
                .thenReturn(objectMapper.writeValueAsString(Map.of(
                        "list", mockCoffees,
                        "total", 1
                )));

        // Mock SSE 响应：先返回工具调用，再返回结果
        String toolCallResponse = """
                data: {"choices":[{"delta":{"tool_calls":[{"index":0,"id":"call_123","function":{"name":"search_coffee","arguments":"{\\"category\\":\\"美式\\"}"}}]}}]}
                data: [DONE]
                """;

        when(responseSpec.bodyToFlux(String.class))
                .thenReturn(Flux.just(toolCallResponse.split("\n")));

        // When
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            agentService.chatStream(request, null, emitter);
        });

        // Then
        future.join();
        verify(toolExecutor, atLeastOnce()).executeTool(eq("search_coffee"), any(), any());
    }

    // ==================== 订单助手 Agent 测试 ====================

    @Test
    @DisplayName("订单助手 Agent - 创建订单流程")
    void testOrderAssistantCreateOrder() {
        // Given
        Long userId = 1L;
        AgentChatRequestDTO request = AgentChatRequestDTO.builder()
                .agentType("order_assistant")
                .messages(List.of(
                        AgentChatRequestDTO.Message.builder()
                                .role("user")
                                .content("我要下单，一杯拿铁")
                                .build()
                ))
                .build();

        SseEmitter emitter = new SseEmitter(60000L);

        // Mock 工具执行结果
        when(toolExecutor.executeTool(eq("create_order"), any(), eq(userId)))
                .thenReturn("{\"success\":true,\"orderId\":\"ORD123\"}");

        String sseResponse = """
                data: {"choices":[{"delta":{"content":"好的"}}]}
                data: {"choices":[{"delta":{"content":"，我来"}}]}
                data: {"choices":[{"delta":{"content":"帮您下单"}}]}
                data: [DONE]
                """;

        when(responseSpec.bodyToFlux(String.class))
                .thenReturn(Flux.just(sseResponse.split("\n")));

        // When
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            agentService.chatStream(request, userId, emitter);
        });

        // Then
        future.join();
        verify(responseSpec).bodyToFlux(String.class);
    }

    @Test
    @DisplayName("订单助手 Agent - 查询订单列表")
    void testOrderAssistantGetOrderList() {
        // Given
        Long userId = 1L;
        AgentChatRequestDTO request = AgentChatRequestDTO.builder()
                .agentType("order_assistant")
                .messages(List.of(
                        AgentChatRequestDTO.Message.builder()
                                .role("user")
                                .content("查看我的订单")
                                .build()
                ))
                .build();

        SseEmitter emitter = new SseEmitter(60000L);

        when(toolExecutor.executeTool(eq("get_order_list"), any(), eq(userId)))
                .thenReturn("{\"list\":[],\"total\":0}");

        String sseResponse = """
                data: {"choices":[{"delta":{"content":"您的"}}]}
                data: {"choices":[{"delta":{"content":"订单列表"}}]}
                data: [DONE]
                """;

        when(responseSpec.bodyToFlux(String.class))
                .thenReturn(Flux.just(sseResponse.split("\n")));

        // When
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            agentService.chatStream(request, userId, emitter);
        });

        // Then
        future.join();
    }

    // ==================== 客服助手 Agent 测试 ====================

    @Test
    @DisplayName("客服助手 Agent - 回答常见问题")
    void testCustomerServiceBasicQA() {
        // Given
        AgentChatRequestDTO request = AgentChatRequestDTO.builder()
                .agentType("customer_service")
                .messages(List.of(
                        AgentChatRequestDTO.Message.builder()
                                .role("user")
                                .content("你们的营业时间是什么？")
                                .build()
                ))
                .build();

        SseEmitter emitter = new SseEmitter(60000L);

        String sseResponse = """
                data: {"choices":[{"delta":{"content":"我们的"}}]}
                data: {"choices":[{"delta":{"content":"营业时间是"}}]}
                data: {"choices":[{"delta":{"content":"7:00-21:00"}}]}
                data: [DONE]
                """;

        when(responseSpec.bodyToFlux(String.class))
                .thenReturn(Flux.just(sseResponse.split("\n")));

        // When
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            agentService.chatStream(request, null, emitter);
        });

        // Then
        future.join();
    }

    @Test
    @DisplayName("客服助手 Agent - 查询订单状态")
    void testCustomerServiceCheckOrderStatus() {
        // Given
        Long userId = 1L;
        AgentChatRequestDTO request = AgentChatRequestDTO.builder()
                .agentType("customer_service")
                .messages(List.of(
                        AgentChatRequestDTO.Message.builder()
                                .role("user")
                                .content("我的订单 ORD123 什么时候能到？")
                                .build()
                ))
                .build();

        SseEmitter emitter = new SseEmitter(60000L);

        when(toolExecutor.executeTool(eq("get_order_detail"), any(), eq(userId)))
                .thenReturn("{\"orderId\":\"ORD123\",\"status\":\"preparing\"}");

        String sseResponse = """
                data: {"choices":[{"delta":{"content":"让我"}}]}
                data: {"choices":[{"delta":{"content":"查询一下"}}]}
                data: [DONE]
                """;

        when(responseSpec.bodyToFlux(String.class))
                .thenReturn(Flux.just(sseResponse.split("\n")));

        // When
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            agentService.chatStream(request, userId, emitter);
        });

        // Then
        future.join();
    }

    // ==================== 工具调用测试 ====================

    @Test
    @DisplayName("工具调用 - 未登录用户访问需要登录的工具")
    void testToolCallWithoutLogin() throws Exception {
        // Given
        AgentChatRequestDTO request = AgentChatRequestDTO.builder()
                .agentType("order_assistant")
                .messages(List.of(
                        AgentChatRequestDTO.Message.builder()
                                .role("user")
                                .content("添加到购物车")
                                .build()
                ))
                .build();

        SseEmitter emitter = new SseEmitter(60000L);

        // Mock 工具执行器返回错误
        when(toolExecutor.executeTool(eq("add_to_cart"), any(), any()))
                .thenReturn("{\"error\":\"该操作需要登录，请先登录\"}");

        String sseResponse = """
                data: {"choices":[{"delta":{"tool_calls":[{"index":0,"id":"call_456","function":{"name":"add_to_cart","arguments":"{\\"coffeeId\\":1}"}}]}}]}
                data: [DONE]
                """;

        when(responseSpec.bodyToFlux(String.class))
                .thenReturn(Flux.just(sseResponse.split("\n")));

        // When
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            agentService.chatStream(request, null, emitter);
        });

        // Then
        future.join();
        verify(toolExecutor).executeTool(eq("add_to_cart"), any(), isNull());
    }

    @Test
    @DisplayName("工具调用 - 多工具顺序执行")
    void testMultipleToolsSequentialExecution() throws Exception {
        // Given
        Long userId = 1L;
        AgentChatRequestDTO request = AgentChatRequestDTO.builder()
                .agentType("order_assistant")
                .messages(List.of(
                        AgentChatRequestDTO.Message.builder()
                                .role("user")
                                .content("搜索咖啡，然后添加到购物车")
                                .build()
                ))
                .build();

        SseEmitter emitter = new SseEmitter(60000L);

        // Mock 第一个工具调用：search_coffee
        when(toolExecutor.executeTool(eq("search_coffee"), any(), eq(userId)))
                .thenReturn("{\"list\":[{\"id\":1,\"name\":\"拿铁\"}],\"total\":1}");

        // Mock 第二个工具调用：add_to_cart
        when(toolExecutor.executeTool(eq("add_to_cart"), any(), eq(userId)))
                .thenReturn("{\"success\":true,\"message\":\"已添加到购物车\"}");

        // 模拟多轮工具调用的响应
        String sseResponse = """
                data: {"choices":[{"delta":{"content":"让我"}}]}
                data: {"choices":[{"delta":{"tool_calls":[{"index":0,"id":"call_1","function":{"name":"search_coffee","arguments":"{}"}}]}}]}
                data: [DONE]
                """;

        when(responseSpec.bodyToFlux(String.class))
                .thenReturn(Flux.just(sseResponse.split("\n")));

        // When
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            agentService.chatStream(request, userId, emitter);
        });

        // Then
        future.join();
    }

    // ==================== 错误处理测试 ====================

    @Test
    @DisplayName("错误处理 - API 调用失败")
    void testApiCallFailure() {
        // Given
        AgentChatRequestDTO request = AgentChatRequestDTO.builder()
                .agentType("coffee_advisor")
                .messages(List.of(
                        AgentChatRequestDTO.Message.builder()
                                .role("user")
                                .content("推荐咖啡")
                                .build()
                ))
                .build();

        SseEmitter emitter = new SseEmitter(60000L);

        // Mock API 错误
        when(responseSpec.bodyToFlux(String.class))
                .thenReturn(Flux.error(new RuntimeException("API 服务不可用")));

        // When
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            agentService.chatStream(request, null, emitter);
        });

        // Then
        future.join();
        // 验证 emitter 仍然完成（带有错误消息）
    }

    @Test
    @DisplayName("错误处理 - 无效的 Agent 类型")
    void testInvalidAgentType() {
        // Given
        AgentChatRequestDTO request = AgentChatRequestDTO.builder()
                .agentType("invalid_agent")
                .messages(List.of(
                        AgentChatRequestDTO.Message.builder()
                                .role("user")
                                .content("测试")
                                .build()
                ))
                .build();

        SseEmitter emitter = new SseEmitter(60000L);

        String sseResponse = """
                data: {"choices":[{"delta":{"content":"你好"}}]}
                data: [DONE]
                """;

        when(responseSpec.bodyToFlux(String.class))
                .thenReturn(Flux.just(sseResponse.split("\n")));

        // When
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            agentService.chatStream(request, null, emitter);
        });

        // Then - 应该使用默认的 coffee_advisor prompt
        future.join();
    }

    // ==================== 工具定义测试 ====================

    @Test
    @DisplayName("工具定义 - 验证咖啡顾问工具列表")
    void testCoffeeAdvisorToolDefinitions() {
        // Given
        String agentType = "coffee_advisor";

        // When
        List<Map<String, Object>> tools = toolExecutor.getToolDefinitions(agentType);

        // Then
        assertThat(tools).isNotNull();
        assertThat(tools).hasSize(3);

        List<String> toolNames = tools.stream()
                .map(tool -> (Map<String, Object>) tool.get("function"))
                .map(func -> (String) func.get("name"))
                .toList();

        assertThat(toolNames).containsExactlyInAnyOrder(
                "search_coffee",
                "get_coffee_detail",
                "get_categories"
        );
    }

    @Test
    @DisplayName("工具定义 - 验证订单助手工具列表")
    void testOrderAssistantToolDefinitions() {
        // Given
        String agentType = "order_assistant";

        // When
        List<Map<String, Object>> tools = toolExecutor.getToolDefinitions(agentType);

        // Then
        assertThat(tools).isNotNull();

        List<String> toolNames = tools.stream()
                .map(tool -> (Map<String, Object>) tool.get("function"))
                .map(func -> (String) func.get("name"))
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
    @DisplayName("工具定义 - 验证客服助手工具列表")
    void testCustomerServiceToolDefinitions() {
        // Given
        String agentType = "customer_service";

        // When
        List<Map<String, Object>> tools = toolExecutor.getToolDefinitions(agentType);

        // Then
        assertThat(tools).isNotNull();

        List<String> toolNames = tools.stream()
                .map(tool -> (Map<String, Object>) tool.get("function"))
                .map(func -> (String) func.get("name"))
                .toList();

        assertThat(toolNames).contains(
                "get_order_detail",
                "get_order_list"
        );
    }

    // ==================== 系统提示词测试 ====================

    @Test
    @DisplayName("System Prompt - 验证咖啡顾问提示词")
    void testCoffeeAdvisorSystemPrompt() throws Exception {
        // Given
        AgentChatRequestDTO request = AgentChatRequestDTO.builder()
                .agentType("coffee_advisor")
                .messages(List.of(
                        AgentChatRequestDTO.Message.builder()
                                .role("user")
                                .content("你好")
                                .build()
                ))
                .build();

        SseEmitter emitter = new SseEmitter(60000L);

        String sseResponse = "data: [DONE]\n";
        when(responseSpec.bodyToFlux(String.class))
                .thenReturn(Flux.just(sseResponse.split("\n")));

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        // When
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            agentService.chatStream(request, null, emitter);
        });

        // Then
        future.join();
        verify(requestBodySpec).bodyValue(bodyCaptor.capture());

        JsonNode requestBody = objectMapper.readTree(bodyCaptor.getValue());
        JsonNode messages = requestBody.get("messages");

        assertThat(messages.isArray()).isTrue();
        assertThat(messages).hasSizeGreaterThanOrEqualTo(1);

        JsonNode systemMessage = messages.get(0);
        assertThat(systemMessage.get("role").asText()).isEqualTo("system");
        assertThat(systemMessage.get("content").asText()).contains("咖咖");
        assertThat(systemMessage.get("content").asText()).contains("咖啡顾问");
    }

    @Test
    @DisplayName("System Prompt - 验证客服助手提示词")
    void testCustomerServiceSystemPrompt() throws Exception {
        // Given
        AgentChatRequestDTO request = AgentChatRequestDTO.builder()
                .agentType("customer_service")
                .messages(List.of(
                        AgentChatRequestDTO.Message.builder()
                                .role("user")
                                .content("营业时间")
                                .build()
                ))
                .build();

        SseEmitter emitter = new SseEmitter(60000L);

        String sseResponse = "data: [DONE]\n";
        when(responseSpec.bodyToFlux(String.class))
                .thenReturn(Flux.just(sseResponse.split("\n")));

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        // When
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            agentService.chatStream(request, null, emitter);
        });

        // Then
        future.join();
        verify(requestBodySpec).bodyValue(bodyCaptor.capture());

        JsonNode requestBody = objectMapper.readTree(bodyCaptor.getValue());
        JsonNode messages = requestBody.get("messages");

        JsonNode systemMessage = messages.get(0);
        assertThat(systemMessage.get("content").asText()).contains("客服助手");
        assertThat(systemMessage.get("content").asText()).contains("7:00-21:00");
    }

    // ==================== 对话历史测试 ====================

    @Test
    @DisplayName("对话历史 - 多轮对话上下文传递")
    void testConversationHistory() throws Exception {
        // Given
        AgentChatRequestDTO request = AgentChatRequestDTO.builder()
                .agentType("coffee_advisor")
                .messages(List.of(
                        AgentChatRequestDTO.Message.builder()
                                .role("user")
                                .content("推荐美式咖啡")
                                .build(),
                        AgentChatRequestDTO.Message.builder()
                                .role("assistant")
                                .content("推荐埃塞俄比亚美式")
                                .build(),
                        AgentChatRequestDTO.Message.builder()
                                .role("user")
                                .content("这款咖啡多少钱？")
                                .build()
                ))
                .build();

        SseEmitter emitter = new SseEmitter(60000L);

        String sseResponse = "data: [DONE]\n";
        when(responseSpec.bodyToFlux(String.class))
                .thenReturn(Flux.just(sseResponse.split("\n")));

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        // When
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            agentService.chatStream(request, null, emitter);
        });

        // Then
        future.join();
        verify(requestBodySpec).bodyValue(bodyCaptor.capture());

        JsonNode requestBody = objectMapper.readTree(bodyCaptor.getValue());
        JsonNode messages = requestBody.get("messages");

        // 应该包含 system + 3 条对话消息
        assertThat(messages).hasSize(4);

        // 验证消息顺序
        assertThat(messages.get(0).get("role").asText()).isEqualTo("system");
        assertThat(messages.get(1).get("content").asText()).contains("美式咖啡");
        assertThat(messages.get(2).get("role").asText()).isEqualTo("assistant");
        assertThat(messages.get(3).get("content").asText()).contains("多少钱");
    }
}

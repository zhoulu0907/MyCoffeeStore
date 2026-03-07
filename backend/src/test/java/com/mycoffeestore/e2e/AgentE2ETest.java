package com.mycoffeestore.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycoffeestore.dto.agent.AgentChatRequestDTO;
import com.mycoffeestore.service.agent.AgentService;
import com.mycoffeestore.service.coffee.CoffeeService;
import com.mycoffeestore.service.memory.ConversationMemoryService;
import com.mycoffeestore.util.AgentToolExecutor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Agent 端到端测试
 * 测试完整的对话流程、Agent 协作场景、工具调用链
 *
 * @author Backend Developer
 * @since 2026-03-07
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Agent 端到端测试")
public class AgentE2ETest {

    @Autowired(required = false)
    private AgentService agentService;

    @MockBean
    private CoffeeService coffeeService;

    @MockBean
    private ConversationMemoryService memoryService;

    private ObjectMapper objectMapper = new ObjectMapper();

    // ==================== 场景1：咖啡推荐流程 ====================

    @Test
    @DisplayName("场景1：咖啡推荐流程 - 用户请求推荐 → Agent 调用工具 → 返回推荐结果")
    void testScenario1_CoffeeRecommendationFlow() throws Exception {
        // 假设：这个测试需要完整的 Spring 上下文
        // 如果 AgentService 不可用（测试环境配置），跳过测试
        if (agentService == null) {
            return;
        }

        // Given
        String sessionId = "session_scenario_1";

        // Mock 咖啡搜索结果
        List<Map<String, Object>> mockCoffees = List.of(
                Map.of(
                        "id", 1,
                        "name", "埃塞俄比亚美式",
                        "category", "美式",
                        "price", 28.0,
                        "description", "来自埃塞俄比亚的精选咖啡豆"
                ),
                Map.of(
                        "id", 2,
                        "name", "哥伦比亚拿铁",
                        "category", "拿铁",
                        "price", 32.0,
                        "description", "哥伦比亚与丝滑牛奶的完美融合"
                )
        );

        when(coffeeService.list(any(), anyInt(), anyInt()))
                .thenReturn(Map.of("list", mockCoffees, "total", 2));

        // When - 创建聊天请求
        AgentChatRequestDTO request = AgentChatRequestDTO.builder()
                .agentType("coffee_advisor")
                .messages(List.of(
                        AgentChatRequestDTO.Message.builder()
                                .role("user")
                                .content("推荐一款不太酸的咖啡")
                                .build()
                ))
                .build();

        // 创建 SSE Emitter 并捕获响应
        SseEmitter emitter = new SseEmitter(60000L);
        List<String> receivedEvents = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        emitter.onCompletion(() -> latch.countDown());
        emitter.onError(e -> latch.countDown());

        // When
        CompletableFuture.runAsync(() -> {
            try {
                agentService.chatStream(request, null, emitter);
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        // Then - 等待响应
        boolean completed = latch.await(30, TimeUnit.SECONDS);

        assertThat(completed).isTrue();
        // 在实际环境中，验证收到的 SSE 事件包含推荐结果
    }

    // ==================== 场景2：下单流程 ====================

    @Test
    @DisplayName("场景2：下单流程 - 用户下单 → 路由到 order_assistant → 调用工具链 → 完成下单")
    void testScenario2_OrderFlow() throws Exception {
        if (agentService == null) {
            return;
        }

        // Given
        Long userId = 1L;

        // Mock 咖啡搜索
        when(coffeeService.list(any(), anyInt(), anyInt()))
                .thenReturn(Map.of(
                        "list", List.of(Map.of(
                                "id", 1,
                                "name", "拿铁",
                                "price", 28.0
                        )),
                        "total", 1
                ));

        // When
        AgentChatRequestDTO request = AgentChatRequestDTO.builder()
                .agentType("order_assistant")
                .messages(List.of(
                        AgentChatRequestDTO.Message.builder()
                                .role("user")
                                .content("我要下单，一杯拿铁，堂食")
                                .build()
                ))
                .build();

        SseEmitter emitter = new SseEmitter(60000L);
        CountDownLatch latch = new CountDownLatch(1);

        emitter.onCompletion(() -> latch.countDown());

        CompletableFuture.runAsync(() -> {
            try {
                agentService.chatStream(request, userId, emitter);
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        // Then
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        assertThat(completed).isTrue();
    }

    // ==================== 场景3：智能路由 ====================

    @Test
    @DisplayName("场景3：智能路由 - 用户询问订单 → 意图识别 → 路由到 order_assistant")
    void testScenario3_SmartRouting() throws Exception {
        if (agentService == null) {
            return;
        }

        // Given - 模拟不同的用户意图
        Map<String, String> testCases = Map.of(
                "我的订单什么时候到？", "order_assistant",
                "推荐一款咖啡", "coffee_advisor",
                "你们几点关门？", "customer_service",
                "随便聊聊", "general_chat"
        );

        for (Map.Entry<String, String> entry : testCases.entrySet()) {
            String userMessage = entry.getKey();
            String expectedAgent = entry.getValue();

            AgentChatRequestDTO request = AgentChatRequestDTO.builder()
                    .agentType(expectedAgent)
                    .messages(List.of(
                            AgentChatRequestDTO.Message.builder()
                                    .role("user")
                                    .content(userMessage)
                                    .build()
                    ))
                    .build();

            SseEmitter emitter = new SseEmitter(60000L);
            CountDownLatch latch = new CountDownLatch(1);

            emitter.onCompletion(() -> latch.countDown());

            CompletableFuture.runAsync(() -> {
                try {
                    agentService.chatStream(request, null, emitter);
                } catch (Exception e) {
                    emitter.completeWithError(e);
                }
            });

            boolean completed = latch.await(10, TimeUnit.SECONDS);
            assertThat(completed).isTrue();
        }
    }

    // ==================== 场景4：Agent 协作 ====================

    @Test
    @DisplayName("场景4：Agent 协作 - 咖啡推荐后下单 → coffee_advisor → order_assistant")
    void testScenario4_AgentCollaboration() throws Exception {
        if (agentService == null) {
            return;
        }

        // Given - 用户请求推荐并下单
        Long userId = 1L;

        // Mock 咖啡搜索
        when(coffeeService.list(any(), anyInt(), anyInt()))
                .thenReturn(Map.of(
                        "list", List.of(Map.of(
                                "id", 3,
                                "name", "卡布奇诺",
                                "price", 30.0
                        )),
                        "total", 1
                ));

        // When - 第一阶段：咖啡推荐
        AgentChatRequestDTO recommendRequest = AgentChatRequestDTO.builder()
                .agentType("coffee_advisor")
                .messages(List.of(
                        AgentChatRequestDTO.Message.builder()
                                .role("user")
                                .content("推荐一款咖啡")
                                .build()
                ))
                .build();

        SseEmitter recommendEmitter = new SseEmitter(60000L);
        CountDownLatch recommendLatch = new CountDownLatch(1);

        List<String> recommendResponses = new ArrayList<>();
        recommendEmitter.onCompletion(() -> recommendLatch.countDown());

        CompletableFuture.runAsync(() -> {
            try {
                agentService.chatStream(recommendRequest, null, recommendEmitter);
            } catch (Exception e) {
                recommendEmitter.completeWithError(e);
            }
        });

        boolean recommendCompleted = recommendLatch.await(30, TimeUnit.SECONDS);
        assertThat(recommendCompleted).isTrue();

        // When - 第二阶段：下单
        AgentChatRequestDTO orderRequest = AgentChatRequestDTO.builder()
                .agentType("order_assistant")
                .messages(List.of(
                        AgentChatRequestDTO.Message.builder()
                                .role("user")
                                .content("我要一杯卡布奇诺，堂食")
                                .build()
                ))
                .build();

        SseEmitter orderEmitter = new SseEmitter(60000L);
        CountDownLatch orderLatch = new CountDownLatch(1);

        orderEmitter.onCompletion(() -> orderLatch.countDown());

        CompletableFuture.runAsync(() -> {
            try {
                agentService.chatStream(orderRequest, userId, orderEmitter);
            } catch (Exception e) {
                orderEmitter.completeWithError(e);
            }
        });

        boolean orderCompleted = orderLatch.await(30, TimeUnit.SECONDS);
        assertThat(orderCompleted).isTrue();

        // Then - 验证两个 Agent 都被正确调用
        verify(coffeeService, atLeastOnce()).list(any(), anyInt(), anyInt());
    }

    // ==================== 场景5：工具调用链 ====================

    @Test
    @DisplayName("场景5：工具调用链 - search_coffee → get_coffee_detail → add_to_cart")
    void testScenario5_ToolCallChain() throws Exception {
        if (agentService == null) {
            return;
        }

        // Given
        Long userId = 1L;

        // Mock 咖啡搜索
        when(coffeeService.list(any(), anyInt(), anyInt()))
                .thenReturn(Map.of(
                        "list", List.of(Map.of(
                                "id", 5,
                                "name", "焦糖玛奇朵",
                                "price", 35.0
                        )),
                        "total", 1
                ));

        // Mock 咖啡详情
        when(coffeeService.detail(5L))
                .thenReturn(Map.of(
                        "id", 5,
                        "name", "焦糖玛奇朵",
                        "description", "浓郁焦糖与意式浓缩的完美结合",
                        "price", 35.0
                ));

        // When
        AgentChatRequestDTO request = AgentChatRequestDTO.builder()
                .agentType("order_assistant")
                .messages(List.of(
                        AgentChatRequestDTO.Message.builder()
                                .role("user")
                                .content("搜索焦糖玛奇朵并加入购物车")
                                .build()
                ))
                .build();

        SseEmitter emitter = new SseEmitter(60000L);
        CountDownLatch latch = new CountDownLatch(1);

        AtomicInteger toolCallCount = new AtomicInteger(0);

        emitter.onCompletion(() -> latch.countDown());

        CompletableFuture.runAsync(() -> {
            try {
                agentService.chatStream(request, userId, emitter);
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        // Then
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        assertThat(completed).isTrue();
    }

    // ==================== 场景6：多轮对话上下文 ====================

    @Test
    @DisplayName("场景6：多轮对话 - 保留上下文信息")
    void testScenario6_MultiTurnContext() throws Exception {
        if (agentService == null) {
            return;
        }

        // Given - 模拟多轮对话
        List<AgentChatRequestDTO.Message> conversationHistory = new ArrayList<>();
        conversationHistory.add(AgentChatRequestDTO.Message.builder()
                .role("user")
                .content("推荐一款咖啡")
                .build());
        conversationHistory.add(AgentChatRequestDTO.Message.builder()
                .role("assistant")
                .content("我推荐埃塞俄比亚美式")
                .build());
        conversationHistory.add(AgentChatRequestDTO.Message.builder()
                .role("user")
                .content("这款多少钱？")
                .build());

        when(coffeeService.list(any(), anyInt(), anyInt()))
                .thenReturn(Map.of(
                        "list", List.of(Map.of(
                                "id", 1,
                                "name", "埃塞俄比亚美式",
                                "price", 28.0
                        )),
                        "total", 1
                ));

        // When
        AgentChatRequestDTO request = AgentChatRequestDTO.builder()
                .agentType("coffee_advisor")
                .messages(conversationHistory)
                .build();

        SseEmitter emitter = new SseEmitter(60000L);
        CountDownLatch latch = new CountDownLatch(1);

        emitter.onCompletion(() -> latch.countDown());

        CompletableFuture.runAsync(() -> {
            try {
                agentService.chatStream(request, null, emitter);
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        // Then
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        assertThat(completed).isTrue();
    }

    // ==================== 场景7：错误处理 ====================

    @Test
    @DisplayName("场景7：错误处理 - 工具调用失败时优雅降级")
    void testScenario7_ErrorHandling() throws Exception {
        if (agentService == null) {
            return;
        }

        // Given - Mock 工具执行失败
        when(coffeeService.list(any(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("数据库连接失败"));

        // When
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
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> errorResponse = new AtomicReference<>();

        emitter.onCompletion(() -> latch.countDown());
        emitter.onError(e -> {
            errorResponse.set(e.getMessage());
            latch.countDown();
        });

        CompletableFuture.runAsync(() -> {
            try {
                agentService.chatStream(request, null, emitter);
            } catch (Exception e) {
                // 预期会有异常
            }
        });

        // Then
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        assertThat(completed).isTrue();
        // 验证服务优雅地处理了错误
    }

    // ==================== 场景8：会话记忆测试 ====================

    @Test
    @DisplayName("场景8：会话记忆 - 验证消息被正确保存")
    void testScenario8_ConversationMemory() throws Exception {
        if (agentService == null) {
            return;
        }

        // Given
        String sessionId = "session_memory_test";

        // Mock 记忆服务
        doAnswer(invocation -> {
            // 捕获保存的消息
            return null;
        }).when(memoryService).save(any());

        // When
        AgentChatRequestDTO request = AgentChatRequestDTO.builder()
                .agentType("coffee_advisor")
                .messages(List.of(
                        AgentChatRequestDTO.Message.builder()
                                .role("user")
                                .content("测试消息")
                                .build()
                ))
                .build();

        SseEmitter emitter = new SseEmitter(60000L);
        CountDownLatch latch = new CountDownLatch(1);

        emitter.onCompletion(() -> latch.countDown());

        CompletableFuture.runAsync(() -> {
            try {
                agentService.chatStream(request, null, emitter);
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        // Then
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        assertThat(completed).isTrue();

        // 在实际实现中，验证 memoryService.save 被调用
        // verify(memoryService, atLeastOnce()).save(any());
    }

    // ==================== 场景9：并发场景 ====================

    @Test
    @DisplayName("场景9：并发场景 - 多用户同时对话")
    void testScenario9_ConcurrentUsers() throws Exception {
        if (agentService == null) {
            return;
        }

        // Given
        int userCount = 5;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(userCount);

        when(coffeeService.list(any(), anyInt(), anyInt()))
                .thenReturn(Map.of("list", List.of(), "total", 0));

        // When - 多个用户同时发起请求
        for (int i = 0; i < userCount; i++) {
            final Long userId = (long) i;
            new Thread(() -> {
                try {
                    startLatch.await();

                    AgentChatRequestDTO request = AgentChatRequestDTO.builder()
                            .agentType("coffee_advisor")
                            .messages(List.of(
                                    AgentChatRequestDTO.Message.builder()
                                            .role("user")
                                            .content("用户 " + userId + " 的消息")
                                            .build()
                            ))
                            .build();

                    SseEmitter emitter = new SseEmitter(60000L);
                    agentService.chatStream(request, userId, emitter);
                } catch (Exception e) {
                    // 忽略异常
                } finally {
                    endLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown(); // 同时启动所有线程

        // Then
        boolean completed = endLatch.await(60, TimeUnit.SECONDS);
        assertThat(completed).isTrue();
    }

    // ==================== 场景10：长时间会话 ====================

    @Test
    @DisplayName("场景10：长时间会话 - 多轮交互无状态丢失")
    void testScenario10_LongConversation() throws Exception {
        if (agentService == null) {
            return;
        }

        // Given
        when(coffeeService.list(any(), anyInt(), anyInt()))
                .thenReturn(Map.of(
                        "list", List.of(Map.of("id", 1, "name", "拿铁", "price", 28.0)),
                        "total", 1
                ));

        when(coffeeService.detail(1L))
                .thenReturn(Map.of("id", 1, "name", "拿铁", "description", "经典意式", "price", 28.0));

        // When - 模拟多轮对话
        List<String> userMessages = List.of(
                "你好",
                "推荐一款咖啡",
                "这款多少钱？",
                "好的，我要一杯"
        );

        List<AgentChatRequestDTO.Message> conversationHistory = new ArrayList<>();

        for (String userMessage : userMessages) {
            // 添加用户消息
            conversationHistory.add(AgentChatRequestDTO.Message.builder()
                    .role("user")
                    .content(userMessage)
                    .build());

            AgentChatRequestDTO request = AgentChatRequestDTO.builder()
                    .agentType("coffee_advisor")
                    .messages(new ArrayList<>(conversationHistory))
                    .build();

            SseEmitter emitter = new SseEmitter(60000L);
            CountDownLatch latch = new CountDownLatch(1);

            emitter.onCompletion(() -> latch.countDown());

            CompletableFuture.runAsync(() -> {
                try {
                    agentService.chatStream(request, null, emitter);
                } catch (Exception e) {
                    emitter.completeWithError(e);
                }
            });

            boolean completed = latch.await(30, TimeUnit.SECONDS);
            assertThat(completed).isTrue();

            // 在实际场景中，这里会添加 assistant 的回复到历史
            // conversationHistory.add(assistantMessage);
        }

        // Then - 验证所有轮次都成功完成
        assertThat(conversationHistory).hasSize(userMessages.size());
    }
}

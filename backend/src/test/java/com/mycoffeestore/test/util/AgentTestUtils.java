package com.mycoffeestore.test.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycoffeestore.dto.agent.AgentChatRequestDTO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Agent 测试工具类
 * 提供通用的测试辅助方法
 *
 * @author Backend Developer
 * @since 2026-03-07
 */
public class AgentTestUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 创建简单的聊天请求
     */
    public static AgentChatRequestDTO createSimpleChatRequest(String agentType, String userMessage) {
        return AgentChatRequestDTO.builder()
                .agentType(agentType)
                .messages(List.of(
                        AgentChatRequestDTO.Message.builder()
                                .role("user")
                                .content(userMessage)
                                .build()
                ))
                .build();
    }

    /**
     * 创建多轮对话请求
     */
    public static AgentChatRequestDTO createMultiTurnChatRequest(
            String agentType,
            List<String> conversation) {

        List<AgentChatRequestDTO.Message> messages = new ArrayList<>();
        boolean isUser = true;

        for (String content : conversation) {
            messages.add(AgentChatRequestDTO.Message.builder()
                    .role(isUser ? "user" : "assistant")
                    .content(content)
                    .build());
            isUser = !isUser;
        }

        return AgentChatRequestDTO.builder()
                .agentType(agentType)
                .messages(messages)
                .build();
    }

    /**
     * 等待 SSE Emitter 完成
     */
    public static boolean awaitEmitterCompletion(SseEmitter emitter, long timeout, TimeUnit unit)
            throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Boolean> completed = new AtomicReference<>(false);

        SseEmitter.SseEventBuilder originalBuilder = SseEmitter.event();

        emitter.onCompletion(() -> {
            completed.set(true);
            latch.countDown();
        });

        emitter.onError(e -> {
            latch.countDown();
        });

        emitter.onTimeout(() -> {
            latch.countDown();
        });

        return latch.await(timeout, unit) && completed.get();
    }

    /**
     * 解析 JSON 字符串
     */
    public static JsonNode parseJson(String json) throws IOException {
        return objectMapper.readTree(json);
    }

    /**
     * 对象转 JSON 字符串
     */
    public static String toJson(Object obj) throws IOException {
        return objectMapper.writeValueAsString(obj);
    }

    /**
     * 验证 SSE 事件格式
     */
    public static boolean isValidSseEvent(String event) {
        if (event == null || event.isEmpty()) {
            return false;
        }

        // 检查是否包含 data: 前缀
        if (!event.startsWith("data:")) {
            return false;
        }

        // 提取 JSON 部分
        String jsonPart = event.substring(5).trim();

        // 检查是否为 [DONE]
        if (jsonPart.equals("[DONE]")) {
            return true;
        }

        // 验证是否为有效 JSON
        try {
            objectMapper.readTree(jsonPart);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 创建测试用的咖啡数据
     */
    public static class CoffeeTestData {
        public static final Long TEST_COFFEE_ID = 1L;
        public static final String TEST_COFFEE_NAME = "测试拿铁";
        public static final String TEST_COFFEE_CATEGORY = "拿铁";
        public static final Double TEST_COFFEE_PRICE = 28.0;
        public static final String TEST_COFFEE_DESCRIPTION = "测试用咖啡产品";

        /**
         * 创建咖啡 Map
         */
        public static java.util.Map<String, Object> createCoffeeMap() {
            return java.util.Map.of(
                    "id", TEST_COFFEE_ID,
                    "name", TEST_COFFEE_NAME,
                    "category", TEST_COFFEE_CATEGORY,
                    "price", TEST_COFFEE_PRICE,
                    "description", TEST_COFFEE_DESCRIPTION
            );
        }

        /**
         * 创建咖啡列表响应
         */
        public static java.util.Map<String, Object> createCoffeeListResponse() {
            return java.util.Map.of(
                    "list", List.of(createCoffeeMap()),
                    "total", 1
            );
        }
    }

    /**
     * 创建测试用的订单数据
     */
    public static class OrderTestData {
        public static final String TEST_ORDER_ID = "ORD123456";
        public static final String TEST_ORDER_STATUS = "pending";

        /**
         * 创建订单 Map
         */
        public static java.util.Map<String, Object> createOrderMap() {
            return java.util.Map.of(
                    "orderId", TEST_ORDER_ID,
                    "status", TEST_ORDER_STATUS,
                    "items", List.of(),
                    "totalAmount", 28.0
            );
        }

        /**
         * 创建订单列表响应
         */
        public static java.util.Map<String, Object> createOrderListResponse() {
            return java.util.Map.of(
                    "list", List.of(createOrderMap()),
                    "total", 1
            );
        }
    }

    /**
     * SSE 事件收集器
     */
    public static class SseEventCollector {
        private final List<String> events = new ArrayList<>();
        private final List<String> messageEvents = new ArrayList<>();
        private final List<String> toolCallEvents = new ArrayList<>();
        private final List<String> toolResultEvents = new ArrayList<>();
        private String doneEvent = null;

        /**
         * 添加事件
         */
        public void addEvent(String event) {
            events.add(event);

            if (event.startsWith("data:")) {
                String data = event.substring(5).trim();

                if (data.equals("[DONE]")) {
                    doneEvent = event;
                    return;
                }

                try {
                    JsonNode json = objectMapper.readTree(data);
                    String type = json.has("type") ? json.get("type").asText() : "";

                    switch (type) {
                        case "text" -> messageEvents.add(event);
                        case "tool_call" -> toolCallEvents.add(event);
                        case "tool_result" -> toolResultEvents.add(event);
                    }
                } catch (IOException e) {
                    // 忽略解析错误
                }
            }
        }

        public List<String> getAllEvents() {
            return new ArrayList<>(events);
        }

        public List<String> getMessageEvents() {
            return new ArrayList<>(messageEvents);
        }

        public List<String> getToolCallEvents() {
            return new ArrayList<>(toolCallEvents);
        }

        public List<String> getToolResultEvents() {
            return new ArrayList<>(toolResultEvents);
        }

        public String getDoneEvent() {
            return doneEvent;
        }

        public boolean hasDoneEvent() {
            return doneEvent != null;
        }

        public int getMessageCount() {
            return messageEvents.size();
        }

        public int getToolCallCount() {
            return toolCallEvents.size();
        }

        public int getToolResultCount() {
            return toolResultEvents.size();
        }

        /**
         * 清空所有事件
         */
        public void clear() {
            events.clear();
            messageEvents.clear();
            toolCallEvents.clear();
            toolResultEvents.clear();
            doneEvent = null;
        }
    }

    /**
     * 等待条件满足
     */
    public static boolean waitForCondition(java.util.function.BooleanSupplier condition,
                                          long timeout,
                                          TimeUnit unit) throws InterruptedException {
        long deadline = System.nanoTime() + unit.toNanos(timeout);

        while (System.nanoTime() < deadline) {
            if (condition.getAsBoolean()) {
                return true;
            }
            Thread.sleep(50);
        }

        return condition.getAsBoolean();
    }

    /**
     * 创建延迟执行的 Runnable
     */
    public static Runnable delayedRunnable(Runnable delegate, long delay, TimeUnit unit) {
        return () -> {
            try {
                Thread.sleep(unit.toMillis(delay));
                delegate.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
    }

    /**
     * 创建重试执行的 Runnable
     */
    public static Runnable retryingRunnable(Runnable delegate, int maxRetries, long delayMs) {
        return () -> {
            int attempt = 0;
            while (attempt < maxRetries) {
                try {
                    delegate.run();
                    return;
                } catch (Exception e) {
                    attempt++;
                    if (attempt >= maxRetries) {
                        throw new RuntimeException("重试失败", e);
                    }
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(ie);
                    }
                }
            }
        };
    }
}

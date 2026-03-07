package com.mycoffeestore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycoffeestore.dto.agent.AgentChatRequestDTO;
import com.mycoffeestore.service.agent.AgentService;
import com.mycoffeestore.util.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Agent Controller 测试
 * 测试 V1 API（手动选择 Agent）和 SSE 流式响应
 *
 * @author Backend Developer
 * @since 2026-03-07
 */
@WebMvcTest(AgentController.class)
@DisplayName("Agent Controller 测试")
public class AgentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AgentService agentService;

    @MockBean
    private JwtUtil jwtUtil;

    // 用于捕获 SSE Emitter
    private AtomicReference<SseEmitter> capturedEmitter;

    @BeforeEach
    void setUp() {
        capturedEmitter = new AtomicReference<>();

        // 配置 JWT Mock
        when(jwtUtil.isTokenExpired(any())).thenReturn(false);
        when(jwtUtil.getUserId(any())).thenReturn(1L);

        // 配置 AgentService Mock - 捕获 emitter
        doAnswer(invocation -> {
            SseEmitter emitter = invocation.getArgument(2);
            capturedEmitter.set(emitter);
            // 立即完成 emitter 以避免超时
            CompletableFuture.runAsync(() -> {
                try {
                    emitter.send(SseEmitter.event().name("message")
                            .data("{\"type\":\"text\",\"content\":\"测试响应\"}"));
                    emitter.send(SseEmitter.event().name("done")
                            .data("{\"type\":\"done\"}"));
                    emitter.complete();
                } catch (Exception e) {
                    emitter.completeWithError(e);
                }
            });
            return null;
        }).when(agentService).chatStream(any(), any(), any());
    }

    @AfterEach
    void tearDown() {
        // 清理资源
    }

    // ==================== V1 API 测试 ====================

    @Test
    @DisplayName("V1 API - 咖啡顾问聊天（未登录）")
    void testV1CoffeeAdvisorChatNotLoggedIn() throws Exception {
        // Given
        AgentChatRequestDTO request = AgentChatRequestDTO.builder()
                .agentType("coffee_advisor")
                .messages(List.of(
                        AgentChatRequestDTO.Message.builder()
                                .role("user")
                                .content("推荐一款咖啡")
                                .build()
                ))
                .build();

        // Mock JWT 失败（未登录）
        when(jwtUtil.isTokenExpired(any())).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/v1/agent/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Type"))
                .andExpect(header().string("Content-Type", "text/event-stream;charset=UTF-8"));

        // 验证服务调用，userId 为 null
        verify(agentService).chatStream(any(), isNull(), any(SseEmitter.class));
    }

    @Test
    @DisplayName("V1 API - 咖啡顾问聊天（已登录）")
    void testV1CoffeeAdvisorChatLoggedIn() throws Exception {
        // Given
        AgentChatRequestDTO request = AgentChatRequestDTO.builder()
                .agentType("coffee_advisor")
                .messages(List.of(
                        AgentChatRequestDTO.Message.builder()
                                .role("user")
                                .content("推荐一款咖啡")
                                .build()
                ))
                .build();

        // Mock JWT 成功
        when(jwtUtil.isTokenExpired(any())).thenReturn(false);
        when(jwtUtil.getUserId(any())).thenReturn(1L);

        // When & Then
        mockMvc.perform(post("/v1/agent/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer valid-token")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Type"));

        // 验证服务调用，userId 为 1L
        ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);
        verify(agentService).chatStream(any(), userIdCaptor.capture(), any(SseEmitter.class));
        assertThat(userIdCaptor.getValue()).isEqualTo(1L);
    }

    @Test
    @DisplayName("V1 API - 订单助手聊天")
    void testV1OrderAssistantChat() throws Exception {
        // Given
        AgentChatRequestDTO request = AgentChatRequestDTO.builder()
                .agentType("order_assistant")
                .messages(List.of(
                        AgentChatRequestDTO.Message.builder()
                                .role("user")
                                .content("我要下单")
                                .build()
                ))
                .build();

        // When & Then
        mockMvc.perform(post("/v1/agent/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(agentService).chatStream(any(), any(), any(SseEmitter.class));
    }

    @Test
    @DisplayName("V1 API - 客服助手聊天")
    void testV1CustomerServiceChat() throws Exception {
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

        // When & Then
        mockMvc.perform(post("/v1/agent/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(agentService).chatStream(any(), any(), any(SseEmitter.class));
    }

    @Test
    @DisplayName("V1 API - 通用聊天")
    void testV1GeneralChat() throws Exception {
        // Given
        AgentChatRequestDTO request = AgentChatRequestDTO.builder()
                .agentType("general_chat")
                .messages(List.of(
                        AgentChatRequestDTO.Message.builder()
                                .role("user")
                                .content("你好")
                                .build()
                ))
                .build();

        // When & Then
        mockMvc.perform(post("/v1/agent/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(agentService).chatStream(any(), any(), any(SseEmitter.class));
    }

    // ==================== 请求验证测试 ====================

    @Test
    @DisplayName("请求验证 - 缺少 agentType")
    void testValidationMissingAgentType() throws Exception {
        // Given - 使用无效的 JSON（缺少 agentType）
        String invalidJson = """
                {
                    "messages": [
                        {"role": "user", "content": "测试"}
                    ]
                }
                """;

        // When & Then
        mockMvc.perform(post("/v1/agent/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(agentService, never()).chatStream(any(), any(), any());
    }

    @Test
    @DisplayName("请求验证 - 空消息列表")
    void testValidationEmptyMessages() throws Exception {
        // Given
        String invalidJson = """
                {
                    "agentType": "coffee_advisor",
                    "messages": []
                }
                """;

        // When & Then
        mockMvc.perform(post("/v1/agent/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(agentService, never()).chatStream(any(), any(), any());
    }

    @Test
    @DisplayName("请求验证 - 无效的 agentType")
    void testValidationInvalidAgentType() throws Exception {
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

        // When & Then
        mockMvc.perform(post("/v1/agent/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    String response = result.getResponse().getErrorMessage();
                    assertThat(response).contains("无效的角色类型");
                });

        verify(agentService, never()).chatStream(any(), any(), any());
    }

    @Test
    @DisplayName("请求验证 - 消息缺少 role")
    void testValidationMissingRole() throws Exception {
        // Given
        String invalidJson = """
                {
                    "agentType": "coffee_advisor",
                    "messages": [
                        {"content": "测试"}
                    ]
                }
                """;

        // When & Then
        mockMvc.perform(post("/v1/agent/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(agentService, never()).chatStream(any(), any(), any());
    }

    @Test
    @DisplayName("请求验证 - 消息缺少 content")
    void testValidationMissingContent() throws Exception {
        // Given
        String invalidJson = """
                {
                    "agentType": "coffee_advisor",
                    "messages": [
                        {"role": "user"}
                    ]
                }
                """;

        // When & Then
        mockMvc.perform(post("/v1/agent/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(agentService, never()).chatStream(any(), any(), any());
    }

    // ==================== SSE 流式响应测试 ====================

    @Test
    @DisplayName("SSE 响应 - 验证 Content-Type")
    void testSSEContentType() throws Exception {
        // Given
        AgentChatRequestDTO request = AgentChatRequestDTO.builder()
                .agentType("coffee_advisor")
                .messages(List.of(
                        AgentChatRequestDTO.Message.builder()
                                .role("user")
                                .content("测试")
                                .build()
                ))
                .build();

        // When & Then
        mockMvc.perform(post("/v1/agent/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/event-stream;charset=UTF-8"));
    }

    @Test
    @DisplayName("SSE 响应 - 验证异步执行")
    void testSSEAsyncExecution() throws Exception {
        // Given
        AgentChatRequestDTO request = AgentChatRequestDTO.builder()
                .agentType("coffee_advisor")
                .messages(List.of(
                        AgentChatRequestDTO.Message.builder()
                                .role("user")
                                .content("测试")
                                .build()
                ))
                .build();

        CountDownLatch latch = new CountDownLatch(1);

        doAnswer(invocation -> {
            SseEmitter emitter = invocation.getArgument(2);
            // 异步发送响应
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(100);
                    emitter.send(SseEmitter.event().name("message")
                            .data("{\"type\":\"text\",\"content\":\"异步响应\"}"));
                    emitter.complete();
                    latch.countDown();
                } catch (Exception e) {
                    emitter.completeWithError(e);
                }
            });
            return null;
        }).when(agentService).chatStream(any(), any(), any());

        // When & Then
        mockMvc.perform(post("/v1/agent/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // 等待异步操作完成
        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
    }

    // ==================== JWT 认证测试 ====================

    @Test
    @DisplayName("JWT 认证 - 无 Authorization header")
    void testJWTNoAuthHeader() throws Exception {
        // Given
        AgentChatRequestDTO request = AgentChatRequestDTO.builder()
                .agentType("coffee_advisor")
                .messages(List.of(
                        AgentChatRequestDTO.Message.builder()
                                .role("user")
                                .content("测试")
                                .build()
                ))
                .build();

        // When & Then
        mockMvc.perform(post("/v1/agent/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // 验证 userId 为 null
        verify(agentService).chatStream(any(), isNull(), any(SseEmitter.class));
    }

    @Test
    @DisplayName("JWT 认证 - Bearer token 格式错误")
    void testJWTInvalidBearerFormat() throws Exception {
        // Given
        AgentChatRequestDTO request = AgentChatRequestDTO.builder()
                .agentType("coffee_advisor")
                .messages(List.of(
                        AgentChatRequestDTO.Message.builder()
                                .role("user")
                                .content("测试")
                                .build()
                ))
                .build();

        // Mock JWT 验证失败
        when(jwtUtil.isTokenExpired(any())).thenThrow(new RuntimeException("无效的 token"));

        // When & Then
        mockMvc.perform(post("/v1/agent/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "InvalidFormat token")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // JWT 解析失败时，userId 应为 null
        verify(agentService).chatStream(any(), isNull(), any(SseEmitter.class));
    }

    @Test
    @DisplayName("JWT 认证 - Token 过期")
    void testJWTExpiredToken() throws Exception {
        // Given
        AgentChatRequestDTO request = AgentChatRequestDTO.builder()
                .agentType("coffee_advisor")
                .messages(List.of(
                        AgentChatRequestDTO.Message.builder()
                                .role("user")
                                .content("测试")
                                .build()
                ))
                .build();

        // Mock token 过期
        when(jwtUtil.isTokenExpired(any())).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/v1/agent/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer expired-token")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Token 过期时，userId 应为 null
        verify(agentService).chatStream(any(), isNull(), any(SseEmitter.class));
    }

    @Test
    @DisplayName("JWT 认证 - 有效 token")
    void testJWTValidToken() throws Exception {
        // Given
        AgentChatRequestDTO request = AgentChatRequestDTO.builder()
                .agentType("coffee_advisor")
                .messages(List.of(
                        AgentChatRequestDTO.Message.builder()
                                .role("user")
                                .content("测试")
                                .build()
                ))
                .build();

        // Mock 有效 token
        when(jwtUtil.isTokenExpired(any())).thenReturn(false);
        when(jwtUtil.getUserId(any())).thenReturn(123L);

        // When & Then
        mockMvc.perform(post("/v1/agent/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer valid-token")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // 验证 userId 被正确提取
        ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);
        verify(agentService).chatStream(any(), userIdCaptor.capture(), any(SseEmitter.class));
        assertThat(userIdCaptor.getValue()).isEqualTo(123L);
    }

    // ==================== 多轮对话测试 ====================

    @Test
    @DisplayName("多轮对话 - 包含历史消息")
    void testMultiTurnConversation() throws Exception {
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
                                .content("多少钱？")
                                .build()
                ))
                .build();

        // When & Then
        mockMvc.perform(post("/v1/agent/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // 验证消息历史被传递
        ArgumentCaptor<AgentChatRequestDTO> requestCaptor =
                ArgumentCaptor.forClass(AgentChatRequestDTO.class);
        verify(agentService).chatStream(requestCaptor.capture(), any(), any());
        assertThat(requestCaptor.getValue().getMessages()).hasSize(3);
    }

    // ==================== Agent 类型测试 ====================

    @Test
    @DisplayName("Agent 类型 - 所有支持的类型")
    void testAllSupportedAgentTypes() throws Exception {
        // Given
        List<String> supportedTypes = List.of(
                "coffee_advisor",
                "customer_service",
                "order_assistant",
                "general_chat"
        );

        for (String agentType : supportedTypes) {
            AgentChatRequestDTO request = AgentChatRequestDTO.builder()
                    .agentType(agentType)
                    .messages(List.of(
                            AgentChatRequestDTO.Message.builder()
                                    .role("user")
                                    .content("测试")
                                    .build()
                    ))
                    .build();

            // When & Then
            mockMvc.perform(post("/v1/agent/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(agentService, atLeastOnce()).chatStream(any(), any(), any());
        }
    }
}

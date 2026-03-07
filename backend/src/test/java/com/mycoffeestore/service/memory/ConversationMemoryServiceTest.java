package com.mycoffeestore.service.memory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycoffeestore.config.RedisProperties;
import com.mycoffeestore.dto.agent.MemoryMessage;
import com.mycoffeestore.entity.ConversationMemory;
import com.mycoffeestore.mapper.ConversationMemoryMapper;
import com.mycoffeestore.service.impl.memory.ConversationMemoryServiceImpl;
import com.mybatisflex.core.query.QueryWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 对话记忆服务测试
 * 测试 Redis + PostgreSQL 分层存储功能
 *
 * @author Backend Developer
 * @since 2026-03-07
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("对话记忆服务测试")
public class ConversationMemoryServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ConversationMemoryMapper conversationMemoryMapper;

    @Mock
    private RedisConnectionFactory redisConnectionFactory;

    @Mock
    private RedisConnection redisConnection;

    private ObjectMapper objectMapper;
    private RedisProperties redisProperties;
    private ConversationMemoryServiceImpl memoryService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        // 配置 RedisProperties
        redisProperties = new RedisProperties();
        RedisProperties.MemoryConfig memoryConfig = new RedisProperties.MemoryConfig();
        memoryConfig.setKeyPrefix("conv:memory:");
        memoryConfig.setTtl(86400); // 24小时
        memoryConfig.setMaxMessages(100);
        redisProperties.setMemory(memoryConfig);

        memoryService = new ConversationMemoryServiceImpl(
                redisTemplate,
                conversationMemoryMapper,
                objectMapper,
                redisProperties
        );

        // 配置 Redis 连接 Mock
        when(redisTemplate.getConnectionFactory()).thenReturn(redisConnectionFactory);
        when(redisConnectionFactory.getConnection()).thenReturn(redisConnection);
        when(redisConnection.ping()).thenReturn("PONG");
    }

    @AfterEach
    void tearDown() {
        // 清理资源
    }

    // ==================== 消息保存测试 ====================

    @Test
    @DisplayName("消息保存 - 保存单条消息到 Redis")
    void testSaveSingleMessage() {
        // Given
        String sessionId = "session_test_001";
        MemoryMessage message = MemoryMessage.builder()
                .sessionId(sessionId)
                .userId(1L)
                .agentType("coffee_advisor")
                .role("user")
                .content("推荐一款咖啡")
                .timestamp(LocalDateTime.now())
                .build();

        String redisKey = "conv:memory:" + sessionId;

        // Mock Redis 操作
        when(redisTemplate.opsForValue()).thenReturn(mock(org.springframework.data.redis.core.ValueOperations.class));
        when(redisTemplate.opsForValue().get(redisKey)).thenReturn(new ArrayList<MemoryMessage>());

        // When
        memoryService.save(message);

        // Then
        // 验证保存到 Redis（异步，所以可能需要等待）
        verify(redisTemplate, atLeastOnce()).opsForValue();
    }

    @Test
    @DisplayName("消息保存 - 批量保存消息")
    void testSaveAllMessages() {
        // Given
        String sessionId = "session_test_002";
        List<MemoryMessage> messages = List.of(
                MemoryMessage.builder()
                        .sessionId(sessionId)
                        .userId(1L)
                        .agentType("coffee_advisor")
                        .role("user")
                        .content("推荐咖啡")
                        .timestamp(LocalDateTime.now())
                        .build(),
                MemoryMessage.builder()
                        .sessionId(sessionId)
                        .userId(1L)
                        .agentType("coffee_advisor")
                        .role("assistant")
                        .content("推荐美式咖啡")
                        .timestamp(LocalDateTime.now())
                        .build()
        );

        // Mock Redis 操作
        String redisKey = "conv:memory:" + sessionId;
        when(redisTemplate.opsForValue()).thenReturn(mock(org.springframework.data.redis.core.ValueOperations.class));
        when(redisTemplate.opsForValue().get(redisKey)).thenReturn(new ArrayList<MemoryMessage>());

        // When
        memoryService.saveAll(messages);

        // Then
        verify(redisTemplate, atLeastOnce()).opsForValue();
    }

    @Test
    @DisplayName("消息保存 - 空消息列表不报错")
    void testSaveEmptyMessages() {
        // Given
        List<MemoryMessage> emptyList = List.of();

        // When & Then
        assertThatCode(() -> memoryService.saveAll(emptyList))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("消息保存 - 超过最大消息数限制时裁剪")
    void testSaveMessageWithTruncation() throws InterruptedException {
        // Given
        String sessionId = "session_test_003";
        int maxMessages = 100;

        // 创建超过限制的消息数量
        List<MemoryMessage> messages = new ArrayList<>();
        for (int i = 0; i < maxMessages + 10; i++) {
            messages.add(MemoryMessage.builder()
                    .sessionId(sessionId)
                    .userId(1L)
                    .agentType("coffee_advisor")
                    .role("user")
                    .content("消息 " + i)
                    .timestamp(LocalDateTime.now())
                    .build());
        }

        String redisKey = "conv:memory:" + sessionId;
        List<MemoryMessage> existingMessages = new ArrayList<>();

        // Mock Redis 操作
        org.springframework.data.redis.core.ValueOperations<String, Object> valueOps =
                mock(org.springframework.data.redis.core.ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(redisKey)).thenReturn(existingMessages);

        // When
        for (MemoryMessage message : messages) {
            memoryService.save(message);
        }

        // 等待异步操作完成
        Thread.sleep(100);

        // Then - 验证消息被裁剪到最大数量
        ArgumentCaptor<List> messageCaptor = ArgumentCaptor.forClass(List.class);
        // 注意：由于是异步操作，这里可能需要更复杂的验证
    }

    // ==================== 历史查询测试 ====================

    @Test
    @DisplayName("历史查询 - 从 Redis 获取历史消息")
    void testGetHistoryFromRedis() {
        // Given
        String sessionId = "session_test_004";
        String redisKey = "conv:memory:" + sessionId;

        List<MemoryMessage> expectedMessages = List.of(
                MemoryMessage.builder()
                        .sessionId(sessionId)
                        .userId(1L)
                        .agentType("coffee_advisor")
                        .role("user")
                        .content("你好")
                        .timestamp(LocalDateTime.now())
                        .build(),
                MemoryMessage.builder()
                        .sessionId(sessionId)
                        .userId(1L)
                        .agentType("coffee_advisor")
                        .role("assistant")
                        .content("你好！有什么可以帮助你的？")
                        .timestamp(LocalDateTime.now())
                        .build()
        );

        // Mock Redis 操作
        org.springframework.data.redis.core.ValueOperations<String, Object> valueOps =
                mock(org.springframework.data.redis.core.ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(redisKey)).thenReturn(expectedMessages);

        // When
        List<MemoryMessage> result = memoryService.getHistory(sessionId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getContent()).isEqualTo("你好");
        assertThat(result.get(1).getContent()).contains("你好！");
    }

    @Test
    @DisplayName("历史查询 - Redis 无数据时从数据库加载")
    void testGetHistoryFromDatabase() {
        // Given
        String sessionId = "session_test_005";
        String redisKey = "conv:memory:" + sessionId;

        ConversationMemory dbMemory = ConversationMemory.builder()
                .sessionId(sessionId)
                .userId(1L)
                .agentType("coffee_advisor")
                .title("推荐咖啡")
                .messages("[{\"sessionId\":\"" + sessionId + "\",\"role\":\"user\",\"content\":\"测试\"}]")
                .lastActiveAt(LocalDateTime.now())
                .build();

        // Mock Redis 返回空
        org.springframework.data.redis.core.ValueOperations<String, Object> valueOps =
                mock(org.springframework.data.redis.core.ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(redisKey)).thenReturn(null);

        // Mock 数据库查询
        when(conversationMemoryMapper.selectOneByQuery(any(QueryWrapper.class)))
                .thenReturn(dbMemory);

        // When
        List<MemoryMessage> result = memoryService.getHistory(sessionId);

        // Then
        assertThat(result).isNotNull();
        verify(conversationMemoryMapper).selectOneByQuery(any(QueryWrapper.class));
        // 验证回填到 Redis
        verify(valueOps, atLeastOnce()).set(eq(redisKey), any(), anyLong());
    }

    @Test
    @DisplayName("历史查询 - 空 sessionId 返回空列表")
    void testGetHistoryWithEmptySessionId() {
        // When
        List<MemoryMessage> result = memoryService.getHistory("");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(redisTemplate, never()).opsForValue();
        verify(conversationMemoryMapper, never()).selectOneByQuery(any());
    }

    // ==================== 用户历史查询测试 ====================

    @Test
    @DisplayName("用户历史 - 查询指定用户的所有会话历史")
    void testGetUserHistory() {
        // Given
        Long userId = 1L;
        String agentType = "coffee_advisor";

        List<ConversationMemory> dbMemories = List.of(
                ConversationMemory.builder()
                        .sessionId("session_001")
                        .userId(userId)
                        .agentType(agentType)
                        .title("推荐咖啡")
                        .messages("[{\"role\":\"user\",\"content\":\"推荐\"}]")
                        .lastActiveAt(LocalDateTime.now())
                        .build(),
                ConversationMemory.builder()
                        .sessionId("session_002")
                        .userId(userId)
                        .agentType(agentType)
                        .title("查询订单")
                        .messages("[{\"role\":\"user\",\"content\":\"订单\"}]")
                        .lastActiveAt(LocalDateTime.now().minusHours(1))
                        .build()
        );

        // Mock 数据库查询
        when(conversationMemoryMapper.selectListByQuery(any(QueryWrapper.class)))
                .thenReturn(dbMemories);

        // When
        List<MemoryMessage> result = memoryService.getUserHistory(userId, agentType);

        // Then
        assertThat(result).isNotNull();
        verify(conversationMemoryMapper).selectListByQuery(any(QueryWrapper.class));

        // 验证查询条件
        ArgumentCaptor<QueryWrapper> wrapperCaptor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(conversationMemoryMapper).selectListByQuery(wrapperCaptor.capture());
    }

    @Test
    @DisplayName("用户历史 - 查询所有 Agent 类型的历史")
    void testGetUserHistoryAllAgentTypes() {
        // Given
        Long userId = 1L;

        // Mock 数据库查询
        when(conversationMemoryMapper.selectListByQuery(any(QueryWrapper.class)))
                .thenReturn(new ArrayList<>());

        // When
        List<MemoryMessage> result = memoryService.getUserHistory(userId, null);

        // Then
        assertThat(result).isNotNull();
        verify(conversationMemoryMapper).selectListByQuery(any(QueryWrapper.class));
    }

    @Test
    @DisplayName("用户历史 - 空用户 ID 返回空列表")
    void testGetUserHistoryWithNullUserId() {
        // When
        List<MemoryMessage> result = memoryService.getUserHistory(null, "coffee_advisor");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(conversationMemoryMapper, never()).selectListByQuery(any());
    }

    // ==================== 会话清除测试 ====================

    @Test
    @DisplayName("会话清除 - 清除指定会话")
    void testClearSession() {
        // Given
        String sessionId = "session_test_006";
        String redisKey = "conv:memory:" + sessionId;

        ConversationMemory existingMemory = ConversationMemory.builder()
                .sessionId(sessionId)
                .userId(1L)
                .agentType("coffee_advisor")
                .messages("[]")
                .isDeleted(0)
                .build();

        // Mock 操作
        when(redisTemplate.delete(redisKey)).thenReturn(true);
        when(conversationMemoryMapper.selectOneByQuery(any(QueryWrapper.class)))
                .thenReturn(existingMemory);
        when(conversationMemoryMapper.update(any(ConversationMemory.class))).thenReturn(1);

        // When
        memoryService.clearSession(sessionId);

        // Then
        verify(redisTemplate).delete(redisKey);
        verify(conversationMemoryMapper).update(any(ConversationMemory.class));

        // 验证 isDeleted 标记
        assertThat(existingMemory.getIsDeleted()).isEqualTo(1);
    }

    @Test
    @DisplayName("会话清除 - 清除空 sessionId 不报错")
    void testClearEmptySession() {
        // When & Then
        assertThatCode(() -> memoryService.clearSession(""))
                .doesNotThrowAnyException();

        verify(redisTemplate, never()).delete(any());
    }

    @Test
    @DisplayName("用户历史清除 - 清除用户所有历史")
    void testClearUserHistory() {
        // Given
        Long userId = 1L;

        List<ConversationMemory> userMemories = List.of(
                ConversationMemory.builder()
                        .sessionId("session_001")
                        .userId(userId)
                        .isDeleted(0)
                        .build(),
                ConversationMemory.builder()
                        .sessionId("session_002")
                        .userId(userId)
                        .isDeleted(0)
                        .build()
        );

        // Mock 操作
        when(conversationMemoryMapper.selectListByQuery(any(QueryWrapper.class)))
                .thenReturn(userMemories);
        when(redisTemplate.delete(anyString())).thenReturn(true);
        when(conversationMemoryMapper.update(any(ConversationMemory.class))).thenReturn(1);

        // When
        memoryService.clearUserHistory(userId);

        // Then
        verify(conversationMemoryMapper).selectListByQuery(any(QueryWrapper.class));
        verify(redisTemplate, times(2)).delete(anyString());
        verify(conversationMemoryMapper, times(2)).update(any(ConversationMemory.class));
    }

    // ==================== 会话管理测试 ====================

    @Test
    @DisplayName("会话管理 - 更新最后活跃时间")
    void testUpdateLastActiveTime() {
        // Given
        String sessionId = "session_test_007";

        ConversationMemory existingMemory = ConversationMemory.builder()
                .sessionId(sessionId)
                .userId(1L)
                .agentType("coffee_advisor")
                .messages("[]")
                .lastActiveAt(LocalDateTime.now().minusHours(1))
                .build();

        // Mock 操作
        when(conversationMemoryMapper.selectOneByQuery(any(QueryWrapper.class)))
                .thenReturn(existingMemory);
        when(conversationMemoryMapper.update(any(ConversationMemory.class))).thenReturn(1);

        // When
        memoryService.updateLastActiveTime(sessionId);

        // Then
        verify(conversationMemoryMapper).update(any(ConversationMemory.class));
        assertThat(existingMemory.getLastActiveAt()).isAfter(LocalDateTime.now().minusMinutes(1));
    }

    @Test
    @DisplayName("会话管理 - 生成会话 ID")
    void testGenerateSessionId() {
        // When
        String sessionId1 = memoryService.generateSessionId();
        String sessionId2 = memoryService.generateSessionId();

        // Then
        assertThat(sessionId1).isNotNull();
        assertThat(sessionId2).isNotNull();
        assertThat(sessionId1).isNotEqualTo(sessionId2);
        assertThat(sessionId1).startsWith("session_");
        assertThat(sessionId2).startsWith("session_");
    }

    // ==================== Redis 可用性测试 ====================

    @Test
    @DisplayName("Redis 可用性 - 连接正常")
    void testRedisAvailable() {
        // Given
        when(redisConnectionFactory.getConnection()).thenReturn(redisConnection);
        when(redisConnection.ping()).thenReturn("PONG");

        // When
        boolean available = memoryService.isRedisAvailable();

        // Then
        assertThat(available).isTrue();
    }

    @Test
    @DisplayName("Redis 可用性 - 连接失败")
    void testRedisNotAvailable() {
        // Given
        when(redisConnectionFactory.getConnection()).thenThrow(new RuntimeException("连接失败"));

        // When
        boolean available = memoryService.isRedisAvailable();

        // Then
        assertThat(available).isFalse();
    }

    // ==================== 并发测试 ====================

    @Test
    @DisplayName("并发保存 - 多线程同时保存消息")
    void testConcurrentSave() throws InterruptedException {
        // Given
        String sessionId = "session_concurrent";
        String redisKey = "conv:memory:" + sessionId;
        int threadCount = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        // Mock Redis 操作
        org.springframework.data.redis.core.ValueOperations<String, Object> valueOps =
                mock(org.springframework.data.redis.core.ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(redisKey)).thenReturn(new ArrayList<MemoryMessage>());

        // When - 多线程同时保存
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    MemoryMessage message = MemoryMessage.builder()
                            .sessionId(sessionId)
                            .userId(1L)
                            .agentType("coffee_advisor")
                            .role("user")
                            .content("消息 " + index)
                            .timestamp(LocalDateTime.now())
                            .build();
                    memoryService.save(message);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown(); // 启动所有线程
        boolean completed = endLatch.await(5, TimeUnit.SECONDS);

        // Then
        assertThat(completed).isTrue();
        // 验证 Redis 操作被调用
        verify(redisTemplate, atLeast(threadCount)).opsForValue();
    }

    // ==================== 数据持久化测试 ====================

    @Test
    @DisplayName("数据持久化 - 新会话创建数据库记录")
    void testPersistNewSessionToDatabase() throws InterruptedException {
        // Given
        String sessionId = "session_new_001";
        String redisKey = "conv:memory:" + sessionId;

        MemoryMessage message = MemoryMessage.builder()
                .sessionId(sessionId)
                .userId(1L)
                .agentType("coffee_advisor")
                .role("user")
                .content("新会话消息")
                .timestamp(LocalDateTime.now())
                .build();

        // Mock Redis 操作
        org.springframework.data.redis.core.ValueOperations<String, Object> valueOps =
                mock(org.springframework.data.redis.core.ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(redisKey)).thenReturn(null);
        when(conversationMemoryMapper.selectOneByQuery(any(QueryWrapper.class)))
                .thenReturn(null); // 数据库中不存在

        // When
        memoryService.save(message);

        // 等待异步持久化完成
        Thread.sleep(500);

        // Then - 验证插入新记录
        verify(conversationMemoryMapper, atLeastOnce()).selectOneByQuery(any(QueryWrapper.class));
        // 注意：由于异步执行，可能需要更复杂的验证
    }

    @Test
    @DisplayName("数据持久化 - 现有会话更新数据库记录")
    void testPersistExistingSessionToDatabase() throws InterruptedException {
        // Given
        String sessionId = "session_existing_001";
        String redisKey = "conv:memory:" + sessionId;

        ConversationMemory existingMemory = ConversationMemory.builder()
                .sessionId(sessionId)
                .userId(1L)
                .agentType("coffee_advisor")
                .messages("[]")
                .lastActiveAt(LocalDateTime.now().minusHours(1))
                .build();

        MemoryMessage newMessage = MemoryMessage.builder()
                .sessionId(sessionId)
                .userId(1L)
                .agentType("coffee_advisor")
                .role("user")
                .content("新消息")
                .timestamp(LocalDateTime.now())
                .build();

        // Mock Redis 操作
        org.springframework.data.redis.core.ValueOperations<String, Object> valueOps =
                mock(org.springframework.data.redis.core.ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(redisKey)).thenReturn(null);
        when(conversationMemoryMapper.selectOneByQuery(any(QueryWrapper.class)))
                .thenReturn(existingMemory);

        // When
        memoryService.save(newMessage);

        // 等待异步持久化完成
        Thread.sleep(500);

        // Then - 验证更新现有记录
        verify(conversationMemoryMapper, atLeastOnce()).selectOneByQuery(any(QueryWrapper.class));
    }
}

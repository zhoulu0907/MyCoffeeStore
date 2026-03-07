package com.mycoffeestore.service.impl.memory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycoffeestore.config.RedisProperties;
import com.mycoffeestore.dto.agent.MemoryMessage;
import com.mycoffeestore.entity.ConversationMemory;
import com.mycoffeestore.mapper.ConversationMemoryMapper;
import com.mycoffeestore.service.memory.ConversationMemoryService;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 对话记忆服务实现
 * 使用 Redis + PostgreSQL 分层存储
 * - Redis：存储热数据，24 小时 TTL
 * - PostgreSQL：持久化存储，支持历史查询
 *
 * @author zhoulu
 * @since 2026-03-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationMemoryServiceImpl implements ConversationMemoryService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ConversationMemoryMapper conversationMemoryMapper;
    private final ObjectMapper objectMapper;
    private final RedisProperties redisProperties;

    /**
     * 异步持久化线程池
     */
    private final Executor persistenceExecutor = Executors.newFixedThreadPool(2, r -> {
        Thread thread = new Thread(r);
        thread.setName("conversation-memory-persistence");
        thread.setDaemon(true);
        return thread;
    });

    @Override
    public void save(MemoryMessage message) {
        if (message == null) {
            log.warn("尝试保存空消息，跳过");
            return;
        }

        try {
            String redisKey = buildRedisKey(message.getSessionId());

            // 从 Redis 获取当前消息列表
            List<MemoryMessage> messages = getMessagesFromRedis(redisKey);

            // 添加新消息
            messages.add(message);

            // 限制消息数量
            int maxMessages = redisProperties.getMemory().getMaxMessages();
            if (messages.size() > maxMessages) {
                messages = messages.subList(messages.size() - maxMessages, messages.size());
            }

            // 保存到 Redis
            saveMessagesToRedis(redisKey, messages);

            // 异步持久化到 PostgreSQL
            persistToDatabaseAsync(message.getSessionId(), messages);

            log.debug("保存消息到 Redis，sessionId: {}, 消息数: {}", message.getSessionId(), messages.size());
        } catch (Exception e) {
            log.error("保存消息失败，sessionId: {}", message.getSessionId(), e);
        }
    }

    @Override
    public void saveAll(List<MemoryMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            log.warn("尝试保存空消息列表，跳过");
            return;
        }

        for (MemoryMessage message : messages) {
            save(message);
        }
    }

    @Override
    public List<MemoryMessage> getHistory(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            log.warn("sessionId 为空，返回空列表");
            return Collections.emptyList();
        }

        try {
            String redisKey = buildRedisKey(sessionId);

            // 优先从 Redis 获取
            List<MemoryMessage> messages = getMessagesFromRedis(redisKey);

            if (!messages.isEmpty()) {
                log.debug("从 Redis 获取历史消息，sessionId: {}, 消息数: {}", sessionId, messages.size());
                return messages;
            }

            // Redis 中没有数据，从 PostgreSQL 加载
            log.debug("Redis 中无数据，从 PostgreSQL 加载，sessionId: {}", sessionId);
            return loadFromDatabase(sessionId);
        } catch (Exception e) {
            log.error("获取历史消息失败，sessionId: {}", sessionId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<MemoryMessage> getUserHistory(Long userId, String agentType) {
        if (userId == null) {
            log.warn("userId 为空，返回空列表");
            return Collections.emptyList();
        }

        try {
            // 构建查询条件（使用字符串字段名）
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .where("user_id = ?", userId)
                    .and("is_deleted = ?", 0)
                    .orderBy("last_active_at", false);

            if (agentType != null && !agentType.isEmpty()) {
                queryWrapper.and("agent_type = ?", agentType);
            }

            List<ConversationMemory> memories = conversationMemoryMapper.selectListByQuery(queryWrapper);

            // 转换为 MemoryMessage 列表
            List<MemoryMessage> result = new ArrayList<>();
            for (ConversationMemory memory : memories) {
                List<MemoryMessage> messages = parseMessagesJson(memory.getMessages());
                result.addAll(messages);
            }

            log.debug("获取用户历史消息，userId: {}, agentType: {}, 会话数: {}", userId, agentType, memories.size());
            return result;
        } catch (Exception e) {
            log.error("获取用户历史消息失败，userId: {}, agentType: {}", userId, agentType, e);
            return Collections.emptyList();
        }
    }

    @Override
    public void clearSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            log.warn("sessionId 为空，跳过清除");
            return;
        }

        try {
            // 清除 Redis 中的数据
            String redisKey = buildRedisKey(sessionId);
            redisTemplate.delete(redisKey);

            // 标记数据库中的数据为已删除
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .where("session_id = ?", sessionId);
            ConversationMemory memory = conversationMemoryMapper.selectOneByQuery(queryWrapper);

            if (memory != null) {
                memory.setIsDeleted(1);
                memory.setUpdateTime(LocalDateTime.now());
                conversationMemoryMapper.update(memory);
            }

            log.info("清除会话记忆，sessionId: {}", sessionId);
        } catch (Exception e) {
            log.error("清除会话记忆失败，sessionId: {}", sessionId, e);
        }
    }

    @Override
    public void clearUserHistory(Long userId) {
        if (userId == null) {
            log.warn("userId 为空，跳过清除");
            return;
        }

        try {
            // 查询用户的所有会话
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .where("user_id = ?", userId)
                    .and("is_deleted = ?", 0);
            List<ConversationMemory> memories = conversationMemoryMapper.selectListByQuery(queryWrapper);

            // 清除每个会话的 Redis 数据
            for (ConversationMemory memory : memories) {
                String redisKey = buildRedisKey(memory.getSessionId());
                redisTemplate.delete(redisKey);
            }

            // 标记数据库中的数据为已删除
            for (ConversationMemory memory : memories) {
                memory.setIsDeleted(1);
                memory.setUpdateTime(LocalDateTime.now());
                conversationMemoryMapper.update(memory);
            }

            log.info("清除用户历史记忆，userId: {}, 会话数: {}", userId, memories.size());
        } catch (Exception e) {
            log.error("清除用户历史记忆失败，userId: {}", userId, e);
        }
    }

    @Override
    public void updateLastActiveTime(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return;
        }

        try {
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .where("session_id = ?", sessionId);
            ConversationMemory memory = conversationMemoryMapper.selectOneByQuery(queryWrapper);

            if (memory != null) {
                memory.setLastActiveAt(LocalDateTime.now());
                memory.setUpdateTime(LocalDateTime.now());
                conversationMemoryMapper.update(memory);
            }
        } catch (Exception e) {
            log.error("更新最后活跃时间失败，sessionId: {}", sessionId, e);
        }
    }

    @Override
    public String generateSessionId() {
        return "session_" + UUID.randomUUID().toString().replace("-", "");
    }

    @Override
    public boolean isRedisAvailable() {
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            return true;
        } catch (Exception e) {
            log.warn("Redis 连接不可用: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 构建 Redis Key
     */
    private String buildRedisKey(String sessionId) {
        return redisProperties.getMemory().getKeyPrefix() + sessionId;
    }

    /**
     * 从 Redis 获取消息列表
     */
    @SuppressWarnings("unchecked")
    private List<MemoryMessage> getMessagesFromRedis(String redisKey) {
        try {
            Object data = redisTemplate.opsForValue().get(redisKey);
            if (data instanceof List) {
                return (List<MemoryMessage>) data;
            }
            return new ArrayList<>();
        } catch (Exception e) {
            log.warn("从 Redis 获取消息失败，key: {}", redisKey, e);
            return new ArrayList<>();
        }
    }

    /**
     * 保存消息列表到 Redis
     */
    private void saveMessagesToRedis(String redisKey, List<MemoryMessage> messages) {
        try {
            long ttl = redisProperties.getMemory().getTtl();
            redisTemplate.opsForValue().set(redisKey, messages, ttl);
        } catch (Exception e) {
            log.warn("保存消息到 Redis 失败，key: {}", redisKey, e);
        }
    }

    /**
     * 从数据库加载消息
     */
    private List<MemoryMessage> loadFromDatabase(String sessionId) {
        try {
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .where("session_id = ?", sessionId)
                    .and("is_deleted = ?", 0);
            ConversationMemory memory = conversationMemoryMapper.selectOneByQuery(queryWrapper);

            if (memory != null) {
                List<MemoryMessage> messages = parseMessagesJson(memory.getMessages());

                // 回填到 Redis
                if (!messages.isEmpty()) {
                    String redisKey = buildRedisKey(sessionId);
                    saveMessagesToRedis(redisKey, messages);
                }

                return messages;
            }

            return new ArrayList<>();
        } catch (Exception e) {
            log.error("从数据库加载消息失败，sessionId: {}", sessionId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 异步持久化到数据库
     */
    private void persistToDatabaseAsync(String sessionId, List<MemoryMessage> messages) {
        CompletableFuture.runAsync(() -> {
            try {
                // 查询是否已存在记录
                QueryWrapper queryWrapper = QueryWrapper.create()
                        .where("session_id = ?", sessionId);
                ConversationMemory existingMemory = conversationMemoryMapper.selectOneByQuery(queryWrapper);

                // 构建 JSON
                String messagesJson = objectMapper.writeValueAsString(messages);

                if (existingMemory != null) {
                    // 更新现有记录
                    existingMemory.setMessages(messagesJson);
                    existingMemory.setLastActiveAt(LocalDateTime.now());
                    existingMemory.setUpdateTime(LocalDateTime.now());
                    conversationMemoryMapper.update(existingMemory);
                } else {
                    // 创建新记录
                    MemoryMessage firstMessage = messages.isEmpty() ? null : messages.get(0);
                    ConversationMemory newMemory = ConversationMemory.builder()
                            .sessionId(sessionId)
                            .userId(firstMessage != null ? firstMessage.getUserId() : null)
                            .agentType(firstMessage != null ? firstMessage.getAgentType() : null)
                            .messages(messagesJson)
                            .title(generateTitle(messages))
                            .lastActiveAt(LocalDateTime.now())
                            .build();
                    conversationMemoryMapper.insert(newMemory);
                }

                log.debug("异步持久化到数据库成功，sessionId: {}", sessionId);
            } catch (Exception e) {
                log.error("异步持久化到数据库失败，sessionId: {}", sessionId, e);
            }
        }, persistenceExecutor);
    }

    /**
     * 解析消息 JSON
     */
    private List<MemoryMessage> parseMessagesJson(String json) {
        try {
            if (json == null || json.isEmpty()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(json, new TypeReference<List<MemoryMessage>>() {
            });
        } catch (Exception e) {
            log.error("解析消息 JSON 失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 生成会话标题
     */
    private String generateTitle(List<MemoryMessage> messages) {
        if (messages.isEmpty()) {
            return "新对话";
        }

        // 找到第一条用户消息作为标题
        for (MemoryMessage message : messages) {
            if ("user".equals(message.getRole())) {
                String content = message.getContent();
                if (content != null && content.length() > 20) {
                    return content.substring(0, 20) + "...";
                }
                return content != null ? content : "新对话";
            }
        }

        return "新对话";
    }
}

package com.mycoffeestore.service.memory;

import com.mycoffeestore.dto.agent.MemoryMessage;

import java.util.List;

/**
 * 对话记忆服务接口
 * 提供 Redis + PostgreSQL 分层存储的对话记忆功能
 *
 * @author zhoulu
 * @since 2026-03-07
 */
public interface ConversationMemoryService {

    /**
     * 保存消息到记忆
     * 先保存到 Redis（热数据），然后异步持久化到 PostgreSQL
     *
     * @param message 消息对象
     */
    void save(MemoryMessage message);

    /**
     * 批量保存消息到记忆
     *
     * @param messages 消息列表
     */
    void saveAll(List<MemoryMessage> messages);

    /**
     * 获取指定会话的历史消息
     * 优先从 Redis 获取，如果不存在则从 PostgreSQL 加载并回填到 Redis
     *
     * @param sessionId 会话 ID
     * @return 消息列表（按时间升序排列）
     */
    List<MemoryMessage> getHistory(String sessionId);

    /**
     * 获取指定用户和 Agent 类型的所有会话历史
     * 支持跨 Agent 的历史查询
     *
     * @param userId    用户 ID
     * @param agentType Agent 类型（可选，为 null 则查询所有类型）
     * @return 会话列表（按最后活跃时间降序）
     */
    List<MemoryMessage> getUserHistory(Long userId, String agentType);

    /**
     * 清除指定会话的记忆
     * 同时清除 Redis 和 PostgreSQL 中的数据
     *
     * @param sessionId 会话 ID
     */
    void clearSession(String sessionId);

    /**
     * 清除指定用户的所有记忆
     *
     * @param userId 用户 ID
     */
    void clearUserHistory(Long userId);

    /**
     * 更新会话的最后活跃时间
     *
     * @param sessionId 会话 ID
     */
    void updateLastActiveTime(String sessionId);

    /**
     * 生成新的会话 ID
     *
     * @return 会话 ID
     */
    String generateSessionId();

    /**
     * 检查 Redis 连接是否可用
     *
     * @return true 表示可用，false 表示不可用
     */
    boolean isRedisAvailable();
}

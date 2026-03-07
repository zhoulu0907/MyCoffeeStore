package com.mycoffeestore.mapper;

import com.mycoffeestore.entity.ConversationEntity;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

import static com.mycoffeestore.entity.table.ConversationEntityTableDef.CONVERSATION_ENTITY;

/**
 * 对话记忆 Mapper
 * <p>
 * 提供对话历史的数据库操作，支持按用户、会话、Agent类型查询
 *
 * @author Backend Developer
 * @since 2026-03-07
 */
@Mapper
public interface ConversationMapper extends BaseMapper<ConversationEntity> {

    /**
     * 根据会话ID查询对话历史
     *
     * @param sessionId 会话ID
     * @return 对话列表，按创建时间升序排列
     */
    default List<ConversationEntity> findBySessionId(String sessionId) {
        return selectListByQuery(QueryWrapper.create()
                .where(CONVERSATION_ENTITY.SESSION_ID.eq(sessionId))
                .orderBy(CONVERSATION_ENTITY.CREATED_AT.asc()));
    }

    /**
     * 根据用户ID和Agent类型查询最近的对话历史
     *
     * @param userId   用户ID
     * @param agentId  Agent类型
     * @param limit    返回记录数限制
     * @return 对话列表，按创建时间升序排列
     */
    default List<ConversationEntity> findRecentByUserIdAndAgentId(@Param("userId") Long userId,
                                                                   @Param("agentId") String agentId,
                                                                   @Param("limit") int limit) {
        return selectListByQuery(QueryWrapper.create()
                .where(CONVERSATION_ENTITY.USER_ID.eq(userId))
                .and(CONVERSATION_ENTITY.AGENT_ID.eq(agentId))
                .orderBy(CONVERSATION_ENTITY.CREATED_AT.desc())
                .limit(limit));
    }

    /**
     * 根据用户ID和Agent类型查询指定时间范围内的对话历史
     *
     * @param userId   用户ID
     * @param agentId  Agent类型
     * @param start    开始时间
     * @param end      结束时间
     * @return 对话列表，按创建时间升序排列
     */
    default List<ConversationEntity> findByUserIdAndAgentIdAndTimeRange(@Param("userId") Long userId,
                                                                         @Param("agentId") String agentId,
                                                                         @Param("start") LocalDateTime start,
                                                                         @Param("end") LocalDateTime end) {
        return selectListByQuery(QueryWrapper.create()
                .where(CONVERSATION_ENTITY.USER_ID.eq(userId))
                .and(CONVERSATION_ENTITY.AGENT_ID.eq(agentId))
                .and(CONVERSATION_ENTITY.CREATED_AT.between(start, end))
                .orderBy(CONVERSATION_ENTITY.CREATED_AT.asc()));
    }

    /**
     * 分页查询用户的对话历史
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    default Page<ConversationEntity> findByUserIdWithPage(@Param("userId") Long userId,
                                                           @Param("pageNum") int pageNum,
                                                           @Param("pageSize") int pageSize) {
        return paginate(Page.of(pageNum, pageSize), QueryWrapper.create()
                .where(CONVERSATION_ENTITY.USER_ID.eq(userId))
                .orderBy(CONVERSATION_ENTITY.CREATED_AT.desc()));
    }

    /**
     * 根据会话ID删除对话历史
     *
     * @param sessionId 会话ID
     * @return 删除的记录数
     */
    default int deleteBySessionId(String sessionId) {
        return deleteByQuery(QueryWrapper.create()
                .where(CONVERSATION_ENTITY.SESSION_ID.eq(sessionId)));
    }

    /**
     * 批量插入对话记录
     *
     * @param conversations 对话列表
     * @return 插入的记录数
     */
    default int insertBatch(@Param("conversations") List<ConversationEntity> conversations) {
        if (conversations == null || conversations.isEmpty()) {
            return 0;
        }
        return insertBatch(conversations, conversations.size());
    }
}

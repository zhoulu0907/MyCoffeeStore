package com.mycoffeestore.entity;

import com.mycoffeestore.common.base.BaseEntity;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 对话记忆实体类
 * <p>
 * 用于存储 AI Agent 与用户的对话历史记录，支持 Redis + PostgreSQL 分层存储。
 * 热数据存储在 Redis 中，冷数据持久化到 PostgreSQL。
 *
 * @author Backend Developer
 * @since 2026-03-07
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "对话记忆实体")
@Table("mcs_agent_conversation")
public class ConversationEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Schema(description = "主键ID", example = "1")
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1")
    private Long userId;

    /**
     * 会话ID（用于关联同一会话的所有消息）
     */
    @Schema(description = "会话ID", example = "sess_1234567890")
    private String sessionId;

    /**
     * Agent类型（coffee_advisor / customer_service / order_assistant）
     */
    @Schema(description = "Agent类型", example = "coffee_advisor")
    private String agentId;

    /**
     * 消息ID（唯一标识单条消息）
     */
    @Schema(description = "消息ID", example = "msg_1234567890")
    private String messageId;

    /**
     * 消息角色（user / assistant / system）
     */
    @Schema(description = "消息角色", example = "user")
    private String role;

    /**
     * 消息内容
     */
    @Schema(description = "消息内容", example = "推荐一款不太酸的咖啡")
    private String content;

    /**
     * 元数据（JSON格式，存储额外信息如token数量、模型参数等）
     */
    @Schema(description = "元数据")
    private String metadata;

    /**
     * 消息创建时间（精确到毫秒，用于排序）
     */
    @Schema(description = "消息创建时间")
    private LocalDateTime createdAt;
}

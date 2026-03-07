package com.mycoffeestore.entity;

import com.mycoffeestore.common.base.BaseEntity;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 对话记忆实体类
 * 用于持久化存储用户与 Agent 的对话历史
 *
 * @author zhoulu
 * @since 2026-03-07
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "对话记忆实体")
@Table("mcs_conversation_memory")
public class ConversationMemory extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @Schema(description = "主键 ID", example = "1")
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 会话 ID（唯一标识一次对话）
     */
    @Schema(description = "会话 ID", example = "session_abc123")
    private String sessionId;

    /**
     * 用户 ID（可为 null，表示未登录用户）
     */
    @Schema(description = "用户 ID", example = "1")
    private Long userId;

    /**
     * Agent 类型（coffee_advisor / customer_service / order_assistant）
     */
    @Schema(description = "Agent 类型", example = "coffee_advisor")
    private String agentType;

    /**
     * 对话消息列表（JSON 格式存储）
     */
    @Schema(description = "对话消息列表（JSON 格式）")
    private String messages;

    /**
     * 会话标题（自动生成或用户指定）
     */
    @Schema(description = "会话标题", example = "关于咖啡推荐的对话")
    private String title;

    /**
     * 最后活跃时间
     */
    @Schema(description = "最后活跃时间")
    private java.time.LocalDateTime lastActiveAt;
}

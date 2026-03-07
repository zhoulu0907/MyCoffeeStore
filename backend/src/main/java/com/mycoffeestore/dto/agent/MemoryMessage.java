package com.mycoffeestore.dto.agent;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对话记忆消息 DTO
 * 用于在 Redis 和 PostgreSQL 之间传递对话消息数据
 *
 * @author zhoulu
 * @since 2026-03-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "对话记忆消息")
public class MemoryMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话 ID（用于标识一次对话）
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
     * 消息角色（user / assistant / system / tool）
     */
    @Schema(description = "消息角色", example = "user")
    private String role;

    /**
     * 消息内容
     */
    @Schema(description = "消息内容", example = "推荐一款不太酸的咖啡")
    private String content;

    /**
     * 工具调用 ID（仅当 role=tool 时有值）
     */
    @Schema(description = "工具调用 ID", example = "call_abc123")
    private String toolCallId;

    /**
     * 时间戳
     */
    @Schema(description = "时间戳")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * 创建当前时间戳
     */
    public static MemoryMessage withNowTimestamp() {
        return MemoryMessage.builder()
                .timestamp(LocalDateTime.now())
                .build();
    }
}

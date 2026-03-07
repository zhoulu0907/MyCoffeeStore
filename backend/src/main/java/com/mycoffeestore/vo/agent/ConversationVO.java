package com.mycoffeestore.vo.agent;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 会话详情 VO
 * <p>
 * 包含会话的完整信息，包括会话元数据和消息历史
 *
 * @author Backend Developer
 * @since 2026-03-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "会话详情")
public class ConversationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话 ID
     */
    @Schema(description = "会话 ID", example = "session_abc123")
    private String sessionId;

    /**
     * 用户 ID
     */
    @Schema(description = "用户 ID", example = "1")
    private Long userId;

    /**
     * Agent 类型
     */
    @Schema(description = "Agent 类型", example = "coffee_advisor")
    private String agentType;

    /**
     * 会话标题（从第一条用户消息提取）
     */
    @Schema(description = "会话标题", example = "推荐一款不太酸的咖啡...")
    private String title;

    /**
     * 消息数量
     */
    @Schema(description = "消息数量", example = "5")
    private Integer messageCount;

    /**
     * 最后活跃时间
     */
    @Schema(description = "最后活跃时间", example = "2026-03-07T10:30:00")
    private LocalDateTime lastActiveAt;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2026-03-07T10:00:00")
    private LocalDateTime createdAt;

    /**
     * 消息列表（按时间升序）
     */
    @Schema(description = "消息列表")
    private List<MessageItem> messages;

    /**
     * 消息项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "消息项")
    public static class MessageItem implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 消息 ID
         */
        @Schema(description = "消息 ID", example = "msg_1234567890")
        private String messageId;

        /**
         * 消息角色
         */
        @Schema(description = "消息角色", example = "user")
        private String role;

        /**
         * 消息内容
         */
        @Schema(description = "消息内容", example = "推荐一款不太酸的咖啡")
        private String content;

        /**
         * 时间戳
         */
        @Schema(description = "时间戳", example = "2026-03-07T10:30:00")
        private LocalDateTime timestamp;
    }
}

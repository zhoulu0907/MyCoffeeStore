package com.mycoffeestore.dto.agent;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对话消息 VO
 * <p>
 * 用于前后端消息传输，包含对话的基本信息和元数据
 *
 * @author Backend Developer
 * @since 2026-03-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "对话消息VO")
public class MessageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    @Schema(description = "消息ID", example = "msg_1234567890")
    private String messageId;

    /**
     * 会话ID
     */
    @Schema(description = "会话ID", example = "sess_1234567890")
    private String sessionId;

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
     * Agent类型
     */
    @Schema(description = "Agent类型", example = "coffee_advisor")
    private String agentType;

    /**
     * 消息创建时间
     */
    @Schema(description = "消息创建时间", example = "2026-03-07T10:30:00")
    private LocalDateTime createdAt;

    /**
     * Token数量（可选，从元数据中解析）
     */
    @Schema(description = "Token数量", example = "25")
    private Integer tokenCount;

    /**
     * 模型名称（可选，从元数据中解析）
     */
    @Schema(description = "模型名称", example = "gpt-4")
    private String modelName;
}

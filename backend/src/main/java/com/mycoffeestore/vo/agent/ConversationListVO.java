package com.mycoffeestore.vo.agent;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会话列表项 VO
 * <p>
 * 用于会话列表展示，包含会话的摘要信息
 *
 * @author Backend Developer
 * @since 2026-03-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "会话列表项")
public class ConversationListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话 ID
     */
    @Schema(description = "会话 ID", example = "session_abc123")
    private String sessionId;

    /**
     * Agent 类型
     */
    @Schema(description = "Agent 类型", example = "coffee_advisor")
    private String agentType;

    /**
     * Agent 类型显示名称
     */
    @Schema(description = "Agent 类型显示名称", example = "咖啡顾问")
    private String agentTypeName;

    /**
     * 会话标题
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
     * 最后一条消息预览
     */
    @Schema(description = "最后一条消息预览", example = "为您推荐这款咖啡...")
    private String lastMessagePreview;

    /**
     * Agent 类型枚举
     */
    @Schema(description = "Agent 类型")
    public enum AgentType {
        /**
         * 咖啡顾问
         */
        COFFEE_ADVISOR("coffee_advisor", "咖啡顾问"),

        /**
         * 客服助手
         */
        CUSTOMER_SERVICE("customer_service", "客服助手"),

        /**
         * 订单助手
         */
        ORDER_ASSISTANT("order_assistant", "订单助手");

        private final String code;
        private final String name;

        AgentType(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        /**
         * 根据 code 获取显示名称
         */
        public static String getDisplayName(String code) {
            for (AgentType type : values()) {
                if (type.code.equals(code)) {
                    return type.name;
                }
            }
            return "未知类型";
        }
    }
}

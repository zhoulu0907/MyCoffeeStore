package com.mycoffeestore.dto.agent;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Agent 聊天请求 DTO V2
 * <p>
 * 相比 V1 版本的改进：
 * - 支持单条消息（简化前端调用）
 * - 支持 sessionId（用于多轮对话记忆）
 * - 支持 auto 智能路由（自动选择合适的 Agent）
 * - 集成对话记忆服务
 *
 * @author Backend Developer
 * @since 2026-03-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Agent 聊天请求 V2")
public class AgentChatRequestV2DTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户消息（简化版，单条消息）
     * 前端只需传入当前用户消息，历史由 sessionId 自动加载
     */
    @NotBlank(message = "消息内容不能为空")
    @Schema(description = "用户消息内容", example = "推荐一款不太酸的咖啡")
    private String message;

    /**
     * 会话 ID（可选）
     * - 首次对话不传，系统自动生成
     * - 后续对话传入，保持对话上下文
     */
    @Schema(description = "会话 ID（可选，新会话不传）", example = "session_abc123")
    private String sessionId;

    /**
     * Agent 类型（可选，支持智能路由）
     * - auto: 智能路由（默认），系统根据用户意图自动选择
     * - coffee_advisor: 咖啡顾问
     * - customer_service: 客服助手
     * - order_assistant: 订单助手
     */
    @Pattern(regexp = "^(auto|coffee_advisor|customer_service|order_assistant)$", message = "无效的 Agent 类型")
    @Schema(description = "Agent 类型（auto/coffee_advisor/customer_service/order_assistant）", example = "auto")
    @lombok.Builder.Default
    private String agentType = "auto";

    /**
     * 用户 ID（可选，从 JWT 中自动提取）
     * 未登录用户为 null
     * Controller 中通过 @RequestAttribute 或可选解析获取
     */
    @Schema(description = "用户 ID（未登录为 null）", example = "1")
    private Long userId;

    /**
     * 是否强制创建新会话
     * - true: 忽略 sessionId，创建新会话
     * - false: 使用现有 sessionId 或创建新会话（默认）
     */
    @Schema(description = "是否强制创建新会话", example = "false")
    @lombok.Builder.Default
    private Boolean forceNewSession = false;
}

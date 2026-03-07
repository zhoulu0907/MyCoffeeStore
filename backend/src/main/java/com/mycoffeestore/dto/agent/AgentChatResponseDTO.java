package com.mycoffeestore.dto.agent;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Agent 聊天响应 DTO
 * <p>
 * 用于 SSE 流式响应，支持多种事件类型：
 * - text: 文本内容片段
 * - tool_call: 工具调用通知
 * - tool_result: 工具执行结果
 * - done: 对话完成
 * - error: 错误信息
 *
 * @author Backend Developer
 * @since 2026-03-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Agent 聊天响应")
public class AgentChatResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 事件类型
     * - text: 文本内容（流式输出）
     * - tool_call: 工具调用通知
     * - tool_result: 工具执行结果
     * - done: 对话完成
     * - error: 错误信息
     */
    @Schema(description = "事件类型（text/tool_call/tool_result/done/error）", example = "text")
    private String type;

    /**
     * 内容（type=text 时的文本内容）
     */
    @Schema(description = "文本内容", example = "根据您的偏好，为您推荐以下咖啡...")
    private String content;

    /**
     * 会话 ID（新会话时返回）
     * 前端应保存此 ID 用于后续对话
     */
    @Schema(description = "会话 ID", example = "session_abc123")
    private String sessionId;

    /**
     * 实际执行的 Agent 类型（智能路由时返回）
     * 当请求 agentType=auto 时，返回实际选择的 Agent
     */
    @Schema(description = "实际执行的 Agent 类型", example = "coffee_advisor")
    private String agentType;

    /**
     * 工具名称（type=tool_call 或 tool_result 时）
     */
    @Schema(description = "工具名称", example = "searchCoffee")
    private String toolName;

    /**
     * 工具参数（type=tool_call 时）
     */
    @Schema(description = "工具参数（JSON 字符串）", example = "{\"preference\":\"不太酸\"}")
    private String toolArgs;

    /**
     * 工具执行结果（type=tool_result 时）
     */
    @Schema(description = "工具执行结果", example = "找到 3 款咖啡...")
    private String toolResult;

    /**
     * 错误消息（type=error 时）
     */
    @Schema(description = "错误消息", example = "AI 服务暂时不可用")
    private String errorMessage;

    /**
     * 时间戳
     */
    @Schema(description = "时间戳", example = "2026-03-07T10:30:00")
    private LocalDateTime timestamp;

    /**
     * Token 使用量（可选）
     */
    @Schema(description = "Prompt Token 数量", example = "150")
    private Integer promptTokens;

    /**
     * Token 使用量（可选）
     */
    @Schema(description = "Completion Token 数量", example = "200")
    private Integer completionTokens;

    /**
     * Token 使用量（可选）
     */
    @Schema(description = "总 Token 数量", example = "350")
    private Integer totalTokens;

    /**
     * 创建文本响应
     */
    public static AgentChatResponseDTO text(String content) {
        return AgentChatResponseDTO.builder()
                .type("text")
                .content(content)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 创建工具调用响应
     */
    public static AgentChatResponseDTO toolCall(String toolName, String toolArgs) {
        return AgentChatResponseDTO.builder()
                .type("tool_call")
                .toolName(toolName)
                .toolArgs(toolArgs)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 创建工具结果响应
     */
    public static AgentChatResponseDTO toolResult(String toolName, String result) {
        return AgentChatResponseDTO.builder()
                .type("tool_result")
                .toolName(toolName)
                .toolResult(result)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 创建完成响应
     */
    public static AgentChatResponseDTO done(String sessionId, String agentType) {
        return AgentChatResponseDTO.builder()
                .type("done")
                .sessionId(sessionId)
                .agentType(agentType)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 创建错误响应
     */
    public static AgentChatResponseDTO error(String errorMessage) {
        return AgentChatResponseDTO.builder()
                .type("error")
                .errorMessage(errorMessage)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 创建会话信息响应（新会话时发送）
     */
    public static AgentChatResponseDTO sessionInfo(String sessionId, String agentType) {
        return AgentChatResponseDTO.builder()
                .type("session")
                .sessionId(sessionId)
                .agentType(agentType)
                .timestamp(LocalDateTime.now())
                .build();
    }
}

package com.mycoffeestore.dto.agent;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Agent 聊天请求 DTO
 *
 * @author zhoulu
 * @since 2026-02-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Agent 聊天请求")
public class AgentChatRequestDTO {

    /**
     * Agent 角色类型
     */
    @NotBlank(message = "角色类型不能为空")
    @Schema(description = "Agent 角色类型（coffee_advisor / customer_service / order_assistant）", example = "coffee_advisor")
    private String agentType;

    /**
     * 对话消息列表
     */
    @NotEmpty(message = "消息列表不能为空")
    @Valid
    @Schema(description = "对话消息列表")
    private List<Message> messages;

    /**
     * 对话消息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "对话消息")
    public static class Message {

        /**
         * 角色（user / assistant）
         */
        @NotBlank(message = "消息角色不能为空")
        @Schema(description = "消息角色", example = "user")
        private String role;

        /**
         * 消息内容
         */
        @NotBlank(message = "消息内容不能为空")
        @Schema(description = "消息内容", example = "推荐一款不太酸的咖啡")
        private String content;
    }
}

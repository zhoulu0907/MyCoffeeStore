package com.mycoffeestore.vo.llm;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 连接测试结果 VO
 *
 * @author Backend Developer
 * @since 2026-03-05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "连接测试结果")
public class ConnectionTestResultVO {

    /**
     * 是否成功
     */
    @Schema(description = "是否成功", example = "true")
    private Boolean success;

    /**
     * 响应时间（毫秒）
     */
    @Schema(description = "响应时间（毫秒）", example = "1234")
    private Long responseTime;

    /**
     * 错误消息
     */
    @Schema(description = "错误消息")
    private String errorMessage;

    /**
     * 提供商名称
     */
    @Schema(description = "提供商名称", example = "ModelScope 灵积")
    private String providerName;

    /**
     * 模型名称
     */
    @Schema(description = "模型名称", example = "Kimi K2.5")
    private String modelName;

    /**
     * 测试响应内容
     */
    @Schema(description = "测试响应内容")
    private String responseContent;

    public static ConnectionTestResultVO success(String providerName, String modelName, long responseTime, String responseContent) {
        return ConnectionTestResultVO.builder()
                .success(true)
                .responseTime(responseTime)
                .providerName(providerName)
                .modelName(modelName)
                .responseContent(responseContent)
                .build();
    }

    public static ConnectionTestResultVO failure(String providerName, String errorMessage) {
        return ConnectionTestResultVO.builder()
                .success(false)
                .providerName(providerName)
                .errorMessage(errorMessage)
                .build();
    }
}

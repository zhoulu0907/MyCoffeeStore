package com.mycoffeestore.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单状态枚举
 *
 * @author Backend Developer
 * @since 2024-02-26
 */
@Getter
@AllArgsConstructor
@Schema(description = "订单状态")
public enum OrderStatus {

    /**
     * 待确认
     */
    PENDING("pending", "待确认"),

    /**
     * 已确认
     */
    CONFIRMED("confirmed", "已确认"),

    /**
     * 制作中
     */
    PREPARING("preparing", "制作中"),

    /**
     * 待取餐
     */
    READY("ready", "待取餐"),

    /**
     * 已完成
     */
    COMPLETED("completed", "已完成"),

    /**
     * 已取消
     */
    CANCELLED("cancelled", "已取消");

    /**
     * 状态代码
     */
    @JsonValue
    @Schema(description = "状态代码")
    private final String code;

    /**
     * 状态名称
     */
    @Schema(description = "状态名称")
    private final String name;

    /**
     * 根据代码获取枚举
     */
    public static OrderStatus fromCode(String code) {
        for (OrderStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的订单状态: " + code);
    }
}

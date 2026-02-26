package com.mycoffeestore.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单类型枚举
 *
 * @author Backend Developer
 * @since 2024-02-26
 */
@Getter
@AllArgsConstructor
@Schema(description = "订单类型")
public enum OrderType {

    /**
     * 堂食
     */
    DINE_IN("dine_in", "堂食"),

    /**
     * 外带
     */
    TAKEAWAY("takeaway", "外带"),

    /**
     * 外卖
     */
    DELIVERY("delivery", "外卖");

    /**
     * 类型代码
     */
    @JsonValue
    @Schema(description = "类型代码")
    private final String code;

    /**
     * 类型名称
     */
    @Schema(description = "类型名称")
    private final String name;

    /**
     * 根据代码获取枚举
     */
    public static OrderType fromCode(String code) {
        for (OrderType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的订单类型: " + code);
    }
}

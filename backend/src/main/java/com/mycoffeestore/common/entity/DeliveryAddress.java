package com.mycoffeestore.common.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 配送地址实体
 *
 * @author Backend Developer
 * @since 2024-02-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "配送地址")
public class DeliveryAddress implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 详细地址
     */
    @Schema(description = "详细地址", example = "123 Haight St")
    private String address;

    /**
     * 城市
     */
    @Schema(description = "城市", example = "San Francisco")
    private String city;

    /**
     * 州/省
     */
    @Schema(description = "州/省", example = "CA")
    private String state;

    /**
     * 邮编
     */
    @Schema(description = "邮编", example = "94117")
    private String zipCode;

    /**
     * 联系电话
     */
    @Schema(description = "联系电话", example = "14155551234")
    private String phone;
}

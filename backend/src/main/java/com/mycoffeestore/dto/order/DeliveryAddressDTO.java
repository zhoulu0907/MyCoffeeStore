package com.mycoffeestore.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 配送地址DTO
 *
 * @author Backend Developer
 * @since 2024-02-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "配送地址")
public class DeliveryAddressDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 详细地址
     */
    @Schema(description = "详细地址", example = "123 Haight St", required = true)
    @NotBlank(message = "详细地址不能为空")
    private String address;

    /**
     * 城市
     */
    @Schema(description = "城市", example = "San Francisco", required = true)
    @NotBlank(message = "城市不能为空")
    private String city;

    /**
     * 州/省
     */
    @Schema(description = "州/省", example = "CA", required = true)
    @NotBlank(message = "州/省不能为空")
    private String state;

    /**
     * 邮编
     */
    @Schema(description = "邮编", example = "94117", required = true)
    @NotBlank(message = "邮编不能为空")
    private String zipCode;

    /**
     * 联系电话
     */
    @Schema(description = "联系电话", example = "14155551234", required = true)
    @NotBlank(message = "联系电话不能为空")
    private String phone;
}

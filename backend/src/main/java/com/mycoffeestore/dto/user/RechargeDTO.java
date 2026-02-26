package com.mycoffeestore.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 充值请求DTO
 *
 * @author Backend Developer
 * @since 2024-02-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "充值请求")
public class RechargeDTO {

    /**
     * 充值金额
     */
    @NotNull(message = "充值金额不能为空")
    @DecimalMin(value = "0.01", message = "充值金额必须大于0")
    @Schema(description = "充值金额", example = "100.00")
    private BigDecimal amount;
}

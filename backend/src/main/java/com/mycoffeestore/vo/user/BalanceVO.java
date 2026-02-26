package com.mycoffeestore.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 余额响应VO
 *
 * @author Backend Developer
 * @since 2024-02-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "余额响应")
public class BalanceVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 账户余额
     */
    @Schema(description = "账户余额", example = "500.00")
    private BigDecimal balance;
}

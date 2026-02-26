package com.mycoffeestore.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单项DTO
 *
 * @author Backend Developer
 * @since 2024-02-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单项")
public class OrderItemDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 咖啡ID
     */
    @Schema(description = "咖啡ID", example = "1", required = true)
    @NotNull(message = "咖啡ID不能为空")
    private Long coffeeId;

    /**
     * 数量
     */
    @Schema(description = "数量", example = "2", required = true)
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量必须大于0")
    private Integer quantity;

    /**
     * 单价
     */
    @Schema(description = "单价", example = "4.50", required = true)
    @NotNull(message = "单价不能为空")
    private BigDecimal price;
}

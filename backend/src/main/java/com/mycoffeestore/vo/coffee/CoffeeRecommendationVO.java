package com.mycoffeestore.vo.coffee;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 咖啡推荐VO
 *
 * @author Backend Developer
 * @since 2024-02-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "咖啡推荐")
public class CoffeeRecommendationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 咖啡ID
     */
    @Schema(description = "咖啡ID", example = "1")
    private Long coffeeId;

    /**
     * 咖啡名称
     */
    @Schema(description = "咖啡名称", example = "经典美式")
    private String name;

    /**
     * 咖啡描述
     */
    @Schema(description = "咖啡描述", example = "精选阿拉比卡豆，深度烘焙")
    private String description;

    /**
     * 主图URL
     */
    @Schema(description = "主图URL")
    private String imageUrl;

    /**
     * 价格
     */
    @Schema(description = "价格", example = "4.50")
    private BigDecimal price;

    /**
     * 推荐理由
     */
    @Schema(description = "推荐理由", example = "适合咖啡初学者，口感温和不苦涩")
    private String reason;

    /**
     * 推荐指数 1-5
     */
    @Schema(description = "推荐指数 1-5", example = "5")
    private Integer rating;
}

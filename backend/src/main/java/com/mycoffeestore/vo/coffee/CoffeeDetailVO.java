package com.mycoffeestore.vo.coffee;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 咖啡详情VO
 *
 * @author Backend Developer
 * @since 2024-02-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "咖啡详情")
public class CoffeeDetailVO implements Serializable {

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
    @Schema(description = "咖啡描述", example = "精选阿拉比卡豆，深度烘焙，口感醇厚")
    private String description;

    /**
     * 现价
     */
    @Schema(description = "现价", example = "4.50")
    private BigDecimal price;

    /**
     * 原价
     */
    @Schema(description = "原价", example = "5.50")
    private BigDecimal originalPrice;

    /**
     * 分类
     */
    @Schema(description = "分类", example = "espresso")
    private String category;

    /**
     * 分类名称
     */
    @Schema(description = "分类名称", example = "意式浓缩系列")
    private String categoryName;

    /**
     * 主图URL
     */
    @Schema(description = "主图URL")
    private String imageUrl;

    /**
     * 图片列表
     */
    @Schema(description = "图片列表")
    private List<String> images;

    /**
     * 库存
     */
    @Schema(description = "库存", example = "100")
    private Integer stock;

    /**
     * 状态
     */
    @Schema(description = "状态：0-下架，1-上架，2-售罄", example = "1")
    private Integer status;

    /**
     * 销量
     */
    @Schema(description = "销量", example = "1250")
    private Integer sales;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}

package com.mycoffeestore.entity;

import com.mycoffeestore.common.base.BaseEntity;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 咖啡产品实体类
 *
 * @author Backend Developer
 * @since 2024-02-26
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "咖啡产品实体")
@Table("mcs_coffee")
public class Coffee extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 咖啡ID
     */
    @Schema(description = "咖啡ID", example = "1")
    @Id(keyType = KeyType.Auto)
    private Long id;

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
     * 现价
     */
    @Schema(description = "现价", example = "4.50")
    private java.math.BigDecimal price;

    /**
     * 原价
     */
    @Schema(description = "原价", example = "5.50")
    private java.math.BigDecimal originalPrice;

    /**
     * 分类：espresso-意式，brew-手冲，cold-冷萃，blend-拼配
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
     * 图片列表（JSON）
     */
    @Schema(description = "图片列表")
    @Column(typeHandler = com.mycoffeestore.handler.JsonbTypeHandler.class)
    private String images;

    /**
     * 库存数量
     */
    @Schema(description = "库存数量", example = "100")
    private Integer stock;

    /**
     * 销量
     */
    @Schema(description = "销量", example = "1250")
    private Integer sales;

    /**
     * 状态：0-下架，1-上架，2-售罄
     */
    @Schema(description = "状态：0-下架，1-上架，2-售罄", example = "1")
    private Integer status;

    /**
     * 排序值
     */
    @Schema(description = "排序值", example = "100")
    private Integer sortOrder;
}

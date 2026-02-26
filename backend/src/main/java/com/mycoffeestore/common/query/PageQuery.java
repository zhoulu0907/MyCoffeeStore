package com.mycoffeestore.common.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 分页查询基类
 *
 * @author Backend Developer
 * @since 2024-02-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页查询参数")
public class PageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 页码
     */
    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    /**
     * 每页数量
     */
    @Schema(description = "每页数量", example = "10")
    private Integer size = 10;

    /**
     * 获取偏移量
     */
    public Integer getOffset() {
        return (page - 1) * size;
    }
}

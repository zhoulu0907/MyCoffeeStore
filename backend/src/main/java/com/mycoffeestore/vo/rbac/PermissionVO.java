package com.mycoffeestore.vo.rbac;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 权限信息VO
 *
 * @author Backend Developer
 * @since 2026-03-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "权限信息")
public class PermissionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 权限ID
     */
    @Schema(description = "权限ID", example = "1")
    private Long id;

    /**
     * 权限编码
     */
    @Schema(description = "权限编码", example = "order:create")
    private String code;

    /**
     * 权限名称
     */
    @Schema(description = "权限名称", example = "创建订单")
    private String name;

    /**
     * 权限描述
     */
    @Schema(description = "权限描述")
    private String description;
}

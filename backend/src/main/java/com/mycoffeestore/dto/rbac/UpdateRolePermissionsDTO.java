package com.mycoffeestore.dto.rbac;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 更新角色权限请求DTO
 *
 * @author Backend Developer
 * @since 2026-03-06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新角色权限请求")
public class UpdateRolePermissionsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    @Schema(description = "角色ID", example = "1")
    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    /**
     * 权限ID列表
     */
    @Schema(description = "权限ID列表")
    @NotNull(message = "权限ID列表不能为空")
    private List<Long> permissionIds;
}

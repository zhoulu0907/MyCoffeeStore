package com.mycoffeestore.dto.rbac;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 分配角色请求DTO
 *
 * @author Backend Developer
 * @since 2026-03-06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分配角色请求")
public class AssignRoleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 角色编码
     */
    @Schema(description = "角色编码", example = "staff")
    @NotBlank(message = "角色编码不能为空")
    private String roleCode;
}

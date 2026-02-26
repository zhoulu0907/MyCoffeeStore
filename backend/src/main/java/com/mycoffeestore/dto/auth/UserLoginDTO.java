package com.mycoffeestore.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户登录请求DTO
 *
 * @author Backend Developer
 * @since 2024-02-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户登录请求")
public class UserLoginDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 账号（用户名/邮箱/手机号）
     */
    @Schema(description = "账号", example = "coffee_lover", required = true)
    @NotBlank(message = "账号不能为空")
    private String account;

    /**
     * 密码
     */
    @Schema(description = "密码", example = "Coffee123!", required = true)
    @NotBlank(message = "密码不能为空")
    private String password;
}

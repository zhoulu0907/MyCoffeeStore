package com.mycoffeestore.vo.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 登录响应VO
 *
 * @author Backend Developer
 * @since 2024-02-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应")
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1")
    private Long userId;

    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "coffee_lover")
    private String username;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱", example = "coffee@example.com")
    private String email;

    /**
     * 手机号
     */
    @Schema(description = "手机号", example = "14155551234")
    private String phone;

    /**
     * 访问令牌
     */
    @Schema(description = "访问令牌")
    private String token;
}

package com.mycoffeestore.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限校验注解
 * 标注在 Controller 方法或类上，用于声明访问该接口所需的权限。
 *
 * @author Backend Developer
 * @since 2026-03-06
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {

    /**
     * 所需的权限编码
     *
     * @return 权限编码，如 "order:update_status"
     */
    String value();
}

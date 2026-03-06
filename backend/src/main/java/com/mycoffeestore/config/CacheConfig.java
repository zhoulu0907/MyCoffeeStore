package com.mycoffeestore.config;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 缓存配置类
 *
 * @author Backend Developer
 * @since 2026-03-06
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 清除用户权限缓存的注解
     * 当用户角色发生变化时使用
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @CacheEvict(value = "userPermissions", allEntries = true)
    public @interface ClearUserPermissionsCache {
    }
}

package com.mycoffeestore.config;

import com.mycoffeestore.annotation.RequirePermission;
import com.mycoffeestore.service.rbac.RbacService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

/**
 * 权限校验拦截器
 * 检查请求处理方法上的 @RequirePermission 注解，验证用户是否具有所需权限。
 *
 * @author Backend Developer
 * @since 2026-03-06
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionInterceptor implements HandlerInterceptor {

    private final RbacService rbacService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 非 HandlerMethod 直接放行
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // 获取方法上的 @RequirePermission 注解
        RequirePermission annotation = handlerMethod.getMethodAnnotation(RequirePermission.class);

        // 如果方法没有注解，检查类上是否有注解
        if (annotation == null) {
            annotation = handlerMethod.getBeanType().getAnnotation(RequirePermission.class);
        }

        // 无注解直接放行
        if (annotation == null) {
            return true;
        }

        // 获取用户ID
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录\",\"timestamp\":" + System.currentTimeMillis() + "}");
            return false;
        }

        Long userId = (Long) userIdObj;
        String requiredPermission = annotation.value();

        // 获取用户权限列表
        List<String> permissions = rbacService.getUserPermissions(userId);

        if (!permissions.contains(requiredPermission)) {
            log.warn("权限不足: userId={}, required={}, has={}", userId, requiredPermission, permissions);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":403,\"message\":\"权限不足\",\"timestamp\":" + System.currentTimeMillis() + "}");
            return false;
        }

        return true;
    }
}

package com.mycoffeestore.config;

import com.mycoffeestore.annotation.RequirePermission;
import com.mycoffeestore.service.rbac.RbacService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.method.HandlerMethod;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * PermissionInterceptor 单元测试
 *
 * @author Backend Developer
 * @since 2026-03-06
 */
@ExtendWith(MockitoExtension.class)
class PermissionInterceptorTest {

    @Mock
    private RbacService rbacService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private PermissionInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new PermissionInterceptor(rbacService);
    }

    @Test
    @DisplayName("非 HandlerMethod 类型直接放行")
    void preHandle_notHandlerMethod() throws Exception {
        assertTrue(interceptor.preHandle(request, response, new Object()));
    }

    @Test
    @DisplayName("方法无 @RequirePermission 注解直接放行")
    void preHandle_noAnnotation() throws Exception {
        Method method = TestController.class.getMethod("noPermission");
        HandlerMethod handlerMethod = new HandlerMethod(new TestController(), method);

        assertTrue(interceptor.preHandle(request, response, handlerMethod));
    }

    @Test
    @DisplayName("有权限时放行")
    void preHandle_hasPermission() throws Exception {
        Method method = TestController.class.getMethod("needsPermission");
        HandlerMethod handlerMethod = new HandlerMethod(new TestController(), method);

        when(request.getAttribute("userId")).thenReturn(1L);
        when(rbacService.getUserPermissions(1L)).thenReturn(Arrays.asList("order:update_status", "order:view"));

        assertTrue(interceptor.preHandle(request, response, handlerMethod));
    }

    @Test
    @DisplayName("无权限时返回 403")
    void preHandle_noPermission() throws Exception {
        Method method = TestController.class.getMethod("needsPermission");
        HandlerMethod handlerMethod = new HandlerMethod(new TestController(), method);

        when(request.getAttribute("userId")).thenReturn(1L);
        when(rbacService.getUserPermissions(1L)).thenReturn(Collections.singletonList("order:view"));

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        assertFalse(interceptor.preHandle(request, response, handlerMethod));
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    @DisplayName("userId 为空时返回 401")
    void preHandle_noUserId() throws Exception {
        Method method = TestController.class.getMethod("needsPermission");
        HandlerMethod handlerMethod = new HandlerMethod(new TestController(), method);

        when(request.getAttribute("userId")).thenReturn(null);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        assertFalse(interceptor.preHandle(request, response, handlerMethod));
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    // 测试用的 Controller
    static class TestController {
        public void noPermission() {
        }

        @RequirePermission("order:update_status")
        public void needsPermission() {
        }
    }
}

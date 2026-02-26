package com.mycoffeestore.controller;

import com.mycoffeestore.common.result.Result;
import com.mycoffeestore.dto.auth.UserLoginDTO;
import com.mycoffeestore.dto.auth.UserRegisterDTO;
import com.mycoffeestore.service.auth.UserAuthService;
import com.mycoffeestore.util.JwtUtil;
import com.mycoffeestore.vo.auth.LoginVO;
import com.mycoffeestore.vo.auth.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户认证控制器
 *
 * @author Backend Developer
 * @since 2024-02-26
 */
@Slf4j
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "用户认证", description = "用户注册、登录、退出等接口")
public class AuthController {

    private final UserAuthService userAuthService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "创建新用户账户")
    public Result<LoginVO> register(@Valid @RequestBody UserRegisterDTO dto) {
        LoginVO loginVO = userAuthService.register(dto);
        return Result.success("注册成功", loginVO);
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录获取Token")
    public Result<LoginVO> login(@Valid @RequestBody UserLoginDTO dto) {
        LoginVO loginVO = userAuthService.login(dto);
        return Result.success("登录成功", loginVO);
    }

    @GetMapping("/info")
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的信息")
    public Result<UserVO> getUserInfo(@RequestHeader("Authorization") String authHeader) {
        // 提取Token
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtil.getUserId(token);
        UserVO userVO = userAuthService.getUserInfo(userId);
        return Result.success(userVO);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户退出", description = "用户退出登录")
    public Result<Void> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtil.getUserId(token);
        userAuthService.logout(userId);
        return Result.success("退出成功", null);
    }
}

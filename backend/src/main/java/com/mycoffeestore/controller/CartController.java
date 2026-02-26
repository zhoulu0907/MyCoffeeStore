package com.mycoffeestore.controller;

import com.mycoffeestore.common.result.Result;
import com.mycoffeestore.dto.cart.CartAddDTO;
import com.mycoffeestore.dto.cart.CartUpdateDTO;
import com.mycoffeestore.service.cart.CartService;
import com.mycoffeestore.util.JwtUtil;
import com.mycoffeestore.vo.cart.CartListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 购物车控制器
 *
 * @author Backend Developer
 * @since 2024-02-26
 */
@Slf4j
@RestController
@RequestMapping("/v1/cart")
@RequiredArgsConstructor
@Tag(name = "购物车", description = "购物车管理接口")
public class CartController {

    private final CartService cartService;
    private final JwtUtil jwtUtil;

    @PostMapping("/add")
    @Operation(summary = "添加到购物车", description = "将咖啡产品添加到购物车")
    public Result<Void> add(@RequestHeader("Authorization") String authHeader,
                            @Valid @RequestBody CartAddDTO dto) {
        Long userId = getUserIdFromToken(authHeader);
        cartService.add(userId, dto);
        return Result.success("添加成功", null);
    }

    @PostMapping("/remove")
    @Operation(summary = "从购物车移除", description = "从购物车中移除指定商品")
    public Result<Void> remove(@RequestHeader("Authorization") String authHeader,
                               @RequestParam Long cartId) {
        Long userId = getUserIdFromToken(authHeader);
        cartService.remove(userId, cartId);
        return Result.success("移除成功", null);
    }

    @PostMapping("/update")
    @Operation(summary = "更新购物车数量", description = "更新购物车中商品的数量")
    public Result<Void> update(@RequestHeader("Authorization") String authHeader,
                               @Valid @RequestBody CartUpdateDTO dto) {
        Long userId = getUserIdFromToken(authHeader);
        cartService.update(userId, dto);
        return Result.success("更新成功", null);
    }

    @GetMapping("/list")
    @Operation(summary = "获取购物车列表", description = "获取当前用户的购物车列表")
    public Result<CartListVO> list(@RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        CartListVO listVO = cartService.list(userId);
        return Result.success(listVO);
    }

    @PostMapping("/clear")
    @Operation(summary = "清空购物车", description = "清空当前用户的购物车")
    public Result<Void> clear(@RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        cartService.clear(userId);
        return Result.success("清空成功", null);
    }

    /**
     * 从Token中获取用户ID
     */
    private Long getUserIdFromToken(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.getUserId(token);
    }
}

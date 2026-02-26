package com.mycoffeestore.controller;

import com.mycoffeestore.common.result.Result;
import com.mycoffeestore.dto.user.RechargeDTO;
import com.mycoffeestore.entity.User;
import com.mycoffeestore.exception.BusinessException;
import com.mycoffeestore.mapper.UserMapper;
import com.mycoffeestore.util.JwtUtil;
import com.mycoffeestore.vo.user.BalanceVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 *
 * @author Backend Developer
 * @since 2024-02-26
 */
@Slf4j
@RestController
@RequestMapping("/v1/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户余额查询、充值等接口")
public class UserController {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    @GetMapping("/balance")
    @Operation(summary = "查询余额", description = "查询当前用户的账户余额")
    public Result<BalanceVO> getBalance(@RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        User user = userMapper.selectOneById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        BalanceVO balanceVO = BalanceVO.builder()
                .balance(user.getBalance())
                .build();
        return Result.success(balanceVO);
    }

    @PostMapping("/recharge")
    @Operation(summary = "充值", description = "给当前用户账户充值")
    public Result<BalanceVO> recharge(@RequestHeader("Authorization") String authHeader,
                                     @Valid @RequestBody RechargeDTO dto) {
        Long userId = getUserIdFromToken(authHeader);
        User user = userMapper.selectOneById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 增加余额
        user.setBalance(user.getBalance().add(dto.getAmount()));
        userMapper.update(user);

        log.info("用户充值成功: userId={}, amount={}, newBalance={}", userId, dto.getAmount(), user.getBalance());

        BalanceVO balanceVO = BalanceVO.builder()
                .balance(user.getBalance())
                .build();
        return Result.success("充值成功", balanceVO);
    }

    /**
     * 从Token中获取用户ID
     */
    private Long getUserIdFromToken(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.getUserId(token);
    }
}

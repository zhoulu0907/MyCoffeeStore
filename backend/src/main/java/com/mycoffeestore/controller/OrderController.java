package com.mycoffeestore.controller;

import com.mycoffeestore.annotation.RequirePermission;
import com.mycoffeestore.common.result.PageResult;
import com.mycoffeestore.common.result.Result;
import com.mycoffeestore.dto.order.OrderCreateDTO;
import com.mycoffeestore.enums.OrderStatus;
import com.mycoffeestore.service.order.OrderService;
import com.mycoffeestore.vo.order.OrderDetailVO;
import com.mycoffeestore.vo.order.OrderListItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 订单控制器
 *
 * @author Backend Developer
 * @since 2024-02-26
 */
@Slf4j
@RestController
@RequestMapping("/v1/order")
@RequiredArgsConstructor
@Tag(name = "订单管理", description = "订单创建、查询、取消等接口")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    @Operation(summary = "创建订单", description = "创建新订单")
    public Result<OrderDetailVO> create(@RequestAttribute("userId") Long userId,
                                        @Valid @RequestBody OrderCreateDTO dto) {
        OrderDetailVO detailVO = orderService.create(userId, dto);
        return Result.success("订单创建成功", detailVO);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取订单详情", description = "根据订单号获取订单详细信息")
    public Result<OrderDetailVO> detail(@RequestAttribute("userId") Long userId,
                                        @RequestParam String orderId) {
        OrderDetailVO detailVO = orderService.detail(userId, orderId);
        return Result.success(detailVO);
    }

    @GetMapping("/list")
    @Operation(summary = "获取订单列表", description = "分页获取订单列表，user 角色只能看自己的，staff/admin 角色可看全部")
    public Result<PageResult<OrderListItemVO>> list(
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("role") String role,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        OrderStatus orderStatus = null;
        if (status != null && !status.isEmpty()) {
            orderStatus = OrderStatus.fromCode(status);
        }

        PageResult<OrderListItemVO> result = orderService.list(userId, role, orderStatus, page, size);
        return Result.success(result);
    }

    @PostMapping("/cancel")
    @Operation(summary = "取消订单", description = "取消指定的订单")
    public Result<Void> cancel(@RequestAttribute("userId") Long userId,
                               @RequestParam String orderId,
                               @RequestParam(required = false) String reason) {
        orderService.cancel(userId, orderId, reason);
        return Result.success("订单已取消", null);
    }

    @PostMapping("/update-status")
    @RequirePermission("order:update_status")
    @Operation(summary = "更新订单状态", description = "管理员更新订单状态")
    public Result<Void> updateStatus(@RequestParam String orderId,
                                     @RequestParam String status) {
        OrderStatus orderStatus = OrderStatus.fromCode(status);
        orderService.updateStatus(orderId, orderStatus);
        return Result.success("状态更新成功", null);
    }
}

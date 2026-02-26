package com.mycoffeestore.controller;

import com.mycoffeestore.common.result.Result;
import com.mycoffeestore.util.OrderDataGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 数据生成控制器
 * 仅用于开发/测试环境
 *
 * @author zhoulu
 * @since 2026-02-26
 */
@Slf4j
@RestController
@RequestMapping("/v1/data")
@RequiredArgsConstructor
@Tag(name = "数据生成", description = "测试数据生成接口")
public class DataController {

    private final OrderDataGenerator orderDataGenerator;

    @PostMapping("/generate-orders")
    @Operation(summary = "生成历史订单数据", description = "为 zhoulu 用户生成500天的历史订单")
    public Result<String> generateOrders(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "false") boolean forceClean) {
        try {
            log.info("开始生成历史订单数据... forceClean={}", forceClean);
            orderDataGenerator.run(forceClean);
            return Result.success("订单数据生成成功", "已完成");
        } catch (Exception e) {
            log.error("生成订单数据失败", e);
            return Result.error(500, "生成失败: " + e.getMessage());
        }
    }
}

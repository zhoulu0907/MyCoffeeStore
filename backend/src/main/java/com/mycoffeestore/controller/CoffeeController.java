package com.mycoffeestore.controller;

import com.mycoffeestore.common.result.PageResult;
import com.mycoffeestore.common.result.Result;
import com.mycoffeestore.service.coffee.CoffeeService;
import com.mycoffeestore.vo.coffee.CoffeeCategoryVO;
import com.mycoffeestore.vo.coffee.CoffeeDetailVO;
import com.mycoffeestore.vo.coffee.CoffeeListItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 咖啡产品控制器
 *
 * @author Backend Developer
 * @since 2024-02-26
 */
@Slf4j
@RestController
@RequestMapping("/v1/coffee")
@RequiredArgsConstructor
@Tag(name = "咖啡产品", description = "咖啡产品查询接口")
public class CoffeeController {

    private final CoffeeService coffeeService;

    @GetMapping("/list")
    @Operation(summary = "获取咖啡列表", description = "分页获取咖啡产品列表")
    public Result<PageResult<CoffeeListItemVO>> list(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        PageResult<CoffeeListItemVO> result = coffeeService.list(category, page, size);
        return Result.success(result);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取咖啡详情", description = "获取单个咖啡产品的详细信息")
    public Result<CoffeeDetailVO> detail(@RequestParam Long coffeeId) {
        CoffeeDetailVO detailVO = coffeeService.detail(coffeeId);
        return Result.success(detailVO);
    }

    @GetMapping("/categories")
    @Operation(summary = "获取咖啡分类", description = "获取所有咖啡分类及数量")
    public Result<List<CoffeeCategoryVO>> categories() {
        List<CoffeeCategoryVO> categories = coffeeService.categories();
        return Result.success(categories);
    }
}

package com.mycoffeestore.service.coffee;

import com.mycoffeestore.common.result.PageResult;
import com.mycoffeestore.vo.coffee.CoffeeCategoryVO;
import com.mycoffeestore.vo.coffee.CoffeeDetailVO;
import com.mycoffeestore.vo.coffee.CoffeeListItemVO;

/**
 * 咖啡产品服务接口
 *
 * @author Backend Developer
 * @since 2024-02-26
 */
public interface CoffeeService {

    /**
     * 获取咖啡列表（分页）
     *
     * @param category 分类（可选）
     * @param page     页码
     * @param size     每页数量
     * @return 咖啡列表
     */
    PageResult<CoffeeListItemVO> list(String category, Integer page, Integer size);

    /**
     * 获取咖啡详情
     *
     * @param coffeeId 咖啡ID
     * @return 咖啡详情
     */
    CoffeeDetailVO detail(Long coffeeId);

    /**
     * 获取咖啡分类
     *
     * @return 分类列表
     */
    java.util.List<CoffeeCategoryVO> categories();
}

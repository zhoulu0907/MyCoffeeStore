package com.mycoffeestore.service.order;

import com.mycoffeestore.common.result.PageResult;
import com.mycoffeestore.dto.order.OrderCreateDTO;
import com.mycoffeestore.enums.OrderStatus;
import com.mycoffeestore.vo.order.OrderDetailVO;
import com.mycoffeestore.vo.order.OrderListItemVO;

/**
 * 订单服务接口
 *
 * @author Backend Developer
 * @since 2024-02-26
 */
public interface OrderService {

    /**
     * 创建订单
     *
     * @param userId 用户ID
     * @param dto    创建订单请求DTO
     * @return 订单详情
     */
    OrderDetailVO create(Long userId, OrderCreateDTO dto);

    /**
     * 获取订单详情
     *
     * @param userId  用户ID
     * @param orderNo 订单号
     * @return 订单详情
     */
    OrderDetailVO detail(Long userId, String orderNo);

    /**
     * 获取订单列表
     *
     * @param userId 用户ID
     * @param status 状态（可选）
     * @param page   页码
     * @param size   每页数量
     * @return 订单列表
     */
    PageResult<OrderListItemVO> list(Long userId, OrderStatus status, Integer page, Integer size);

    /**
     * 取消订单
     *
     * @param userId  用户ID
     * @param orderNo 订单号
     * @param reason  取消原因
     */
    void cancel(Long userId, String orderNo, String reason);

    /**
     * 更新订单状态（管理员）
     *
     * @param orderNo 订单号
     * @param status  新状态
     */
    void updateStatus(String orderNo, OrderStatus status);
}

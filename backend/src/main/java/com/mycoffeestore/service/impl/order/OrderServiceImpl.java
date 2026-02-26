package com.mycoffeestore.service.impl.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycoffeestore.common.entity.DeliveryAddress;
import com.mycoffeestore.common.result.PageResult;
import com.mycoffeestore.dto.order.DeliveryAddressDTO;
import com.mycoffeestore.dto.order.OrderCreateDTO;
import com.mycoffeestore.dto.order.OrderItemDTO;
import com.mycoffeestore.entity.*;
import com.mycoffeestore.enums.OrderStatus;
import com.mycoffeestore.enums.OrderType;
import com.mycoffeestore.exception.BusinessException;
import com.mycoffeestore.mapper.*;
import com.mycoffeestore.service.order.OrderService;
import com.mycoffeestore.vo.order.OrderDetailVO;
import com.mycoffeestore.vo.order.OrderItemDetailVO;
import com.mycoffeestore.vo.order.OrderListItemVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 *
 * @author Backend Developer
 * @since 2024-02-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CoffeeMapper coffeeMapper;
    private final UserMapper userMapper;
    private final CartMapper cartMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public OrderDetailVO create(Long userId, OrderCreateDTO dto) {
        // 验证外卖订单是否填写配送地址
        if (dto.getOrderType() == OrderType.DELIVERY && dto.getDeliveryAddress() == null) {
            throw new BusinessException(400, "外卖订单必须填写配送地址");
        }

        // 计算订单总金额并验证库存
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemDTO item : dto.getItems()) {
            Coffee coffee = coffeeMapper.selectOneById(item.getCoffeeId());
            if (coffee == null) {
                throw new BusinessException(2001, "咖啡不存在: " + item.getCoffeeId());
            }
            if (coffee.getStatus() != 1) {
                throw new BusinessException(2003, "咖啡已下架: " + coffee.getName());
            }
            if (coffee.getStock() < item.getQuantity()) {
                throw new BusinessException(2002, "咖啡库存不足: " + coffee.getName());
            }

            totalAmount = totalAmount.add(coffee.getPrice().multiply(new BigDecimal(item.getQuantity())));
        }

        // 生成订单号
        String orderNo = generateOrderNo();

        // 转换配送地址为JSON
        String deliveryAddressJson = null;
        if (dto.getDeliveryAddress() != null) {
            try {
                DeliveryAddress address = new DeliveryAddress();
                BeanUtils.copyProperties(dto.getDeliveryAddress(), address);
                deliveryAddressJson = objectMapper.writeValueAsString(address);
            } catch (Exception e) {
                log.error("配送地址序列化失败", e);
                throw new BusinessException(500, "配送地址格式错误");
            }
        }

        // 创建订单
        Order order = Order.builder()
                .orderNo(orderNo)
                .userId(userId)
                .totalAmount(totalAmount)
                .orderType(dto.getOrderType().getCode())
                .status(OrderStatus.PENDING.getCode())
                .remark(dto.getRemark())
                .deliveryAddress(deliveryAddressJson)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        orderMapper.insert(order);

        // 创建订单详情并扣减库存
        for (OrderItemDTO item : dto.getItems()) {
            Coffee coffee = coffeeMapper.selectOneById(item.getCoffeeId());

            OrderItem orderItem = OrderItem.builder()
                    .orderId(order.getId())
                    .coffeeId(coffee.getId())
                    .coffeeName(coffee.getName())
                    .imageUrl(coffee.getImageUrl())
                    .quantity(item.getQuantity())
                    .price(coffee.getPrice())
                    .subtotal(coffee.getPrice().multiply(new BigDecimal(item.getQuantity())))
                    .createTime(LocalDateTime.now())
                    .build();

            orderItemMapper.insert(orderItem);

            // 扣减库存
            coffee.setStock(coffee.getStock() - item.getQuantity());
            coffeeMapper.update(coffee);
        }

        // 清空购物车（如果是直接从购物车下单）
        cartMapper.deleteByQuery(QueryWrapper.create().eq(Cart::getUserId, userId));

        return getDetailVO(order);
    }

    @Override
    public OrderDetailVO detail(Long userId, String orderNo) {
        Order order = orderMapper.selectOneByQuery(QueryWrapper.create()
                .eq(Order::getOrderNo, orderNo)
                .eq(Order::getIsDeleted, 0));

        if (order == null) {
            throw new BusinessException(4001, "订单不存在");
        }

        // 验证是否属于当前用户
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权限查看该订单");
        }

        return getDetailVO(order);
    }

    @Override
    public PageResult<OrderListItemVO> list(Long userId, OrderStatus status, Integer page, Integer size) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(Order::getUserId, userId)
                .eq(Order::getIsDeleted, 0)
                .orderBy(Order::getCreateTime, false);

        if (status != null) {
            queryWrapper.eq(Order::getStatus, status.getCode());
        }

        Page<Order> pageResult = orderMapper.paginate(page, size, queryWrapper);

        List<OrderListItemVO> voList = pageResult.getRecords().stream()
                .map(this::convertToListItemVO)
                .collect(Collectors.toList());

        return PageResult.<OrderListItemVO>builder()
                .total(pageResult.getTotalRow())
                .page(page)
                .size(size)
                .list(voList)
                .build();
    }

    @Override
    @Transactional
    public void cancel(Long userId, String orderNo, String reason) {
        Order order = orderMapper.selectOneByQuery(QueryWrapper.create()
                .eq(Order::getOrderNo, orderNo)
                .eq(Order::getIsDeleted, 0));

        if (order == null) {
            throw new BusinessException(4001, "订单不存在");
        }

        // 验证是否属于当前用户
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权限操作该订单");
        }

        // 检查订单状态
        if (order.getStatus().equals(OrderStatus.COMPLETED.getCode()) ||
            order.getStatus().equals(OrderStatus.CANCELLED.getCode())) {
            throw new BusinessException(4002, "订单状态不允许取消");
        }

        // 更新订单状态
        order.setStatus(OrderStatus.CANCELLED.getCode());
        order.setCancelReason(reason);
        order.setCancelledAt(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.update(order);

        // 恢复库存
        List<OrderItem> items = orderItemMapper.selectListByQuery(
                QueryWrapper.create().eq(OrderItem::getOrderId, order.getId()));

        for (OrderItem item : items) {
            Coffee coffee = coffeeMapper.selectOneById(item.getCoffeeId());
            if (coffee != null) {
                coffee.setStock(coffee.getStock() + item.getQuantity());
                coffeeMapper.update(coffee);
            }
        }
    }

    @Override
    @Transactional
    public void updateStatus(String orderNo, OrderStatus status) {
        Order order = orderMapper.selectOneByQuery(QueryWrapper.create()
                .eq(Order::getOrderNo, orderNo)
                .eq(Order::getIsDeleted, 0));

        if (order == null) {
            throw new BusinessException(4001, "订单不存在");
        }

        order.setStatus(status.getCode());
        order.setUpdateTime(LocalDateTime.now());

        if (status == OrderStatus.COMPLETED) {
            order.setCompletedAt(LocalDateTime.now());
        }

        orderMapper.update(order);
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        Random random = new Random();
        int randomNum = random.nextInt(10000);
        return "ORD" + timestamp + String.format("%04d", randomNum);
    }

    /**
     * 转换为列表项VO
     */
    private OrderListItemVO convertToListItemVO(Order order) {
        // 查询订单项数量
        long itemCount = orderItemMapper.selectCountByQuery(
                QueryWrapper.create().eq(OrderItem::getOrderId, order.getId()));

        return OrderListItemVO.builder()
                .orderId(order.getOrderNo())
                .totalAmount(order.getTotalAmount())
                .orderType(order.getOrderType())
                .orderTypeName(OrderType.fromCode(order.getOrderType()).getName())
                .status(order.getStatus())
                .statusName(OrderStatus.fromCode(order.getStatus()).getName())
                .itemCount((int) itemCount)
                .createTime(order.getCreateTime())
                .build();
    }

    /**
     * 获取订单详情VO
     */
    private OrderDetailVO getDetailVO(Order order) {
        // 查询用户信息
        User user = userMapper.selectOneById(order.getUserId());

        // 查询订单项
        List<OrderItem> items = orderItemMapper.selectListByQuery(
                QueryWrapper.create().eq(OrderItem::getOrderId, order.getId()));

        List<OrderItemDetailVO> itemVOs = items.stream()
                .map(item -> OrderItemDetailVO.builder()
                        .itemId(item.getId())
                        .coffeeId(item.getCoffeeId())
                        .coffeeName(item.getCoffeeName())
                        .imageUrl(item.getImageUrl())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return OrderDetailVO.builder()
                .orderId(order.getOrderNo())
                .userId(order.getUserId())
                .username(user != null ? user.getUsername() : "")
                .totalAmount(order.getTotalAmount())
                .orderType(order.getOrderType())
                .orderTypeName(OrderType.fromCode(order.getOrderType()).getName())
                .status(order.getStatus())
                .statusName(OrderStatus.fromCode(order.getStatus()).getName())
                .remark(order.getRemark())
                .items(itemVOs)
                .createTime(order.getCreateTime())
                .updateTime(order.getUpdateTime())
                .build();
    }
}

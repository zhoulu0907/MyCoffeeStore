package com.mycoffeestore.util;

import com.mycoffeestore.entity.*;
import com.mycoffeestore.enums.OrderStatus;
import com.mycoffeestore.enums.OrderType;
import com.mycoffeestore.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.mybatisflex.core.query.QueryWrapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Random;

import static com.mycoffeestore.entity.table.OrderTableDef.ORDER;

/**
 * 订单历史数据生成器
 * 为用户生成500天的历史订单数据
 *
 * @author zhoulu
 * @since 2026-02-26
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderDataGenerator {

    private final UserMapper userMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CoffeeMapper coffeeMapper;

    /**
     * 运行数据生成
     * @param forceClean 是否强制清空现有数据
     */
    public void run(boolean forceClean) {
        log.info("开始生成历史订单数据...");

        // 查找 zhoulu 用户
        User user = userMapper.selectOneByQuery(
                QueryWrapper.create().eq(User::getUsername, "zhoulu")
        );

        if (user == null) {
            log.error("未找到用户 zhoulu");
            return;
        }

        // 检查是否已有订单数据
        long existingOrders = orderMapper.selectCountByQuery(
                QueryWrapper.create().eq(Order::getUserId, user.getId())
        );

        if (existingOrders > 0) {
            if (forceClean) {
                // 清空现有订单数据
                log.info("清空现有订单数据...");
                orderItemMapper.deleteByQuery(
                        QueryWrapper.create().in(OrderItem::getOrderId,
                                orderMapper.selectListByQuery(
                                        QueryWrapper.create().eq(Order::getUserId, user.getId())
                                ).stream().map(Order::getId).toList()
                        )
                );
                orderMapper.deleteByQuery(QueryWrapper.create().eq(Order::getUserId, user.getId()));
                log.info("已清空 {} 条订单数据", existingOrders);
            } else {
                log.info("用户已有 {} 条订单数据，跳过生成（使用 forceClean=true 强制重新生成）", existingOrders);
                return;
            }
        }

        generateOrders(user);

        log.info("历史订单数据生成完成！");
    }

    /**
     * 默认运行（不强制清空）
     */
    public void run() {
        run(false);
    }

    /**
     * 生成订单数据
     */
    private void generateOrders(User user) {
        Random random = new Random();
        LocalDate today = LocalDate.now();
        int totalOrders = 0;
        int totalAmount = 0;

        // 从500天前开始生成
        for (int dayOffset = 500; dayOffset >= 0; dayOffset--) {
            LocalDate date = today.minusDays(dayOffset);
            int dayOfWeek = date.getDayOfWeek().getValue(); // 1=周一, 7=周日

            // 判断是否周末
            boolean isWeekend = (dayOfWeek == 6 || dayOfWeek == 7);

            // 判断是否夏天（6-8月）
            boolean isSummer = (date.getMonthValue() >= 6 && date.getMonthValue() <= 8);

            // 确定当天订单数量（每天超过3单）
            int orderCount;
            if (isWeekend) {
                // 周末：2-4单（较少但仍有订单）
                double rand = random.nextDouble();
                if (rand < 0.4) {
                    orderCount = 2;  // 40% 概率2单
                } else if (rand < 0.8) {
                    orderCount = 3;  // 40% 概率3单
                } else {
                    orderCount = 4;  // 20% 概率4单
                }
            } else {
                // 工作日：4-6单，中午较多
                double rand = random.nextDouble();
                if (rand < 0.25) {
                    orderCount = 4;  // 25% 概率4单
                } else if (rand < 0.65) {
                    orderCount = 5;  // 40% 概率5单
                } else {
                    orderCount = 6;  // 35% 概率6单
                }
            }

            // 生成当天的订单
            for (int i = 0; i < orderCount; i++) {
                Order order = createOrder(user, date, i, orderCount, isSummer, random);
                orderMapper.insert(order);

                // 创建订单项
                OrderItem item = createOrderItem(order, date, i, orderCount, isSummer, random);
                orderItemMapper.insert(item);

                // 更新订单总金额
                order.setTotalAmount(item.getSubtotal());
                orderMapper.update(order);

                totalOrders++;
                totalAmount += item.getSubtotal().intValue();
            }

            // 每50天输出一次进度
            if ((500 - dayOffset) % 50 == 0 && dayOffset < 500) {
                log.info("已生成 {} 天的数据，共 {} 单，金额 {} 元",
                        500 - dayOffset + 1, totalOrders, totalAmount);
            }
        }

        // 更新用户余额（假设最初有足够的余额）
        user.setBalance(new BigDecimal(10000));
        userMapper.update(user);

        log.info("========================================");
        log.info("订单生成完成！");
        log.info("总订单数: {}", totalOrders);
        log.info("总金额: {} 元", totalAmount);
        log.info("平均每天: {} 单", String.format("%.2f", (double) totalOrders / 501));
        log.info("========================================");
    }

    /**
     * 创建订单
     */
    private Order createOrder(User user, LocalDate date, int orderIndex, int orderCount,
                              boolean isSummer, Random random) {
        // 生成订单时间（根据订单数量和索引分配）
        LocalTime orderTime = generateOrderTime(orderIndex, orderCount, random);

        LocalDateTime orderDateTime = LocalDateTime.of(date, orderTime);

        // 生成订单号
        String orderNo = "ORD" + orderDateTime.format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        ) + String.format("%04d", random.nextInt(10000));

        // 订单状态：较早的订单已完成，最近的订单各种状态
        OrderStatus status;
        long daysAgo = java.time.temporal.ChronoUnit.DAYS.between(date, LocalDate.now());
        if (daysAgo > 30) {
            status = OrderStatus.COMPLETED;
        } else if (daysAgo > 7) {
            // 最近30天：随机状态
            double rand = random.nextDouble();
            if (rand < 0.7) {
                status = OrderStatus.COMPLETED;
            } else if (rand < 0.85) {
                status = OrderStatus.CANCELLED;
            } else if (rand < 0.95) {
                status = OrderStatus.PREPARING;
            } else {
                status = OrderStatus.PENDING;
            }
        } else {
            // 最近7天：各种状态
            double rand = random.nextDouble();
            if (rand < 0.4) {
                status = OrderStatus.COMPLETED;
            } else if (rand < 0.6) {
                status = OrderStatus.CANCELLED;
            } else if (rand < 0.75) {
                status = OrderStatus.PREPARING;
            } else if (rand < 0.9) {
                status = OrderStatus.CONFIRMED;
            } else {
                status = OrderStatus.PENDING;
            }
        }

        return Order.builder()
                .orderNo(orderNo)
                .userId(user.getId())
                .totalAmount(BigDecimal.ZERO)
                .orderType(OrderType.DINE_IN.getCode())
                .status(status.getCode())
                .remark(generateRemark(random))
                .paidAt(orderDateTime)
                .createTime(orderDateTime)
                .updateTime(orderDateTime)
                .build();
    }

    /**
     * 创建订单项
     */
    private OrderItem createOrderItem(Order order, LocalDate date, int orderIndex,
                                       int orderCount, boolean isSummer, Random random) {
        // 选择咖啡
        Coffee coffee;
        LocalTime orderTime = order.getCreateTime().toLocalTime();
        int hour = orderTime.getHour();

        if (hour < 11) {
            // 早上：拿铁为主
            coffee = selectMorningCoffee(random);
        } else if (hour >= 11 && hour < 15) {
            // 中午：美式/冰美式
            coffee = selectNoonCoffee(isSummer, random);
        } else {
            // 下午：随机选择
            coffee = selectAfternoonCoffee(random);
        }

        // 数量：1-2杯
        int quantity = random.nextDouble() < 0.8 ? 1 : 2;
        BigDecimal subtotal = coffee.getPrice().multiply(new BigDecimal(quantity));

        return OrderItem.builder()
                .orderId(order.getId())
                .coffeeId(coffee.getId())
                .coffeeName(coffee.getName())
                .imageUrl(coffee.getImageUrl())
                .quantity(quantity)
                .price(coffee.getPrice())
                .subtotal(subtotal)
                .createTime(order.getCreateTime())
                .build();
    }

    /**
     * 选择早上咖啡（拿铁为主）
     */
    private Coffee selectMorningCoffee(Random random) {
        // 70% 拿铁，30% 卡布奇诺
        long coffeeId = random.nextDouble() < 0.7 ? 3 : 2;
        return coffeeMapper.selectOneById(coffeeId);
    }

    /**
     * 选择中午咖啡（美式/冰美式）
     */
    private Coffee selectNoonCoffee(boolean isSummer, Random random) {
        if (isSummer) {
            // 夏天：60% 冰美式，30% 冷萃，10% 美式
            double rand = random.nextDouble();
            if (rand < 0.6) {
                return coffeeMapper.selectOneById(10L); // 冰美式
            } else if (rand < 0.9) {
                return coffeeMapper.selectOneById(9L);  // 冷萃
            } else {
                return coffeeMapper.selectOneById(1L);  // 美式
            }
        } else {
            // 非夏天：50% 美式，30% 拿铁，20% 冰美式
            double rand = random.nextDouble();
            if (rand < 0.5) {
                return coffeeMapper.selectOneById(1L);  // 美式
            } else if (rand < 0.8) {
                return coffeeMapper.selectOneById(3L);  // 拿铁
            } else {
                return coffeeMapper.selectOneById(10L); // 冰美式
            }
        }
    }

    /**
     * 选择下午咖啡
     */
    private Coffee selectAfternoonCoffee(Random random) {
        Coffee[] options = {coffeeMapper.selectOneById(1L),  // 美式
                            coffeeMapper.selectOneById(3L),  // 拿铁
                            coffeeMapper.selectOneById(6L),  // 耶加雪菲
                            coffeeMapper.selectOneById(9L)}; // 冷萃
        return options[random.nextInt(options.length)];
    }

    /**
     * 生成订单时间
     * 根据订单索引和数量合理分配时间
     */
    private LocalTime generateOrderTime(int orderIndex, int orderCount, Random random) {
        return switch (orderCount) {
            case 2 -> generateTimeFor2Orders(orderIndex, random);
            case 3 -> generateTimeFor3Orders(orderIndex, random);
            case 4 -> generateTimeFor4Orders(orderIndex, random);
            case 5 -> generateTimeFor5Orders(orderIndex, random);
            default -> generateTimeFor6Orders(orderIndex, random);
        };
    }

    /**
     * 2单时间分配
     */
    private LocalTime generateTimeFor2Orders(int orderIndex, Random random) {
        return switch (orderIndex) {
            case 0 -> LocalTime.of(8 + random.nextInt(2), random.nextInt(60));  // 早上8-10点
            default -> LocalTime.of(13 + random.nextInt(2), random.nextInt(60)); // 中午13-15点
        };
    }

    /**
     * 3单时间分配
     */
    private LocalTime generateTimeFor3Orders(int orderIndex, Random random) {
        return switch (orderIndex) {
            case 0 -> LocalTime.of(8 + random.nextInt(2), random.nextInt(60));   // 早上8-10点
            case 1 -> LocalTime.of(12 + random.nextInt(2), random.nextInt(60));  // 中午12-14点
            default -> LocalTime.of(15 + random.nextInt(2), random.nextInt(60));  // 下午15-17点
        };
    }

    /**
     * 4单时间分配
     */
    private LocalTime generateTimeFor4Orders(int orderIndex, Random random) {
        return switch (orderIndex) {
            case 0 -> LocalTime.of(8 + random.nextInt(1), random.nextInt(60));   // 早上8-9点
            case 1 -> LocalTime.of(10 + random.nextInt(1), random.nextInt(60));  // 上午10-11点
            case 2 -> LocalTime.of(13 + random.nextInt(2), random.nextInt(60));  // 中午13-15点
            default -> LocalTime.of(16 + random.nextInt(2), random.nextInt(60));  // 下午16-18点
        };
    }

    /**
     * 5单时间分配
     */
    private LocalTime generateTimeFor5Orders(int orderIndex, Random random) {
        return switch (orderIndex) {
            case 0 -> LocalTime.of(8, random.nextInt(30));                       // 早上8-8:30
            case 1 -> LocalTime.of(9 + random.nextInt(1), random.nextInt(60));   // 上午9-10点
            case 2 -> LocalTime.of(12 + random.nextInt(2), random.nextInt(60));  // 中午12-14点
            case 3 -> LocalTime.of(14 + random.nextInt(2), random.nextInt(60));  // 下午14-16点
            default -> LocalTime.of(17 + random.nextInt(1), random.nextInt(60));  // 下午17-18点
        };
    }

    /**
     * 6单时间分配（工作日高峰）
     */
    private LocalTime generateTimeFor6Orders(int orderIndex, Random random) {
        return switch (orderIndex) {
            case 0 -> LocalTime.of(8, random.nextInt(30));                       // 早上8-8:30
            case 1 -> LocalTime.of(9, random.nextInt(60));                       // 上午9-10点
            case 2 -> LocalTime.of(11, random.nextInt(60));                       // 上午11-12点
            case 3 -> LocalTime.of(13, random.nextInt(60));                       // 下午13-14点
            case 4 -> LocalTime.of(14 + random.nextInt(1), random.nextInt(60));  // 下午14-15点
            default -> LocalTime.of(16 + random.nextInt(2), random.nextInt(60));  // 下午16-18点
        };
    }

    /**
     * 生成备注
     */
    private String generateRemark(Random random) {
        String[] remarks = {
            "", "", "", "",  // 大部分无备注
            "少冰",
            "少糖",
            "多加一份浓缩",
            "请用吸管",
            "尽快做好，谢谢"
        };
        return remarks[random.nextInt(remarks.length)];
    }
}

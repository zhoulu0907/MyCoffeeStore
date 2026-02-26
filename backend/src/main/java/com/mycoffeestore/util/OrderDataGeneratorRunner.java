package com.mycoffeestore.util;

import com.mycoffeestore.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.mybatisflex.core.query.QueryWrapper;

/**
 * 订单数据生成启动器
 * 通过环境变量控制是否生成数据
 *
 * @author zhoulu
 * @since 2026-02-26
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderDataGeneratorRunner implements CommandLineRunner {

    private final OrderDataGenerator orderDataGenerator;
    private final UserMapper userMapper;

    @Override
    public void run(String... args) throws Exception {
        // 检查环境变量是否启用数据生成
        String generateData = System.getProperty("generate.data", "false");
        if ("true".equals(generateData)) {
            log.info("检测到 generate.data=true，开始生成历史订单数据...");

            // 等待数据库连接稳定
            Thread.sleep(2000);

            // 检查用户是否存在
            Long userCount = userMapper.selectCountByQuery(
                    QueryWrapper.create().eq("username", "zhoulu")
            );

            if (userCount > 0) {
                orderDataGenerator.run(true);
            } else {
                log.warn("用户 zhoulu 不存在，跳过数据生成");
            }
        }
    }
}

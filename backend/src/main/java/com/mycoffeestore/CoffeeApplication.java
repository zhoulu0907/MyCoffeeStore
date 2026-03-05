package com.mycoffeestore;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MyCoffeeStore 应用启动类
 *
 * @author Backend Developer
 * @since 2024-02-26
 */
@SpringBootApplication(exclude = {
        com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeAutoConfiguration.class
})
@MapperScan("com.mycoffeestore.mapper")
public class CoffeeApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoffeeApplication.class, args);
    }
}

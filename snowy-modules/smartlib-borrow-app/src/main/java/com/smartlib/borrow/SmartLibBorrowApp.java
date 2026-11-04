package com.smartlib.borrow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * SmartLib 借阅应用启动类
 */
@Slf4j
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.smartlib", "vip.xiaonuo"})
@SpringBootApplication(scanBasePackages = {"com.smartlib.borrow", "vip.xiaonuo.common.cache"})
public class SmartLibBorrowApp {

    public static void main(String[] args) {
        SpringApplication.run(SmartLibBorrowApp.class, args);
        log.info(">>> {}", SmartLibBorrowApp.class.getSimpleName().toUpperCase() + " STARTING SUCCESS");
    }
}

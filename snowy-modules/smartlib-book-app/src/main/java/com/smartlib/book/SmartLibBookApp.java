package com.smartlib.book;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * SmartLib 图书应用启动类
 */
@Slf4j
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.smartlib")
@SpringBootApplication(scanBasePackages = {"com.smartlib.book", "vip.xiaonuo.common.cache"})
public class SmartLibBookApp {

    public static void main(String[] args) {
        SpringApplication.run(SmartLibBookApp.class, args);
        log.info(">>> {}", SmartLibBookApp.class.getSimpleName().toUpperCase() + " STARTING SUCCESS");
    }
}

package com.smartlib.borrow.core.config;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusPropertiesCustomizer;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import vip.xiaonuo.common.enums.CommonDeleteFlagEnum;
import vip.xiaonuo.common.interceptor.CommonTraceInterceptor;

/**
 * 借阅服务全局配置。
 */
@Configuration(proxyBeanMethods = false)
@MapperScan("com.smartlib.borrow.modular.*.mapper")
public class GlobalConfigure implements WebMvcConfigurer {

    /** 与项目其他服务一致，启用 MyBatis-Plus 分页插件。 */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }

    /** 沿用框架的字符串逻辑删除标志，与 CommonEntity 保持一致。 */
    @Bean
    public MybatisPlusPropertiesCustomizer borrowMybatisPlusPropertiesCustomizer() {
        return properties -> {
            properties.getGlobalConfig().getDbConfig()
                    .setLogicNotDeleteValue(CommonDeleteFlagEnum.NOT_DELETE.name())
                    .setLogicDeleteValue(CommonDeleteFlagEnum.DELETED.name());
        };
    }

    /** 注册框架已有的请求链路追踪，统一生成或沿用 traceId。 */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CommonTraceInterceptor()).addPathPatterns("/**");
    }
}

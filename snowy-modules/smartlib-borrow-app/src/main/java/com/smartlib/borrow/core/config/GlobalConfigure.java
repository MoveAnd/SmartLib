package com.smartlib.borrow.core.config;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusPropertiesCustomizer;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import vip.xiaonuo.common.enums.CommonDeleteFlagEnum;
import vip.xiaonuo.common.interceptor.CommonTraceInterceptor;
import vip.xiaonuo.auth.core.util.StpClientUtil;
import vip.xiaonuo.common.pojo.CommonEntity;

import java.util.Date;

/**
 * 借阅服务全局配置。
 */
@Configuration(proxyBeanMethods = false)
@MapperScan("com.smartlib.borrow.modular.*.mapper")
public class GlobalConfigure implements WebMvcConfigurer {

    /** 公共审计字段填充规则。 */
    @Component
    public static class CustomMetaObjectHandler implements MetaObjectHandler {

        @Override
        public void insertFill(MetaObject metaObject) {
            if (!(metaObject.getOriginalObject() instanceof CommonEntity)) {
                return;
            }
            strictInsertFill(metaObject, "deleteFlag", String.class, CommonDeleteFlagEnum.NOT_DELETE.name());
            strictInsertFill(metaObject, "createTime", Date.class, new Date());
            strictInsertFill(metaObject, "createUser", String.class, getUserId());
        }

        @Override
        public void updateFill(MetaObject metaObject) {
            if (!(metaObject.getOriginalObject() instanceof CommonEntity)) {
                return;
            }
            setFieldValByName("updateUser", getUserId(), metaObject);
            setFieldValByName("updateTime", new Date(), metaObject);
        }

        private String getUserId() {
            try {
                String loginId;
                try {
                    loginId = StpUtil.getLoginIdAsString();
                } catch (Exception e) {
                    loginId = StpClientUtil.getLoginIdAsString();
                }
                return StrUtil.isNotEmpty(loginId) ? loginId : "-1";
            } catch (Exception e) {
                return "-1";
            }
        }
    }

    /** 启用 MyBatis-Plus 乐观锁及分页插件。 */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
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

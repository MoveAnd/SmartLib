package com.smartlib.book.core.config;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusPropertiesCustomizer;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import vip.xiaonuo.auth.core.util.StpClientUtil;
import vip.xiaonuo.common.enums.CommonDeleteFlagEnum;
import vip.xiaonuo.common.pojo.CommonEntity;

import java.util.Date;

/**
 * 图书服务全局配置。
 */
@Configuration(proxyBeanMethods = false)
@MapperScan("com.smartlib.book.modular.*.mapper")
public class GlobalConfigure {

    /** 按框架约定填充公共审计字段，未获取到登录身份时使用-1。 */
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

    /** 与项目其他服务一致，启用MyBatis-Plus分页插件。 */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }

    /** 图书导入数据使用框架的字符串删除标志，避免默认0/1与数据不匹配。 */
    @Bean
    public MybatisPlusPropertiesCustomizer bookMybatisPlusPropertiesCustomizer() {
        return properties -> {
            properties.getGlobalConfig().getDbConfig()
                    .setLogicNotDeleteValue(CommonDeleteFlagEnum.NOT_DELETE.name())
                    .setLogicDeleteValue(CommonDeleteFlagEnum.DELETED.name());
        };
    }
}

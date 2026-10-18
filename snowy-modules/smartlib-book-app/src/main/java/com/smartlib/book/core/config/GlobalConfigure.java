package com.smartlib.book.core.config;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.context.model.SaResponse;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.router.SaHttpMethod;
import cn.dev33.satoken.router.SaRouter;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusPropertiesCustomizer;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.annotation.Resource;
import org.apache.ibatis.reflection.MetaObject;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import vip.xiaonuo.auth.core.util.StpClientUtil;
import com.smartlib.book.core.handler.GlobalExceptionUtil;
import vip.xiaonuo.common.enums.CommonDeleteFlagEnum;
import vip.xiaonuo.common.interceptor.CommonTraceInterceptor;
import vip.xiaonuo.common.pojo.CommonEntity;
import vip.xiaonuo.common.pojo.CommonResult;
import vip.xiaonuo.common.util.CommonServletUtil;
import vip.xiaonuo.common.util.CommonTraceIdUtil;

import java.util.Date;

/**
 * 图书服务全局配置。
 */
@Configuration(proxyBeanMethods = false)
@MapperScan("com.smartlib.book.modular.*.mapper")
public class GlobalConfigure implements WebMvcConfigurer {

    @Resource
    private SaTokenConfig saTokenConfig;

    /** 无需登录的接口地址集合。 */
    public static final String[] NO_LOGIN_PATH_ARR = {
            /* 主入口 */
            "/",

            /* 静态资源与接口文档 */
            "/favicon.ico",
            "/doc.html",
            "/webjars/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",

            /* 健康检查 */
            "/actuator/health",
            "/actuator/health/**",

            /* 公开书目查询 */
            "/book/work/page",
            "/book/work/detail",
            "/book/author/page",
            "/book/author/detail",
            "/book/edition/page",
            "/book/edition/detail",
            "/book/edition/byIsbn"
    };

    /** B端要排除的，相当于C端要认证的。 */
    private static final String[] CLIENT_USER_PERMISSION_PATH_ARR = {
            "/book/c/**"
    };

    /** 沿用Biz的登录过滤器，公开查询之外按路径分别校验B、C端登录。 */
    @Bean
    public SaServletFilter getSaServletFilter() {
        return new SaServletFilter()
                .addInclude("/**")
                .setAuth(r -> {
                    // B端的接口校验B端登录。
                    SaRouter.match("/**")
                            .notMatch(CollectionUtil.newArrayList(NO_LOGIN_PATH_ARR))
                            .notMatch(CollectionUtil.newArrayList(CLIENT_USER_PERMISSION_PATH_ARR))
                            .check(r1 -> {
                                StpUtil.checkLogin();
                                StpUtil.renewTimeout(saTokenConfig.getTimeout());
                            });

                    // C端的接口校验C端登录。
                    SaRouter.match("/**")
                            .notMatch(CollectionUtil.newArrayList(NO_LOGIN_PATH_ARR))
                            .match(CollectionUtil.newArrayList(CLIENT_USER_PERMISSION_PATH_ARR))
                            .check(r1 -> StpClientUtil.checkLogin());
                })
                .setBeforeAuth(obj -> {
                    SaHolder.getResponse()
                            .setHeader("X-XSS-Protection", "1; mode=block")
                            .setHeader("X-Content-Type-Options", "nosniff")
                            .setHeader("Access-Control-Allow-Origin", "*")
                            .setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE")
                            .setHeader("Access-Control-Max-Age", "3600")
                            .setHeader("Access-Control-Allow-Headers", "*");
                    SaRouter.match(SaHttpMethod.OPTIONS).free(r -> {}).back();
                })
                .setError(e -> {
                    SaResponse response = SaHolder.getResponse();
                    response.setHeader(Header.CONTENT_TYPE.getValue(), ContentType.JSON + ";charset=" + CharsetUtil.UTF_8);
                    CommonResult<String> result = GlobalExceptionUtil.getCommonResult((Exception) e);
                    // 过滤器早于MVC链路追踪拦截器执行，拒绝请求也需要traceId。
                    result.setTraceId(CommonTraceIdUtil.generateTraceId(CommonServletUtil.getRequest()));
                    Gson gson = new GsonBuilder().serializeNulls().create();
                    return gson.toJson(result);
                });
    }

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

    /** 注册请求链路追踪，统一生成或沿用traceId。 */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CommonTraceInterceptor()).addPathPatterns("/**");
    }
}

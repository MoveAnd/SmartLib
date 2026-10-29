package com.smartlib.borrow.core.interceptor;

import cn.hutool.core.util.StrUtil;
import cn.dev33.satoken.stp.StpUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import vip.xiaonuo.common.consts.AppConstant;
import vip.xiaonuo.common.util.CommonTraceIdUtil;

/**
 * 为 Feign 出站请求传递当前请求的登录凭证与链路标识。
 */
@Configuration(proxyBeanMethods = false)
public class FeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        if (RequestContextHolder.getRequestAttributes() != null) {
            String token = StpUtil.getTokenValueNotCut();
            if (StrUtil.isNotBlank(token)) {
                requestTemplate.removeHeader(StpUtil.getTokenName());
                requestTemplate.header(StpUtil.getTokenName(), token);
            }
        }
        String traceId = CommonTraceIdUtil.getTraceId();
        if (StrUtil.isNotBlank(traceId)) {
            requestTemplate.removeHeader(AppConstant.TRACE_ID_STRING);
            requestTemplate.header(AppConstant.TRACE_ID_STRING, traceId);
        }
    }
}

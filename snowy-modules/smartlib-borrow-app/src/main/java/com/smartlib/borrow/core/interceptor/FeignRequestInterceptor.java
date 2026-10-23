package com.smartlib.borrow.core.interceptor;

import cn.hutool.core.util.StrUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;
import vip.xiaonuo.common.consts.AppConstant;
import vip.xiaonuo.common.util.CommonTraceIdUtil;

/**
 * 为 Feign 出站请求传递当前请求的链路标识。
 */
@Configuration(proxyBeanMethods = false)
public class FeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String traceId = CommonTraceIdUtil.getTraceId();
        if (StrUtil.isNotBlank(traceId)) {
            requestTemplate.removeHeader(AppConstant.TRACE_ID_STRING);
            requestTemplate.header(AppConstant.TRACE_ID_STRING, traceId);
        }
    }
}

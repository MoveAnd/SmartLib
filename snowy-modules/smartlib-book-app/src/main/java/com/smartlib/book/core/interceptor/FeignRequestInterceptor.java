package com.smartlib.book.core.interceptor;

import cn.hutool.core.util.StrUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import vip.xiaonuo.common.consts.AppConstant;
import vip.xiaonuo.common.util.CommonTraceIdUtil;

import java.util.Enumeration;
import java.util.Optional;

@Slf4j
@Configuration
public class FeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            Enumeration<String> headerNames = request.getHeaderNames();
            Optional.ofNullable(headerNames).ifPresent(headers -> {
                while (headers.hasMoreElements()) {
                    String name = headers.nextElement();
                    if (name.equalsIgnoreCase(org.apache.seata.core.context.RootContext.KEY_XID)) continue;
                    String value = request.getHeader(name);

                    // 跳过 content-length
                    if (name.equals("content-length")){
                        continue;
                    }

                    requestTemplate.header(name, value);
                    log.info(">>> feign header set key:{}, value:{}", name, value);
                }
            });
        }

        // add traceId
        String traceId = CommonTraceIdUtil.getTraceId();
        if(StrUtil.isNotBlank(traceId)){
            requestTemplate.header(AppConstant.TRACE_ID_STRING, traceId);
            log.info(">>> feign header traceId set key:{}, value:{}", AppConstant.TRACE_ID_STRING, traceId);
        }
    }
}

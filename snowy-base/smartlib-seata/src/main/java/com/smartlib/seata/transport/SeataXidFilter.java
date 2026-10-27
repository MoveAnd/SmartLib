package com.smartlib.seata.transport;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.seata.core.context.RootContext;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/** 请求线程显式绑定和恢复 XID；业务鉴权仍由各应用原有拦截器执行。 */
public class SeataXidFilter extends OncePerRequestFilter {
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        var values = Collections.list(request.getHeaders(RootContext.KEY_XID));
        String xid = request.getHeader(RootContext.KEY_XID);
        if (values.size() > 1 || (xid != null && !xid.matches("[\\x21-\\x7e]{1,128}"))) {
            response.sendError(400, "Invalid transaction context");
            return;
        }
        String previous = RootContext.unbind();
        try {
            if (xid != null) RootContext.bind(xid);
            chain.doFilter(request, response);
        } finally {
            RootContext.unbind();
            if (previous != null) RootContext.bind(previous);
        }
    }
}

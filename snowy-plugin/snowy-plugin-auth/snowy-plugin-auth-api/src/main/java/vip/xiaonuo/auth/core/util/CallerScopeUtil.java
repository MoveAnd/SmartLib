package vip.xiaonuo.auth.core.util;

import cn.dev33.satoken.stp.StpUtil;
import vip.xiaonuo.auth.core.consts.CallerScopeConstant;

/** 从当前登录态生成调用者范围，未登录时由对应身份工具拒绝请求。 */
public final class CallerScopeUtil {

    private CallerScopeUtil() {
    }

    /** 获取当前已登录读者的调用范围。 */
    public static String currentReaderScope() {
        return CallerScopeConstant.READER_SCOPE_PREFIX + StpClientUtil.getLoginIdAsString();
    }

    /** 获取当前已登录管理员的调用范围。 */
    public static String currentAdminScope() {
        return CallerScopeConstant.ADMIN_SCOPE_PREFIX + StpUtil.getLoginIdAsString();
    }
}

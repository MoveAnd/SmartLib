package vip.xiaonuo.client.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vip.xiaonuo.common.consts.FeignConstant;

/** C端用户Feign。 */
@FeignClient(name = FeignConstant.WEB_APP, contextId = "ClientUserFeign")
public interface ClientUserFeign {

    /**
     * 根据C端用户id获取用户JSON，不存在或已逻辑删除时返回JSON null。
     * 用户状态由Borrow判断；参数或查询异常沿用项目异常处理。
     */
    @RequestMapping("/feign/client/user/getUserByIdWithoutException")
    String getUserByIdWithoutException(@RequestParam("userId") String userId);
}

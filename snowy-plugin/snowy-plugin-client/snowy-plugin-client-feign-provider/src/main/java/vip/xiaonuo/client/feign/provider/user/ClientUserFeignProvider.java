package vip.xiaonuo.client.feign.provider.user;

import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vip.xiaonuo.client.ClientUserApi;
import vip.xiaonuo.client.feign.ClientUserFeign;

/** C端用户Feign提供者。 */
@RequiredArgsConstructor
@RestController
public class ClientUserFeignProvider implements ClientUserFeign {

    private final ClientUserApi clientUserApi;

    @Override
    @RequestMapping("/feign/client/user/getUserByIdWithoutException")
    public String getUserByIdWithoutException(@RequestParam("userId") String userId) {
        return JSONUtil.toJsonStr(clientUserApi.getUserByIdWithoutException(userId));
    }
}

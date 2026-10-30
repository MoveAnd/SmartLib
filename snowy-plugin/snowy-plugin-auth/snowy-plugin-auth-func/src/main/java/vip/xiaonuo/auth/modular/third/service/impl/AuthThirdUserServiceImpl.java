/*
 * Copyright [2022] [https://www.xiaonuo.vip]
 *
 * Snowy采用APACHE LICENSE 2.0开源协议，您在使用过程中，需要注意以下几点：
 *
 * 1.请不要删除和修改根目录下的LICENSE文件。
 * 2.请不要删除和修改Snowy源码头部的版权声明。
 * 3.本项目代码可免费商业使用，商业使用请保留源码和相关描述文件的项目出处，作者声明等。
 * 4.分发源码时候，请注明软件出处 https://www.xiaonuo.vip
 * 5.不可二次分发开源参与同类竞品，如有想法可联系团队xiaonuobase@qq.com商议合作。
 * 6.若您的项目无法满足以上几点，需要更多功能代码，获取Snowy商业授权许可，请在官网购买授权，地址为 https://www.xiaonuo.vip
 */
package vip.xiaonuo.auth.modular.third.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.model.AuthUser;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vip.xiaonuo.auth.api.SaBaseLoginUserApi;
import vip.xiaonuo.auth.core.enums.AuthPlatformEnum;
import vip.xiaonuo.auth.core.enums.SaClientTypeEnum;
import vip.xiaonuo.auth.modular.third.entity.AuthThirdUser;
import vip.xiaonuo.auth.modular.third.mapper.AuthThirdUserMapper;
import vip.xiaonuo.auth.modular.third.service.AuthThirdUserService;

/**
 * 第三方登录Service接口实现类
 *
 * @author xuyuxiang
 * @date 2022/7/8 16:20
 **/
@Slf4j
@Service
public class AuthThirdUserServiceImpl extends ServiceImpl<AuthThirdUserMapper, AuthThirdUser> implements AuthThirdUserService {

    @Resource(name = "loginUserApi")
    private SaBaseLoginUserApi loginUserApi;

    @Resource(name = "clientLoginUserApi")
    private SaBaseLoginUserApi clientLoginUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createAndBindUser(AuthUser authUser, AuthThirdUser authThirdUser, String platform) {
        return createAndBindUser(authUser, authThirdUser, platform, SaClientTypeEnum.B.getValue());
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public String createAndBindClientUser(AuthUser authUser, AuthThirdUser authThirdUser, String platform) {
        return createAndBindUser(authUser, authThirdUser, platform, SaClientTypeEnum.C.getValue());
    }

    private String createAndBindUser(AuthUser authUser, AuthThirdUser authThirdUser, String platform, String clientType) {
        String userId = autoCreateUser(authUser, platform, clientType);
        if (ObjectUtil.isEmpty(authThirdUser)) {
            authThirdUser = new AuthThirdUser();
            authThirdUser.setThirdId(authUser.getUuid());
            authThirdUser.setUserId(userId);
            authThirdUser.setAvatar(authUser.getAvatar());
            authThirdUser.setName(authUser.getUsername());
            authThirdUser.setNickname(authUser.getNickname());
            authThirdUser.setGender(authUser.getGender() != null ? authUser.getGender().getDesc() : "未知");
            authThirdUser.setCategory(platform);
            authThirdUser.setExtJson(JSONUtil.toJsonStr(authUser.getRawUserInfo()));
            this.save(authThirdUser);
        } else {
            authThirdUser.setUserId(userId);
            this.updateById(authThirdUser);
        }
        log.info(">>> SSO登录创建账户并绑定成功，userId={}, thirdId={}, source={}", userId, authUser.getUuid(), platform);
        return userId;
    }

    @Override
    public void insertAuthThirdUser(String userId, AuthUser authUser, String platform) {
        AuthThirdUser authThirdUser = new AuthThirdUser();
        authThirdUser.setThirdId(authUser.getUuid());
        authThirdUser.setUserId(userId);
        authThirdUser.setAvatar(authUser.getAvatar());
        authThirdUser.setName(authUser.getUsername());
        authThirdUser.setNickname(authUser.getNickname());
        authThirdUser.setGender(ObjectUtil.isEmpty(authUser.getGender()) ? null : authUser.getGender().getDesc());
        authThirdUser.setCategory(platform);
        authThirdUser.setExtJson(JSONUtil.toJsonStr(authUser.getRawUserInfo()));
        this.save(authThirdUser);
    }

    /**
     * 自动创建本地用户
     *
     * @author yubaoshan
     * @date 2024/07/05
     */
    private String autoCreateUser(AuthUser authUser, String platform, String clientType) {
        String email = authUser.getEmail();
        String phone = platform.equalsIgnoreCase("phone") ? authUser.getUsername() : null;
        String userId;

        // 生成账号：来源 + "_" + 完整UUID，保证全局唯一性，避免截取导致的冲突
        String account = platform.toLowerCase() + "_" + authUser.getUuid();

        // 生成美观的默认名称（昵称）
        String name = authUser.getNickname();
        if (StrUtil.isBlank(name)) {
            name = authUser.getUsername();
        }
        if (StrUtil.isBlank(name)) {
            name = getFriendlyPlatformName(platform) + "用户_" + RandomUtil.randomString(10);
        }

        if (SaClientTypeEnum.B.getValue().equals(clientType)) {
            if (StrUtil.isNotBlank(phone)) {
                userId = loginUserApi.createUserWithPhone(phone).getId();
            } else if (StrUtil.isNotBlank(email)) {
                userId = loginUserApi.createUserWithEmail(email).getId();
            } else {
                String randomPassword = RandomUtil.randomString(16);
                userId = loginUserApi.createUserWithAccount(account, randomPassword, name).getId();
            }
        } else {
            if (StrUtil.isNotBlank(phone)) {
                userId = clientLoginUserApi.createClientUserWithPhone(phone).getId();
            } else if (StrUtil.isNotBlank(email)) {
                userId = clientLoginUserApi.createClientUserWithEmail(email).getId();
            } else {
                String randomPassword = RandomUtil.randomString(16);
                userId = clientLoginUserApi.createClientUserWithAccount(account, randomPassword, name).getId();
            }
        }
        return userId;
    }

    /**
     * 获取友好的平台名称
     *
     * @author yubaoshan
     * @date 2024/07/05
     */
    private String getFriendlyPlatformName(String source) {
        if (StrUtil.isBlank(source)) {
            return "第三方";
        }
        for (AuthPlatformEnum platformEnum : AuthPlatformEnum.values()) {
            if (platformEnum.getValue().equalsIgnoreCase(source)) {
                return platformEnum.getDescription();
            }
        }
        return source;
    }
}

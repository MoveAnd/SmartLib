package vip.xiaonuo.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Component;
import vip.xiaonuo.common.prop.CommonPasswordProperties;

import java.util.Collection;

/**
 * 密码工具类，统一使用 Argon2id 算法对密码进行单向编码与校验
 *
 * <p>本类不自行实现任何密码学算法：Argon2id 的算法实现来自 BouncyCastle
 * （{@code org.bouncycastle.crypto.generators.Argon2BytesGenerator}），
 * 由 Spring Security 的 {@link Argon2PasswordEncoder} 封装调用；
 * 随机盐由 Spring Security 的 {@code KeyGenerators.secureRandom}（基于 SecureRandom）生成，
 * 比较采用常量时间比较，避免时序攻击。</p>
 *
 * <p>编码结果遵循 PHC 标准 Argon2 字符串格式，盐与参数一并保存在编码串中，
 * 因此同一密码每次编码结果都不同，但都可以正确校验：</p>
 * <pre>$argon2id$v=19$m=19456,t=2,p=1$&lt;salt&gt;$&lt;hash&gt;</pre>
 *
 * <p>本类是 Spring Bean：所需的 {@link Argon2PasswordEncoder} 由 {@link #argon2PasswordEncoder} 依据
 * {@link CommonPasswordProperties} 注册，再通过构造器注入本类，不使用任何静态运行期状态。
 * 参数没有内置默认值，配置缺失或非法时注册编码器即失败，从而阻断应用启动。</p>
 *
 * <p>本实现不兼容历史 SM3 密码：{@code matches} 对 SM3 等非法编码一律返回 {@code false}，
 * 不提供回退或自动升级逻辑，旧账号需通过重置密码重新使用。</p>
 *
 * @author MoveAnd
 * @date 2026/02/05
 */
@Slf4j
@Component
public class CommonPasswordUtil {

    /** Argon2id 编码前缀 */
    private static final String ARGON2ID_PREFIX = "$argon2id$";

    /** Argon2id 编码器，由配置注册后注入 */
    private final Argon2PasswordEncoder argon2PasswordEncoder;

    /**
     * 构造器注入Argon2id编码器
     *
     * @author MoveAnd
     * @date 2026/02/05
     **/
    public CommonPasswordUtil(Argon2PasswordEncoder argon2PasswordEncoder) {
        this.argon2PasswordEncoder = argon2PasswordEncoder;
    }

    /**
     * 依据配置注册Argon2id编码器
     *
     * <p>先校验配置再创建编码器：配置缺失或非法时这里直接抛出异常使应用启动失败，
     * 不会退化为任何内置参数。</p>
     *
     * @author MoveAnd
     * @date 2026/02/05
     **/
    @Bean
    public static Argon2PasswordEncoder argon2PasswordEncoder(CommonPasswordProperties commonPasswordProperties) {
        commonPasswordProperties.validate();
        CommonPasswordProperties.Argon2 argon2 = commonPasswordProperties.getArgon2();
        Argon2PasswordEncoder encoder = new Argon2PasswordEncoder(argon2.getSaltLength(), argon2.getHashLength(),
                argon2.getParallelism(), argon2.getMemory(), argon2.getIterations());
        log.info(">>> Argon2id密码编码参数已生效：m={} KiB，t={}，p={}，saltLength={}，hashLength={}",
                argon2.getMemory(), argon2.getIterations(), argon2.getParallelism(),
                argon2.getSaltLength(), argon2.getHashLength());
        return encoder;
    }

    /**
     * 对明文密码进行Argon2id编码（每次调用使用新的随机盐，结果均不相同）
     *
     * @param rawPassword 明文密码
     * @return Argon2id编码串
     * @author MoveAnd
     * @date 2026/02/05
     **/
    public String encode(String rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("待编码的密码不可为空");
        }
        return argon2PasswordEncoder.encode(rawPassword);
    }

    /**
     * 校验明文密码与已保存的Argon2id编码是否匹配
     *
     * <p>仅接受 {@code $argon2id$} 前缀的编码：底层Argon2解码器同时支持argon2i/argon2d，
     * 若不限制前缀，其它Argon2变体的编码也可能校验通过，与本项目统一使用Argon2id的约定不符。
     * 对空值、其它Argon2变体编码、历史SM3编码、损坏编码以及未知编码一律返回false，
     * 不做回退或自动升级。</p>
     *
     * @param rawPassword 明文密码
     * @param encodedPassword 已保存的编码串
     * @return 是否匹配
     * @author MoveAnd
     * @date 2026/02/05
     **/
    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null || encodedPassword.isEmpty()) {
            return false;
        }
        // 仅允许Argon2id编码，拒绝argon2i、argon2d等其它算法编码
        if (!isArgon2idEncoded(encodedPassword)) {
            return false;
        }
        try {
            return argon2PasswordEncoder.matches(rawPassword, encodedPassword);
        } catch (RuntimeException e) {
            log.warn(">>> 密码编码格式非法，校验失败：{}", e.getMessage());
            return false;
        }
    }

    /**
     * 判断已保存的编码是否为Argon2id编码
     *
     * <p>纯字符串判断，不依赖任何运行期状态，故保持为静态方法。</p>
     *
     * @param encodedPassword 已保存的编码串
     * @return 是否为Argon2id编码
     * @author MoveAnd
     * @date 2026/02/05
     **/
    public static boolean isArgon2idEncoded(String encodedPassword) {
        return encodedPassword != null && encodedPassword.startsWith(ARGON2ID_PREFIX);
    }

    /**
     * 在编码集合中逐条校验明文密码是否命中（用于历史密码重复检查）
     *
     * <p>历史密码虽然保存的是同一个明文，但由于随机盐的存在，编码结果各不相同，
     * 因此必须逐条调用{@link #matches(String, String)}校验，不可使用equals或contains比较编码串。
     * 历史记录中可能存在历史SM3编码或损坏编码，此类记录一律跳过，不视为命中。</p>
     *
     * @param rawPassword 明文密码
     * @param encodedPasswordCollection 已保存的编码集合
     * @return 是否存在匹配的历史编码
     * @author MoveAnd
     * @date 2026/02/05
     **/
    public boolean matchesAny(String rawPassword, Collection<String> encodedPasswordCollection) {
        if (rawPassword == null || encodedPasswordCollection == null || encodedPasswordCollection.isEmpty()) {
            return false;
        }
        for (String encodedPassword : encodedPasswordCollection) {
            if (matches(rawPassword, encodedPassword)) {
                return true;
            }
        }
        return false;
    }
}

package vip.xiaonuo.common.prop;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 密码加密配置（Argon2id）
 *
 * <p>本类只承载配置数据与参数校验，不创建任何编码器：编码器由
 * {@code CommonPasswordUtil#argon2PasswordEncoder()} 依据本配置注册为 Spring Bean，
 * 因此参数只有"配置 → Bean → 使用方"这一条路径。</p>
 *
 * <p>配置示例（Nacos 中对应应用的 dataId，如 {@code snowy-web-app-{env}.yaml}）：</p>
 * <pre>
 * snowy:
 *   config:
 *     password:
 *       argon2:
 *         memory: 19456
 *         iterations: 2
 *         parallelism: 1
 *         salt-length: 16
 *         hash-length: 32
 * </pre>
 *
 * <p>五项均为必填，没有内置默认值：缺失或非法时应用启动直接失败，避免漏配时静默使用某个内置参数。</p>
 *
 * @author MoveAnd
 * @date 2026/02/05
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "snowy.config.password")
public class CommonPasswordProperties {

    /** 随机盐长度的安全下限，单位字节 */
    public static final int MIN_SALT_LENGTH = 8;

    /** 输出摘要长度的安全下限，单位字节 */
    public static final int MIN_HASH_LENGTH = 16;

    /**
     * 并行度上限。
     *
     * <p>与BouncyCastle Argon2BytesGenerator的MAX_PARALLELISM保持一致（16777216）；
     * 超过该值时init()会抛异常，必须在启动校验阶段拦住。</p>
     */
    public static final int MAX_PARALLELISM = 16777216;

    /**
     * Argon2id算法参数
     */
    private Argon2 argon2;

    /**
     * 校验配置是否完整且合法，不满足时抛出异常阻止启动
     *
     * @author MoveAnd
     * @date 2026/02/05
     **/
    public void validate() {
        if (argon2 == null) {
            throw new IllegalStateException("缺少Argon2id密码编码配置，请检查配置项snowy.config.password.argon2.*是否完整提供");
        }
        argon2.validate();
    }

    /**
     * Argon2id算法参数
     *
     * <p>全部字段使用包装类型且不设默认值：未配置时保持为null，
     * 以便区分"没有配置"与"配置了非法值"，前者提示缺失、后者提示取值范围。</p>
     *
     * @author MoveAnd
     * @date 2026/02/05
     */
    @Getter
    @Setter
    public static class Argon2 {

        /**
         * 内存代价，单位KiB，必填，基线参考值19456 KiB（19 MiB）
         */
        private Integer memory;

        /**
         * 迭代次数，必填，基线参考值2
         */
        private Integer iterations;

        /**
         * 并行度，必填，基线参考值1
         */
        private Integer parallelism;

        /**
         * 随机盐长度，单位字节，必填，基线参考值16
         */
        private Integer saltLength;

        /**
         * 输出摘要长度，单位字节，必填，基线参考值32
         */
        private Integer hashLength;

        /**
         * 参数合法性校验，配置缺失或非法直接抛出异常阻止启动
         *
         * @author MoveAnd
         * @date 2026/02/05
         **/
        public void validate() {
            if (memory == null) {
                throw new IllegalStateException("缺少Argon2id内存代价配置：snowy.config.password.argon2.memory");
            }
            if (iterations == null) {
                throw new IllegalStateException("缺少Argon2id迭代次数配置：snowy.config.password.argon2.iterations");
            }
            if (parallelism == null) {
                throw new IllegalStateException("缺少Argon2id并行度配置：snowy.config.password.argon2.parallelism");
            }
            if (saltLength == null) {
                throw new IllegalStateException("缺少Argon2id随机盐长度配置：snowy.config.password.argon2.salt-length");
            }
            if (hashLength == null) {
                throw new IllegalStateException("缺少Argon2id输出摘要长度配置：snowy.config.password.argon2.hash-length");
            }
            if (memory <= 0) {
                throw new IllegalArgumentException("Argon2id内存代价（snowy.config.password.argon2.memory）必须大于0");
            }
            if (iterations <= 0) {
                throw new IllegalArgumentException("Argon2id迭代次数（snowy.config.password.argon2.iterations）必须大于0");
            }
            if (parallelism <= 0) {
                throw new IllegalArgumentException("Argon2id并行度（snowy.config.password.argon2.parallelism）必须大于0");
            }
            if (parallelism > MAX_PARALLELISM) {
                throw new IllegalArgumentException("Argon2id并行度（snowy.config.password.argon2.parallelism）不可大于"
                        + MAX_PARALLELISM + "，与BouncyCastle算法上限一致");
            }
            // 算法硬约束：Argon2要求 memory >= 2 * lanes（见 Argon2BytesGenerator.init），
            // 不满足时首次编码才抛异常，因此必须在此拦截；用long运算避免乘法溢出
            if ((long) memory < 2L * parallelism) {
                throw new IllegalArgumentException("Argon2id内存代价（snowy.config.password.argon2.memory）不可小于并行度的2倍，"
                        + "当前并行度为" + parallelism + "，至少需要" + (2L * parallelism) + " KiB");
            }
            if (saltLength < MIN_SALT_LENGTH) {
                throw new IllegalArgumentException("Argon2id随机盐长度（snowy.config.password.argon2.salt-length）不可小于"
                        + MIN_SALT_LENGTH + "字节");
            }
            if (hashLength < MIN_HASH_LENGTH) {
                throw new IllegalArgumentException("Argon2id输出摘要长度（snowy.config.password.argon2.hash-length）不可小于"
                        + MIN_HASH_LENGTH + "字节");
            }
        }
    }

}

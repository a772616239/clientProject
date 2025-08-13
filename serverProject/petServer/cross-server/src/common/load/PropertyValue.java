package common.load;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author huhan
 * @date 2020/07/07
 *  此注解用于标注ServerConfig中需要初始化的filed
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PropertyValue {
    String value();

    /**
     * 允许字段为0
     * @return
     */
    boolean allowZero() default false;

    /**
     * 允许字段为空
     * @return
     */
    boolean allowNull() default false;
}

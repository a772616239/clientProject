package server.handler.resRecycle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import protocol.Common;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResourceRecycle {
     Common.EnumFunction function () default Common.EnumFunction.NullFuntion;
}

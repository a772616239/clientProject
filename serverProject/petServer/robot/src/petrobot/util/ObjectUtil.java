package petrobot.util;

/**
 * @author huhan
 * @date 2020.02.28
 */
public class ObjectUtil {
    /**
     * 判断指定对象都不为空
     * @param objects
     * @return true:没有一个对象为空，false:一个或者多个对象为空
     */
    public static boolean requireNotNull(Object... objects) {
        if (objects == null) {
            return true;
        }
        for (Object object : objects) {
            if (object == null) {
                return false;
            }
        }
        return true;
    }
}

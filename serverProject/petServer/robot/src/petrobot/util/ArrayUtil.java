package petrobot.util;

/**
 * @author huhan
 * @date 2020.02.28
 */
public class ArrayUtil {

    public static boolean containStringIgnoreCase(String[] array, String target) {
        if (!ObjectUtil.requireNotNull(array, target)) {
            return false;
        }

        for (String s : array) {
            if (target.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }

    public static boolean contain(Object[] array, Object target) {
        if (!ObjectUtil.requireNotNull(array, target)) {
            return false;
        }

        for (Object obj : array) {
            if (target.equals(obj)) {
                return true;
            }
        }
        return false;
    }
}

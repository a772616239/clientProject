package code;

import java.math.BigDecimal;

/**
 * @author Administrator
 * @date
 */
public class Util {

    public static String toUpperCaseFirstOne(String s) {
        if (Character.isUpperCase(s.charAt(0))) {
            return s;
        } else {
            return Character.toUpperCase(s.charAt(0)) +
                    s.substring(1);
        }
    }

    public static int getShWr(double t) {
        BigDecimal b = new BigDecimal(t).setScale(0, BigDecimal.ROUND_HALF_UP);
        return b.intValue();

    }
}

package util;

import com.bowlong.util.DateEx;
import model.obj.BaseObj;

public class GameUtil {
    static BaseObj baseObj;

    public static String useTime(long l1, long l2) {

        long df = l2 - l1;

        String strStarUp = "";
        if (df > DateEx.TIME_SECOND) {
            df = (long) Math.ceil((double) df / DateEx.TIME_SECOND);
            strStarUp = df + " Miao(s)";
        } else {
            strStarUp = df + " Hao Miao(ms)";
        }
        return strStarUp;

    }

    public static void sleep(long elapsed) {
        if (elapsed <= 0) {
            return;
        }
        try {
            Thread.sleep(elapsed);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static BaseObj getDefaultEventSource() {
        if (baseObj == null) {
            baseObj = new BaseObj() {

                @Override
                public String getIdx() {
                    return "system";
                }

                @Override
                public void setIdx(String idx) {

                }

                @Override
                public String getClassType() {
                    return "system";
                }

                @Override
                public boolean lockObj() {
                    return false;
                }

                @Override
                public boolean unlockObj() {
                    return false;
                }
            };
        }
        return baseObj;
    }

    public static long stringToLong(String source, long defaultVal) {
        try {
            return Long.valueOf(source);
        } catch (Exception e) {
            return defaultVal;
        }
    }

    public static String longToString(long v, String defaultValue) {
        try {
            return String.valueOf(v);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static byte[] intToByte(int val) {
        byte[] b = new byte[4];
        b[0] = (byte) (val & 0xff);
        b[1] = (byte) ((val >> 8) & 0xff);
        b[2] = (byte) ((val >> 16) & 0xff);
        b[3] = (byte) ((val >> 24) & 0xff);
        return b;
    }

    public static boolean isBasicType(Object obj) {
        try {
            return ((Class<?>) obj.getClass().getField("TYPE").get(null)).isPrimitive();
        } catch (Exception e) {
            return false;
        }
    }

}

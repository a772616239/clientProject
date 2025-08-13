package util;

import com.bowlong.util.DateEx;
import common.load.ServerConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import model.obj.BaseObj;
import org.apache.commons.collections4.CollectionUtils;
import protocol.Common.Reward;
import protocol.Common.SC_DisplayRewards;
import protocol.RetCodeId.ParamInfo;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;

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
                public String getBaseIdx() {
                    return "system";
                }

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

                @Override
                public void putToCache() {

                }

                @Override
                public void transformDBData() {

                }
            };
        }
        return baseObj;
    }

    // 跨服排行榜相同clientId情况下区分
    public static String buildTransServerRankName(String transRankName) {
        return transRankName + ServerConfig.getInstance().getRedisDbIndex() + "-";
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

    public static RetCode.Builder buildRetCode(RetCodeEnum codeNum, Object... params) {
        RetCode.Builder retBuilder = RetCode.newBuilder();
        retBuilder.setRetCode(codeNum);
        if (params != null && params.length > 0) {
            for (Object param : params) {
                ParamInfo.Builder paramBuilder = ParamInfo.newBuilder();
                try {
                    if (param instanceof String) {
                        paramBuilder.setPramType(1); // 1为字符串类型,0为int类型
                        paramBuilder.setParamVal((String) param);
                    } else if (param instanceof Character) {
                        char val = (char) param;
                        paramBuilder.setPramType(1);
                        paramBuilder.setParamVal(String.valueOf(val));
                    } else if (param instanceof Boolean) {
                        boolean val = (Boolean) param;
                        paramBuilder.setParamVal(val ? "1" : "0");
                    } else if (isBasicType(param)) {
                        paramBuilder.setParamVal(String.valueOf(param));
                    } else {
                        paramBuilder.setParamVal("0");
                    }
                } catch (Exception e) {
                    paramBuilder.setPramType(0);
                    paramBuilder.setParamVal("0");
                }
                retBuilder.addParams(paramBuilder);
            }
        }
        return retBuilder;
    }

    public static SC_DisplayRewards.Builder builderDisRewards(List<Reward> rewards) {
        if (rewards == null) {
            return null;
        }
        SC_DisplayRewards.Builder disReward = SC_DisplayRewards.newBuilder();
        disReward.addAllRewardList(rewards);
        return disReward;
    }

    /**
     * 判断集合是否为空
     *
     * @param collection
     * @return
     */
    public static boolean collectionIsEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * 判断参数是否在指定范围之外
     *
     * @param border_1 边界
     * @param border_2 边界
     * @param param
     * @return
     */
    public static boolean outOfScope(long border_1, long border_2, long param) {
        long up = Math.max(border_1, border_2);
        long low = Math.min(border_1, border_2);
        return param < low || param > up;
    }

    /**
     * 判断参数是否在范围之内,包含边界
     *
     * @param border_1 边界
     * @param border_2 边界
     * @param param
     * @return
     */
    public static boolean inScope(long border_1, long border_2, long param) {
        return !outOfScope(border_1, border_2, param);
    }

    public static String collectionToString(Collection<?> collection) {
        if (collectionIsEmpty(collection)) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (Object o : collection) {
            result.append(",");
            result.append(o.toString());
        }
        return result.toString();
    }

    /**
     * 分割List
     *
     * @param coll     需要分割的目标
     * @param eachSize 分割大小  小于等于0 时，不分割返回
     * @param <T>
     * @return
     */
    public static <T> List<List<T>> splitCollection(Collection<T> coll, int eachSize) {
        if (GameUtil.collectionIsEmpty(coll)) {
            LogUtil.warn("util.GameUtil.splitCollection, targetCollection is empty");
            return null;
        }

        List<List<T>> result = new ArrayList<>();
        if (eachSize <= 0 || coll.size() <= eachSize) {
            result.add(new ArrayList<>(coll));
            return result;
        }

        List<T> curList = new ArrayList<>(eachSize);
        for (T t : coll) {
            curList.add(t);

            if (curList.size() >= eachSize) {
                result.add(curList);
                curList = new ArrayList<>();
            }
        }

        if (CollectionUtils.isNotEmpty(coll)) {
            result.add(curList);
        }

        return result;
    }

    public static String builderServerAddr() {
        return ServerConfig.getInstance().getIp() + ":" + ServerConfig.getInstance().getPort();
    }

    public static <T> List<T> parseArrayToList(T[] array) {
        List<T> result = new ArrayList<>();
        if (array == null) {
            return result;
        }
        result.addAll(Arrays.asList(array));
        return result;
    }

    public static long mergeIntToLong(int high, int low) {
        return ((((long) high) << 32) & 0xFFFFFFFF00000000l) | (((long) low) & 0xFFFFFFFFl);
    }

    public static int getHighLong(long value) {
        return (int) (value >>> 32);
    }

    public static int getLowLong(long value) {
        return (int) value;
    }
}

package util;

import cfg.GameConfig;
import com.alibaba.fastjson.JSONObject;
import com.bowlong.util.DateEx;
import common.GameConst;
import common.load.ServerConfig;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import model.obj.BaseObj;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import protocol.Common.LanguageEnum;
import protocol.Common.RewardTypeEnum;
import protocol.RetCodeId.ParamInfo;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;

public class GameUtil {
    static BaseObj baseObj;

    public static String useTime(long startTime, long endTime) {

        long df = endTime - startTime;

        String strStarUp = "";
        if (df > DateEx.TIME_SECOND) {
            df = (long) Math.ceil((double) df / DateEx.TIME_SECOND);
            strStarUp = df + " Miao(s)";
        } else {
            strStarUp = df + " Hao Miao(ms)";
        }
        return strStarUp;

    }

    public static BaseObj getDefaultEventSource() {
        if (baseObj == null) {
            baseObj = new BaseObj() {
                @Override
                public String getBaseIdx() {
                    return "system";
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
        return transRankName + "-" + ServerConfig.getInstance().getRedisDbIndex();
    }

    public static long stringToLong(String source, long defaultVal) {
        try {
            return Long.parseLong(source);
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
                        // 1为字符串类型,0为int类型
                        paramBuilder.setPramType(1);
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

    /**
     * 检查配置参数的正确性,正确返回true，错误返回false, 目前int类型不许有负数
     *
     * @return
     */
    public static boolean checkCfgParams(Object obj) {
        if (obj == null) {
            LogUtil.error("GameUtil.checkCfgParams，obj is null");
            return false;
        }

        Class<?> clazz = obj.getClass();
        try {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Class<?> type = field.getType();
                if (type == int.class) {
                    int value = field.getInt(obj);
                    if (value <= 0) {
                        LogUtil.error("cfgName : " + clazz.getName() + ", fieldName = " + field.getName() + ", value = " + value);
                        return false;
                    }
                } else if (type == String.class) {
                    String value = (String) field.get(obj);
                    if (value == null || "".equals(value) || "null".equalsIgnoreCase(value)) {
                        LogUtil.error("cfgName : " + clazz.getName() + ", fieldName:" + field.getName() + ", value:" + value);
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }

    public static int sumInt(int a, int b) {
        try {
            return Math.addExact(a, b);
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }
    }

    public static long sumLong(long a, long b) {
        try {
            return Math.addExact(a, b);
        } catch (Exception e) {
            return Long.MAX_VALUE;
        }
    }

    public static int multi(int a, int b) {
        try {
            return Math.multiplyExact(a, b);
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }
    }

    public static long multi(long a, long b) {
        try {
            return Math.multiplyExact(a, b);
        } catch (Exception e) {
            return Long.MAX_VALUE;
        }
    }

    /**
     * 得到对应语言版本的字符串,该字符串是以枚举值对应的int为值的JSON
     * @param string {"languageValue":"",...}
     */
    public static String getLanguageStr(String string, LanguageEnum languageNum) {
        JSONObject obj;
        try {
            obj = JSONObject.parseObject(string);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return "";
        }
        if (obj == null) {
            return "";
        }

        String key = String.valueOf(languageNum.getNumber());
        if (!obj.containsKey(key)) {
            return "";
        }
        String str = obj.getString(key);
        return str == null ? "" : str;
    }


    /**
     * 将json{languageNum,content} to Map<languageNum,content>
     *
     * @param str
     * @return
     */
    public static Map<Integer, String> parseStrToLanguageNumContentMap(String str) {
        return parseJsonObjToLanguageNumContentMap(JSONObject.parseObject(str));
    }

    public static Map<Integer, String> parseJsonObjToLanguageNumContentMap(JSONObject jsonObj) {
        Map<Integer, String> result = new HashMap<>(5);
        if (jsonObj == null) {
            return result;
        }

        for (Entry<String, Object> entry : jsonObj.entrySet()) {
            if (StringUtils.isNumeric(entry.getKey())) {
                result.put(Integer.parseInt(entry.getKey()), entry.getValue().toString());
            }
        }
        return result;
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

    public static <T> List<T> parseArrayToList(T[] array) {
        List<T> result = new ArrayList<>();
        if (array == null) {
            return result;
        }
        result.addAll(Arrays.asList(array));
        return result;
    }

    /**
     * ip地址中合法的字符
     **/
    public static final int[] IP_LEGAL_CHAR = new int[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f',
            'A', 'B', 'C', 'D', 'E', 'F', '.', ':'};

    /**
     * 暂且只适合未缩写的ip地址,只能过滤非合法字符的字符,注意使用限制
     *
     * @param ipPort
     * @return String[0] = ip, String[1] = port;
     */
    public static String[] getIpPort(String ipPort) {
        if (ipPort == null) {
            return null;
        }
        String[] split = ipPort.split(":");

        for (int i = 0; i < split.length; i++) {
            String str = split[i];
            if (str == null) {
                continue;
            }

            StringBuilder stringBuffer = new StringBuilder();
            for (char ch : str.toCharArray()) {
                if (ArrayUtil.intArrayContain(IP_LEGAL_CHAR, ch)) {
                    stringBuffer.append(ch);
                }
            }

            split[i] = stringBuffer.toString();
        }
        return split;
    }


    public static boolean isNewbieMap(int fightMakeId) {
        int[] newbieMapList = GameConfig.getById(GameConst.CONFIG_ID).getNewbeefightmakeid();
        if (newbieMapList != null && newbieMapList.length > 0) {
            for (int i = 0; i < newbieMapList.length; i++) {
                if (fightMakeId == newbieMapList[i]) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String collectionToString(Collection<?> collection) {
        if (collectionIsEmpty(collection)) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (Object o : collection) {
            result.append(o.toString());
            result.append(",");
        }
        return result.toString();
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
     * 分割List
     *
     * @param list     需要分割的目标
     * @param eachSize 分割大小
     * @param <T>
     * @return
     */
    public static <T> List<List<T>> splitList(List<T> list, int eachSize) {
        if (GameUtil.collectionIsEmpty(list)) {
            LogUtil.warn("util.GameUtil.splitCollection, targetCollection is empty");
            return null;
        }

        List<List<T>> result = new ArrayList<>();
        if (eachSize <= 0) {
            LogUtil.warn("util.GameUtil.splitCollection, eachSize need > 0");
            result.add(list);
            return result;
        }

        List<T> curList = new ArrayList<>(eachSize);
        for (T t : list) {
            curList.add(t);

            if (curList.size() >= eachSize) {
                result.add(curList);
                curList = new ArrayList<>();
            }
        }

        if (!curList.isEmpty()) {
            result.add(curList);
        }

        return result;
    }

    public static void sleep(long elapsed) {
        if (elapsed <= 0) {
            return;
        }
        try {
            Thread.sleep(elapsed);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    /**
     * 随机正整数
     */
    public static long randomPositiveLong() {
        return Math.abs(new Random().nextLong());
    }

    public static int randomInScope(int[] scope) {
        if (scope == null || scope.length < 2) {
            return 0;
        }
        return randomInScope(scope[0], scope[1]);
    }

    /**
     * 包含上界
     *
     * @param border_1
     * @param border_2
     * @return
     */
    public static int randomInScope(int border_1, int border_2) {
        int max = Math.max(border_1, border_2);
        int min = Math.min(border_1, border_2);
        if (max <= min) {
            return max;
        }

        return min + new Random().nextInt(max - min + 1);
    }

    /**
     * 从一个Collection随机取几个元素
     *
     * @param target
     * @param needCount
     * @param <T>
     * @return
     */
    public static <T> Collection<T> randomGet(Collection<T> target, int needCount) {
        if (CollectionUtils.size(target) <= needCount) {
            return target;
        }

        if (needCount <= 0) {
            return null;
        }

        List<T> targetList = new ArrayList<>(target);
        Random random = new Random();

        List<T> result = new ArrayList<>();
        for (int i = 0; i < needCount; i++) {
            int randomIndex = random.nextInt(targetList.size());
            result.add(targetList.get(randomIndex));
            targetList.remove(randomIndex);
        }
        return result;
    }

    public static int longToInt(long longNum) {
        return longNum >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) longNum;
    }

    public static int floatToInt(float floatNum) {
        return floatNum >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) floatNum;
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

    /**
     * 判断奖励类型是否是货币类型
     * @param rewardType
     * @return
     */
    public static boolean isCurrencyRewardType(RewardTypeEnum rewardType) {
        if (rewardType == RewardTypeEnum.RTE_Gold
                || rewardType == RewardTypeEnum.RTE_Diamond
                || rewardType == RewardTypeEnum.RTE_Coupon
                || rewardType == RewardTypeEnum.RTE_HolyWater) {
            return true;
        }
        return false;
    }

    public static boolean isCurrencyRewardType(int rewardTypeValue) {
        return isCurrencyRewardType(RewardTypeEnum.forNumber(rewardTypeValue));
    }
}

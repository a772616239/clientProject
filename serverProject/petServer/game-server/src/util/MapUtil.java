package util;

import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import protocol.Battle;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import protocol.Common;

public class MapUtil {

    /**
     * 添加一对键值对入map
     * 如果存在key,则把当前值加原值作为新值.如果不存在,则放入当前key,value
     *
     * @param map
     * @param key
     * @param value
     */
    public static void add2IntMapValue(Map<Integer, Integer> map, Integer key, Integer value) {
        if (map == null || value == null || key == null) {
            return;
        }
        Integer existValue = map.get(key);
        map.put(key, existValue == null ? value : existValue + value);
    }

    public static void add2LongMapValue(Map<Integer, Long> map, Integer key, Long value) {
        if (map == null || value == null || key == null) {
            return;
        }
        Long existValue = map.get(key);
        map.put(key, existValue == null ? value : existValue + value);
    }


    /**
     * 添加int[][]值入map  单个int[] [0]为key [1]为value
     * 如果存在key,则把当前值加原值作为新值.如果不存在,则放入当前key,value
     *
     * @param map
     * @param value
     */
    public static void add2IntMapValue(Map<Integer, Integer> map, int[][] value) {
        if (map == null || ArrayUtils.isEmpty(value)) {
            return;
        }
        for (int[] data : value) {
            Integer existValue = map.get(data[0]);
            map.put(data[0], existValue == null ? data[1] : existValue + data[1]);
        }
    }


    public static void mergeIntMaps(Map<Integer, Integer> baseMap, Map<Integer, Integer> adds) {
        if (MapUtils.isEmpty(adds) || baseMap == null) {
            return;
        }
        for (Entry<Integer, Integer> entry : adds.entrySet()) {
            MapUtil.add2IntMapValue(baseMap, entry.getKey(), entry.getValue());
        }
    }

    public static void mergeLongMaps(Map<Integer, Long> baseMap, Map<Integer, Long> adds) {
        if (MapUtils.isEmpty(adds) || baseMap == null) {
            return;
        }
        for (Entry<Integer, Long> entry : adds.entrySet()) {
            MapUtil.add2LongMapValue(baseMap, entry.getKey(), entry.getValue());
        }
    }

    public static void subtractValue(Map<Integer, Long> baseMap, Map<Integer, Long> cut) {
        if (MapUtils.isEmpty(baseMap) && MapUtils.isEmpty(cut)) {
            return;
        }
        for (Entry<Integer, Long> entry : cut.entrySet()) {
            add2LongMapValue(baseMap, entry.getKey(), -entry.getValue());
        }
    }

    public static Map<Integer, Integer> exPropertyToMap(Battle.ExtendProperty.Builder exProperty) {
        if (exProperty == null) {
            return Collections.emptyMap();
        }
        Battle.PetPropertyDict propDict = exProperty.getPropDict();
        Map<Integer, Integer> values = new HashMap<>();

        int cycle = Math.min(propDict.getKeysCount(), propDict.getValuesCount());

        for (int index = 0; index < cycle; index++) {
            values.put(propDict.getKeys(index).getNumber(), (int) propDict.getValues(index));
        }
        return values;


    }

    public static Integer getIntMapValue(Common.IntMap map, int key) {
        if (map == null) {
            throw new RuntimeException("getIntMapValue map can`t be null");
        }
        List<Integer> keysList = map.getKeysList();
        for (int i = 0; i < keysList.size(); i++) {
            if (keysList.get(i) == key) {
                return map.getValuesList().get(i);
            }
        }
        return null;
    }

    public static void incrIntMapValue(Common.IntMap.Builder map, int key, int incr) {
        if (map == null) {
            throw new RuntimeException("getIntMapValue map can`t be null");
        }
        List<Integer> keysList = map.getKeysList();
        for (int i = 0; i < keysList.size(); i++) {
            if (keysList.get(i) == key) {
                map.setValues(i, map.getKeys(i) + incr);
                return;
            }
        }
        map.addKeys(key).addValues(incr);
    }

    public static Map<Integer, Integer> exPropertyToMap(int[][] exProperty) {
        if (ArrayUtils.isEmpty(exProperty)) {
            return null;
        }
        Map<Integer, Integer> result = new HashMap<>();
        for (int[] ints : exProperty) {
            if (ints.length < 2) {
                continue;
            }
            result.put(ints[0], ints[1]);
        }
        return result;
    }

    public static Common.IntMap map2IntMap(Map<Integer, Integer> data) {
        if (MapUtils.isEmpty(data)) {
            return Common.IntMap.getDefaultInstance();
        }
        Common.IntMap.Builder result = Common.IntMap.newBuilder();
        result.addAllKeys(data.keySet());
        result.addAllValues(data.values());
        return result.build();
    }
}

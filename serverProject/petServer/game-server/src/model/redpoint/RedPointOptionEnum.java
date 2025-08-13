package model.redpoint;

import java.util.HashMap;
import java.util.Map;

public enum RedPointOptionEnum {
    ADD,
    REMOVE,
    CHECK;

    private static Map<Integer, RedPointOptionEnum> map = new HashMap<>();

    public static RedPointOptionEnum optionOf(int option) {
        return map.getOrDefault(option, CHECK);
    }

    static {
        for (RedPointOptionEnum value : values()) {
            map.put(value.ordinal(), value);
        }
    }
}

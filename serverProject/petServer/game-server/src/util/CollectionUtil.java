package util;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BinaryOperator;
import org.apache.commons.collections4.CollectionUtils;

public class CollectionUtil {

    public static <C> List<C> safeMerge(List<C> c1, List<C> c2) {
        boolean empty1 = CollectionUtils.isEmpty(c1);
        boolean empty2 = CollectionUtils.isEmpty(c2);
        if (empty1 && empty2) {
            return Collections.emptyList();
        }
        List<C> result = new ArrayList<>();
        if (!empty1) {
            result.addAll(c1);
        }
        if (!empty2) {
            result.addAll(c2);
        }
        return result;
    }

    public static <E> E getLastItemOfList(List<E> list) {
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.get(list.size() - 1);

    }

    @SafeVarargs
    public static <K, V> Map<K, V> mergeMap(BinaryOperator<V> operator, Map<K, V>... maps) {
        if (maps == null || maps.length <= 0 || operator == null) {
            return Collections.emptyMap();
        }

        Map<K, V> result = new HashMap<>();
        for (Map<K, V> map : maps) {
            if (map == null) {
                continue;
            }
            for (Entry<K, V> entry : map.entrySet()) {
                if (entry.getValue() == null) {
                    continue;
                }
                V oldVal = result.get(entry.getKey());
                if (oldVal == null) {
                    result.put(entry.getKey(), entry.getValue());
                    continue;
                }

                V newVal = operator.apply(oldVal, entry.getValue());
                if (newVal != null) {
                    result.put(entry.getKey(), newVal);
                }
            }
        }

        return result;
    }
}

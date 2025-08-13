package model.gloryroad;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.springframework.util.CollectionUtils;
import util.LogUtil;

/**
 * 优先填充奇数位,然后再填充偶数位,用于对筛选玩家的分组
 *
 * @author huhan
 * @date 2021/3/16
 */
public class GloryRoadGroup<T> {
    private int size;

    @Getter
    private Map<Integer, T> indexMap;

    public GloryRoadGroup(int size) {
        this.size = size;
        this.indexMap = new HashMap<>();
    }

    public boolean addMember(T result) {
        if (this.indexMap.containsValue(result)) {
            LogUtil.error("GloryRoadGroup.addMember, member:" + result + " is exist");
            return false;
        }

        //选择奇数位
        for (int i = 1; i <= size; i += 2) {
            if (!this.indexMap.containsKey(i)) {
                this.indexMap.put(i, result);
                return true;
            }
        }

        //选择偶数位
        for (int i = 2; i <= size; i += 2) {
            if (!this.indexMap.containsKey(i)) {
                this.indexMap.put(i, result);
                return true;
            }
        }

        LogUtil.error("GloryRoadGroup.addMember, add failed, have no remain position");
        return false;
    }

    public boolean addAllMembers(List<T> results) {
        if (CollectionUtils.isEmpty(results)) {
            return false;
        }
        for (T result : results) {
            if (!addMember(result)) {
                return false;
            }
        }

        return true;
    }
}

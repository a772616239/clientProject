package common.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

/**
 * @author xiao_FL
 * @date 2019/8/21
 */

@Getter
@Setter
public class RankingServerMsg {
    /**
     * 必填
     * 更新排行榜的名称,无该字段请求将无效
     */
    private String rank;

    /**
     * 必填
     * 该字段用于区分数据不互通区服,无该字段请求将无效
     */
    private Integer serverIndex;

    /**
     * 删除指定key,为空，删除整个排行榜； 非空，删除该排行榜里面的该条记录
     */
    private List<String> keyList;

    /**
     * 排序规则
     */
    private List<Integer> sortRules;

    public RankingServerMsg() {
        this.sortRules = RankingUpdateRequest.DEFAULT_SORT_RULES;
    }

    public void addKeyList(String key) {
        addAllKeyList(Collections.singletonList(key));
    }

    public void addAllKeyList(List<String> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return;
        }

        if (this.keyList == null) {
            this.keyList = new ArrayList<>();
        }

        this.keyList.addAll(keys);
    }
}

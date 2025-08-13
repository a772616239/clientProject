package common.entity;

import common.load.ServerConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;

/**
 * @author xiao_FL
 * @date 2019/8/16
 */
@Getter
@Setter
@ToString
public class RankingUpdateRequest {

    /**
     * 升序
     */
    public static final int SORT_SAC = 1;

    /**
     * 降序
     */
    public static final int SORT_DES = 0;

    /**
     * 默认排序规则,第一个键降序,第二个键升序
     */
    public static final List<Integer> DEFAULT_SORT_RULES = Arrays.asList(SORT_DES, SORT_SAC);

    /**
     * 双健都为降序
     */
    public static final List<Integer> SORT_RULES_DES_DES = Arrays.asList(SORT_DES, SORT_DES);

    /**
     * 更新排行榜的名称,无该字段请求将无效
     */
    private String rank;

    /**
     * 该字段用于区分数据不互通区服,无该字段请求将无效,
     */
    private int serverIndex;

    /**
     * 排行榜更新数据
     */
    private List<RankingScore> items;

    /**
     * 排序规则，例如[1,0]表示第一字段表示升序，第二字段表示降序
     */
    private List<Integer> sortRules;

    private void addScore(RankingScore score) {
        if (score == null) {
            return;
        }
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(score);
    }

    public void addScore(String primaryKey, long primaryScore, long subScore) {
        this.addScore(RankingScore.createScore(primaryKey, primaryScore, subScore, getRank()));
    }

    public void addScore(String primaryKey, long primaryScore) {
        this.addScore(primaryKey, primaryScore, 0);
    }

    private RankingUpdateRequest(String rankingName, int serverIndex) {
        this(rankingName, serverIndex, DEFAULT_SORT_RULES);
    }

    public RankingUpdateRequest(String rankingName) {
        this(rankingName, ServerConfig.getInstance().getServer());
    }

    public void addPetScore(String playerId, String primaryKey, long primaryScore) {
        this.addScore(PetRankingScore.createScore(playerId, primaryKey, primaryScore, 0, getRank()));
    }

    public RankingUpdateRequest(String rankName, int serverIndex, List<Integer> sortRules) {
        this.rank = rankName;
        this.serverIndex = serverIndex;
        if (CollectionUtils.isEmpty(sortRules)) {
            this.sortRules = DEFAULT_SORT_RULES;
        } else {
            this.sortRules = sortRules;
        }
        items = Collections.synchronizedList(new ArrayList<>());
    }
}

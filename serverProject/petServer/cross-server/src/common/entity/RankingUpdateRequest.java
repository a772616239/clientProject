package common.entity;

import common.GameConst;
import common.load.ServerConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 排行榜数据更新实体,
 * 此类亲不要使用无参构造器,封装 items，玩家的额外信息需要用到排行榜名
 *
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
    public static final List<Integer> DEFAULT_SORT_RULES;

    static {
        List<Integer> tempList = new ArrayList<>();

        tempList.add(SORT_DES);
        tempList.add(SORT_SAC);

        DEFAULT_SORT_RULES = Collections.unmodifiableList(tempList);
    }

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

    public void addItems(RankingScore score) {
        if (score == null) {
            return;
        }
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(score);
    }

    public void addAllItems(RankingScore... score) {
        if (score == null) {
            return;
        }

        for (RankingScore item : score) {
            addItems(item);
        }
    }

    public RankingUpdateRequest(String rankingName, int serverIndex) {
        this.rank = rankingName;
        this.serverIndex = serverIndex;
        this.sortRules = DEFAULT_SORT_RULES;
    }

    /**
     *
     * @param rankingName
     * @param crossRanking 跨服排行榜
     */
    public RankingUpdateRequest(String rankingName, boolean crossRanking) {
        this.rank = rankingName;
        if (crossRanking) {
            this.serverIndex = GameConst.CROSS_RANKING_SERVER_INDEX;
        } else {
            this.serverIndex = ServerConfig.getInstance().getServer();
        }
        this.sortRules = DEFAULT_SORT_RULES;
    }
}

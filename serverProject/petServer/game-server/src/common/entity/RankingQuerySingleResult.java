package common.entity;

import lombok.ToString;

/**
 * 排行榜单个查询结果
 *
 * @author xiao_FL
 * @date 2019/8/16
 */
@ToString
public class RankingQuerySingleResult extends RankingScore {
    /**
     * 排名
     */
    private Integer ranking;

    public Integer getRanking() {
        return ranking;
    }

    public void setRanking(Integer ranking) {
        this.ranking = ranking;
    }
}

package model.ranking.settle;

import java.util.List;
import protocol.Common.Reward;

/**
 * @author huhan
 * @date 2020/12/17
 */
public interface RankingRewards {

    /**
     * 排行榜奖励起始排名
     * @return
     */
    int getStartRanking();


    /**
     * 排行榜奖励借宿排名
     * @return
     */
    int getEndRanking();

    /**
     * 排名奖励
     * @return
     */
    List<Reward> getRankingRewards();
}

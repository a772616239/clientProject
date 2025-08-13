package model.ranking.settle;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import protocol.Common.Reward;

/**
 * @author huhan
 * @date 2020/12/17
 */
@Getter
@Setter
@AllArgsConstructor
public class RankingRewardsImpl implements RankingRewards {
    private int startRanking;
    private int endRanking;
    private List<Reward> rankingRewards;
}

package model.ranking.ranking;

import model.ranking.RankingUtils;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;

/**
 * @author huhan
 * @date 2020/12/16
 */
public class DemonDescendsScoreRanking extends AbstractRanking{

    @Override
    public long getLocalScore(String playerIdx) {
        long activityId = RankingUtils.getActivityIdByRankingName(getRankingName());
        if (activityId != 0) {
            targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
            return entity.getDemonDescendsInfoBuilder(activityId).getScore();
        }

        return 0;
    }
}

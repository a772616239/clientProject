package model.ranking.ranking.arena;

import model.ranking.RankingUtils;
import model.ranking.ranking.AbstractRanking;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import protocol.Activity.EnumRankingType;

/**
 * @author huhan
 * @date 2020/12/16
 */
public class ArenaGainScoreRanking extends AbstractRanking {

    @Override
    public long getLocalScore(String playerIdx) {
        long activityId = RankingUtils.getActivityIdByRankingName(getRankingName());
        if (activityId != 0) {
            targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
            if (entity != null) {
                entity.getRankingActivityScore(activityId, EnumRankingType.ERT_ArenaGainScore);
            }
        }
        return 0;
    }

    @Override
    public void updateTotalPlayerScore() {
    }
}

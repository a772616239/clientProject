package model.ranking.ranking;

import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;

import java.util.Map;

/**
 * 增量类活动排行榜
 */
public class AbstractIncrActivityRanking extends AbstractRanking  {

    @Override
    public long getLocalScore(String playerIdx) {
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            return 0;
        }
        Map.Entry<Long, Long> newestEntry = entity.getDb_Builder().getRankingTargetMap().entrySet().stream()
                .max((o1, o2) -> (int) (o1.getKey() - o2.getKey())).orElse(null);
        if (newestEntry == null) {
            return 0;
        }
        return newestEntry.getValue();
    }
}

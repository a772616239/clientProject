package model.ranking.ranking;

import common.tick.GlobalTick;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;

public class PetAvoidanceRanking extends AbstractRanking {
    @Override
    public long getLocalScore(String playerIdx) {
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);

        return entity == null ? 0 : entity.getDb_Builder().getSpecialInfo().getPetAvoidance().getMaxScore();
    }

    @Override
    public long getLocalSubScore(String playerIdx) {
        return GlobalTick.getInstance().getCurrentTime();
    }
}

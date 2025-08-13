package model.ranking.ranking;

import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;

public class RichManRanking extends AbstractRanking {

    @Override
    public long getLocalScore(String playerIdx) {
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        return entity == null ? 0 : entity.getDb_Builder().getSpecialInfo().getRichMan().getCycle();
    }

}

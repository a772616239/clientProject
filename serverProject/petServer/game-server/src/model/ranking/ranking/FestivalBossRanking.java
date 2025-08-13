package model.ranking.ranking;

import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import protocol.TargetSystemDB;

import java.util.Comparator;
import java.util.Map;

public class FestivalBossRanking extends AbstractRanking {

    @Override
    public long getLocalScore(String playerIdx) {
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            return 0;
        }
        Map.Entry<Long, TargetSystemDB.DB_FestivalBoss> newestEntry = entity.getDb_Builder().getFestivalBossInfoMap().entrySet().stream().max((o1, o2) -> (int) (o1.getKey() - o2.getKey())).orElse(null);
        if (newestEntry == null) {
            return 0;
        }
        return newestEntry.getValue().getCumeScore();
    }

}

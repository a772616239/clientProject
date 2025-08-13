package model.ranking.ranking;

import model.matcharena.dbCache.matcharenaCache;
import model.matcharena.entity.matcharenaEntity;

/**
 * @author huhan
 * @date 2021/05/18
 */
public class MatchArenaLocalRanking extends AbstractRanking {

    @Override
    public long getLocalScore(String playerIdx) {
        matcharenaEntity entity = matcharenaCache.getInstance().getEntity(playerIdx);
        return entity == null ? 0 : entity.getDbBuilder().getRankMatchArena().getScore();
    }
}

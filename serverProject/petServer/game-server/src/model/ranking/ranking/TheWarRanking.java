package model.ranking.ranking;

import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;

/**
 * @author huhan
 * @date 2020/12/7
 */
public class TheWarRanking extends AbstractRanking {

    @Override
    public long getLocalScore(String playerIdx) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return 0L;
        }
        return player.getDb_data().getTheWarData().getKillMonsterCount();
    }
}

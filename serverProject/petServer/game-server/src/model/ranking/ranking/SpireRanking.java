package model.ranking.ranking;

import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;

/**
 * @author huhan
 * @date 2020/12/10
 */
public class SpireRanking extends AbstractRanking implements TargetRewardRanking {

    @Override
    public long getLocalScore(String playerIdx) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        return player == null ? 0 : player.getDb_data().getEndlessSpireInfo().getMaxSpireLv();
    }
}

package model.ranking.ranking.crossarena;

import model.crossarena.CrossArenaManager;
import protocol.CrossArena;

/**
 * 擂台积分榜
 */
public class CrossArenaScoreRanking extends AbstractCrossArenaRanking {

    @Override
    public long getLocalScore(String playerIdx) {
        return CrossArenaManager.getInstance().getPlayerDBInfo(playerIdx,CrossArena.CrossArenaDBKey.LT_GRADECUR);
    }
}
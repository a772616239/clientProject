package model.ranking.ranking.crossarena;

import model.crossarena.CrossArenaManager;
import protocol.CrossArena;

/**
 * 擂台连胜榜
 */
public class CrossArenaSerialWinRanking extends AbstractCrossArenaRanking {

    @Override
    public long getLocalScore(String playerIdx) {
        return CrossArenaManager.getInstance().getPlayerDBInfo(playerIdx, CrossArena.CrossArenaDBKey.LT_GRADECUR);
    }
}
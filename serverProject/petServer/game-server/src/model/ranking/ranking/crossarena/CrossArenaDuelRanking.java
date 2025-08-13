package model.ranking.ranking.crossarena;

import model.crazyDuel.CrazyDuelManager;

/**
 * 疯狂对决榜
 */
public class CrossArenaDuelRanking extends AbstractCrossArenaRanking {

    @Override
    public long getLocalScore(String playerIdx) {
        return CrazyDuelManager.getInstance().findPlayerScore(playerIdx);
    }
}

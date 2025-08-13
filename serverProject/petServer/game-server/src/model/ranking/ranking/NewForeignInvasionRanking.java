package model.ranking.ranking;

import cfg.NewForeignInvasionConfig;
import cfg.NewForeignInvasionConfigObject;
import common.GameConst;
import model.foreignInvasion.dbCache.foreigninvasionCache;
import util.TimeUtil;

/**
 * @author huhan
 * @date 2020/12/2
 */
public class NewForeignInvasionRanking extends AbstractRanking {

    @Override
    public long getLocalScore(String playerIdx) {
        return foreigninvasionCache.getInstance().getPlayerCurScore(playerIdx);
    }

    @Override
    public void updateTotalPlayerScore() {

    }
}

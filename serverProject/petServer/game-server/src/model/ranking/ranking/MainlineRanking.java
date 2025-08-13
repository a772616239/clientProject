package model.ranking.ranking;

import model.mainLine.dbCache.mainlineCache;

/**
 * @author huhan
 * @date 2020/12/8
 */
public class MainlineRanking extends AbstractRanking implements TargetRewardRanking{

    @Override
    public long getLocalScore(String playerIdx) {
        return mainlineCache.getInstance().getPlayerCurNode(playerIdx);
    }
}

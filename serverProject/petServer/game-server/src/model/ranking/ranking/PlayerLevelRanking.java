package model.ranking.ranking;

import model.player.util.PlayerUtil;

/**
 * @author huhan
 * @date 2020/12/16
 */
public class PlayerLevelRanking extends AbstractRanking implements TargetRewardRanking{

    @Override
    public long getLocalScore(String playerIdx) {
        return PlayerUtil.queryPlayerLv(playerIdx);
    }
}

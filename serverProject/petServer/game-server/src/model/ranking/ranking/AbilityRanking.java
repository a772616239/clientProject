package model.ranking.ranking;

import model.pet.dbCache.petCache;

/**
 * @author huhan
 * @date 2020/12/16
 */
public class AbilityRanking extends AbstractRanking implements TargetRewardRanking{

    @Override
    public long getLocalScore(String playerIdx) {
        return petCache.getInstance().totalAbility(playerIdx);
    }
}

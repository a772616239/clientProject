package model.ranking.ranking;

import model.pet.dbCache.petCache;

/**
 * 单个宠物战力排行榜
 * @author huhan
 * @date 2020/12/16
 */
public class PetAbilityRanking extends AbstractSelfUpdateRanking{

    @Override
    public long getLocalScore(String playerIdx) {
        return petCache.getInstance().getPetMaxAbility(playerIdx);
    }
}

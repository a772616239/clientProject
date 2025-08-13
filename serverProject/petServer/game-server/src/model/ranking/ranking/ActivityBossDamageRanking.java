package model.ranking.ranking;

/**
 * @author huhan
 * @date 2020/12/16
 */
public class ActivityBossDamageRanking extends AbstractRanking{

    /**
     * boss战伤害未存储
     * @param playerIdx
     * @return
     */
    @Override
    public long getLocalScore(String playerIdx) {
        return 0;
    }
}

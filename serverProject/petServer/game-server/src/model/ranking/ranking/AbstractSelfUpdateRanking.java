package model.ranking.ranking;

/**
 * 排行榜自动更新的排行榜 不需要触发玩家更新
 *ra
 * @author huhan
 * @date 2020/12/16
 */
public abstract class AbstractSelfUpdateRanking extends AbstractRanking {

    @Override
    public void updateRanking() {
        updateTotalPlayerScore();
        super.updateRanking();
    }

}

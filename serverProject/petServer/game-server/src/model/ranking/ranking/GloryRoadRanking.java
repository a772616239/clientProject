package model.ranking.ranking;

import model.gloryroad.dbCache.gloryroadCache;
import model.gloryroad.entity.gloryroadEntity;

/**
 * @author huhan
 * @date 2021/4/2
 */
public class GloryRoadRanking extends AbstractRanking {

    @Override
    public long getLocalScore(String playerIdx) {
        gloryroadEntity entity = gloryroadCache.getByIdx(playerIdx);
        return entity == null ? 0 : entity.getDbBuilder().getWinCount();
    }

    @Override
    public void updateTotalPlayerScore() {
        //荣耀之路排行榜不需要更新所有
    }
}

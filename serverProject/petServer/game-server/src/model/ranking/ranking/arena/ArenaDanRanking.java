package model.ranking.ranking.arena;

import common.entity.RankingUpdateRequest;
import java.util.List;
import model.arena.dbCache.arenaCache;
import model.arena.entity.arenaEntity;
import model.ranking.ranking.AbstractRanking;

/**
 * @author huhan
 * @date 2020/12/16
 */
public class ArenaDanRanking extends AbstractRanking {

    @Override
    public void init() {
        super.init();
        updateTotalPlayerScore();
    }

    @Override
    public long getLocalScore(String playerIdx) {
        return arenaCache.getInstance().getPlayerDan(playerIdx);
    }

    @Override
    public List<Integer> getSortRules() {
        return RankingUpdateRequest.SORT_RULES_DES_DES;
    }

    @Override
    public long getLocalSubScore(String playerIdx) {
        arenaEntity entity = arenaCache.getInstance().getEntity(playerIdx);
        return entity == null ? 0 : entity.getDbBuilder().getScore();
    }
}

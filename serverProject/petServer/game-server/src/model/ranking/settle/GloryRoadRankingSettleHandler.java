package model.ranking.settle;

import common.SyncExecuteFunction;
import common.entity.RankingQuerySingleResult;
import java.util.Collection;
import java.util.List;
import model.gloryroad.dbCache.gloryroadCache;
import model.gloryroad.entity.gloryroadEntity;
import protocol.Activity.EnumRankingType;
import protocol.Common.RewardSourceEnum;

/**
 * @author huhan
 * @date 2021/4/3
 */
public class GloryRoadRankingSettleHandler extends MailRankingSettleHandler {

    public GloryRoadRankingSettleHandler(EnumRankingType rankingType, Collection<? extends RankingRewards> rankingRewards,
                                         int mailTemplate, RewardSourceEnum rewardSource) {
        super(rankingType, rankingRewards, mailTemplate, rewardSource);
    }

    @Override
    protected void afterSettle(List<RankingQuerySingleResult> rankingTotalInfo) {
        for (RankingQuerySingleResult result : rankingTotalInfo) {
            gloryroadEntity entity = gloryroadCache.getByIdx(result.getPrimaryKey());
            if (entity != null) {
                SyncExecuteFunction.executeConsumer(entity, e -> {
                    entity.setTopRank(result.getRanking());
                });
            }
        }
        super.afterSettle(rankingTotalInfo);
    }
}

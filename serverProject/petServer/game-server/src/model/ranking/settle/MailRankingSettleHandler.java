package model.ranking.settle;

import common.entity.RankingQuerySingleResult;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import model.player.dbCache.playerCache;
import model.ranking.RankingManager;
import model.ranking.RankingUtils;
import org.apache.commons.collections4.CollectionUtils;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import protocol.Activity.EnumRankingType;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;

/**
 * @author huhan
 * @date 2020/12/3
 */
@Getter
public class MailRankingSettleHandler implements RankingSettleHandler {

    private final EnumRankingType rankingType;

    /**
     * 要结算的排行榜名
     */
    private final String settleRankingName;

    /**
     * 排行榜奖励
     */
    private final Collection<? extends RankingRewards> rankingRewards;

    /**
     * 使用的邮件模板
     */
    private final int mailTemplate;

    private final RewardSourceEnum rewardSource;

    /**
     * 未上榜结算的奖励模板，可选
     */
    private int unRankingTemplate;

    /**
     * 需要结算的所有玩家，可选
     */
    private Set<String> totalPlayerIdx;

    public MailRankingSettleHandler(EnumRankingType rankingType, String settleRankingName, Collection<? extends RankingRewards> rankingRewards,
                                    int mailTemplate, RewardSourceEnum rewardSource) {
        this.rankingType = rankingType;
        this.settleRankingName = settleRankingName;
        this.rankingRewards = rankingRewards;
        this.mailTemplate = mailTemplate;
        this.rewardSource = rewardSource;
    }

    public MailRankingSettleHandler(EnumRankingType rankingType, Collection<? extends RankingRewards> rankingRewards,
                                    int mailTemplate, RewardSourceEnum rewardSource) {
        this(rankingType, RankingUtils.getRankingTypeDefaultName(rankingType), rankingRewards, mailTemplate, rewardSource);
    }

    public MailRankingSettleHandler(EnumRankingType rankingType, String settleRankingName, Collection<? extends RankingRewards> rankingRewards,
                                    int mailTemplate, RewardSourceEnum rewardSource, Collection<String> totalPlayerIdx,
                                    int unRankingTemplate) {
        this(rankingType, settleRankingName, rankingRewards, mailTemplate, rewardSource);
        if (CollectionUtils.isNotEmpty(totalPlayerIdx)) {
            this.totalPlayerIdx = new HashSet<>(totalPlayerIdx);
        }
        this.unRankingTemplate = unRankingTemplate;
    }

    public MailRankingSettleHandler(EnumRankingType rankingType, Collection<? extends RankingRewards> rankingRewards,
                                    int mailTemplate, RewardSourceEnum rewardSource, Collection<String> totalPlayerIdx,
                                    int unRankingTemplate) {
        this(rankingType, RankingUtils.getRankingTypeDefaultName(rankingType), rankingRewards, mailTemplate,
                rewardSource, totalPlayerIdx, unRankingTemplate);
    }

    @Override
    public void settleRanking() {
        beforeSettle();

        List<RankingQuerySingleResult> rankingTotalInfo =
                RankingManager.getInstance().getRankingTotalInfo(this.rankingType, this.settleRankingName);
        if (CollectionUtils.isEmpty(rankingTotalInfo)) {
            LogUtil.error("AbstractSettleRankingHandler.settleRanking, ranking size is empty, ranking name:" + this.settleRankingName);
            return;
        }
        if (CollectionUtils.isEmpty(totalPlayerIdx)) {
            commonSettle(rankingTotalInfo);
        } else {
            totalPlayerSettle(rankingTotalInfo);
        }

        afterSettle(rankingTotalInfo);
    }

    private void commonSettle(List<RankingQuerySingleResult> rankingTotalInfo) {
        int ranking = getNeedSettleMaxRanking();
        for (RankingQuerySingleResult result : rankingTotalInfo) {
            if (ranking != -1 && result.getRanking() > ranking) {
                continue;
            }
            doRankingRewards(result, getRankingRewards(result.getRanking()));
        }
    }

    private void totalPlayerSettle(List<RankingQuerySingleResult> rankingTotalInfo) {
        for (RankingQuerySingleResult result : rankingTotalInfo) {
            doRankingRewards(result, getRankingRewards(result.getRanking()));
            this.totalPlayerIdx.remove(result.getPrimaryKey());
        }

        //未上榜玩家结算
        for (String playerIdx : this.totalPlayerIdx) {
            doUnRankingRewards(playerIdx, getRankingRewards(-1));
        }
    }

    private void beforeSettle() {
//        RankingManager.getInstance().stopUpdatePlayerScore(this.settleRankingName);
        RankingManager.getInstance().directUpdateRanking(this.rankingType, this.settleRankingName);
    }

    protected void afterSettle(List<RankingQuerySingleResult> rankingTotalInfo) {
//        RankingManager.getInstance().startUpdatePlayerScore(this.settleRankingName);
        LogUtil.info("MailRankingSettleHandler, settle ranking finished, rankingName:" + this.settleRankingName);
    }

    protected List<Reward> getRankingRewards(int ranking) {
        if (CollectionUtils.isEmpty(this.rankingRewards)) {
            LogUtil.error("AbstractSettleRankingHandler.getRankingRewards, ranking reward list is empty,");
            return null;
        }
        for (RankingRewards rankingReward : this.rankingRewards) {
            if (GameUtil.inScope(rankingReward.getStartRanking(), rankingReward.getEndRanking(), ranking)) {
                return rankingReward.getRankingRewards();
            }
        }
        return null;
    }

    /**
     * 获取需要结算的最大排名, -1结算所有
     *
     * @return
     */
    protected int getNeedSettleMaxRanking() {
        if (CollectionUtils.isEmpty(this.rankingRewards)) {
            LogUtil.error("AbstractSettleRankingHandler.getRankingRewards, ranking reward list is empty,");
            return 0;
        }
        int settleMaxRanking = 0;
        for (RankingRewards rankingReward : rankingRewards) {
            if (rankingReward.getEndRanking() == -1) {
                settleMaxRanking = -1;
                break;
            }

            if (rankingReward.getEndRanking() > settleMaxRanking) {
                settleMaxRanking = rankingReward.getEndRanking();
            }
        }
        LogUtil.error("AbstractSettleRankingHandler.getNeedSettleMaxRanking, settleMaxRanking:" + settleRankingName
                + ", rankingName:" + this.settleRankingName);
        return settleMaxRanking;
    }

    public void doRankingRewards(RankingQuerySingleResult singleResult, List<Reward> rewards) {
        if (singleResult == null || playerCache.getByIdx(singleResult.getPrimaryKey()) == null) {
            return;
        }
        Reason reason = ReasonManager.getInstance().borrowReason(getRewardSource());
        EventUtil.triggerAddMailEvent(singleResult.getPrimaryKey(), this.mailTemplate, rewards
                , reason, String.valueOf(singleResult.getRanking()));
    }

    /**
     * 未上榜奖励
     *
     * @param playerIdx
     * @param rewards
     */
    public void doUnRankingRewards(String playerIdx, List<Reward> rewards) {
        Reason reason = ReasonManager.getInstance().borrowReason(getRewardSource());
        EventUtil.triggerAddMailEvent(playerIdx, this.unRankingTemplate, rewards, reason);
    }
}

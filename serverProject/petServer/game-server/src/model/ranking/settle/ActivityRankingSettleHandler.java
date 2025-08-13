package model.ranking.settle;

import cfg.DemonDescendsConfig;
import cfg.MailTemplateUsed;
import common.GameConst;
import common.entity.RankingQuerySingleResult;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import model.activity.ActivityUtil;
import model.player.dbCache.playerCache;
import model.player.util.PlayerUtil;
import model.ranking.RankingUtils;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import platform.logs.entity.RankingSettleLog;
import protocol.Activity.EnumRankingType;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.Server.ServerActivity;
import util.EventUtil;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020/12/17
 */
@Getter
@Setter
public class ActivityRankingSettleHandler extends MailRankingSettleHandler {

    private ServerActivity activity;

    public ActivityRankingSettleHandler(ServerActivity activity) {
        super(activity.getRankingType(), RankingUtils.getActivityRankingName(activity), ActivityUtil.getRankingRewards(activity),
                getRankSettleMailTemplate(activity), getRankingTypeRewardResource(activity));
        this.activity = activity;
    }

    @Override
    public void doRankingRewards(RankingQuerySingleResult singleResult, List<Reward> rewards) {
        if (singleResult == null || playerCache.getByIdx(singleResult.getPrimaryKey()) == null) {
            return;
        }
        String titleParam = GameUtil.getLanguageStr(activity.getTitle(), PlayerUtil.queryPlayerLanguage(singleResult.getPrimaryKey()));
        Reason reason = ReasonManager.getInstance().borrowReason(getRewardSource());
        EventUtil.triggerAddMailEvent(singleResult.getPrimaryKey(), getMailTemplate(), rewards
                , reason, titleParam, String.valueOf(singleResult.getRanking()));
    }

    @Override
    protected void afterSettle(List<RankingQuerySingleResult> rankingTotalInfo) {
        LogService.getInstance().submit(new RankingSettleLog(this.activity, rankingTotalInfo));
        super.afterSettle(rankingTotalInfo);
    }

    private static int getRankSettleMailTemplate(ServerActivity activity) {
        if (activity == null) {
            return MailTemplateUsed.getById(GameConst.CONFIG_ID).getRankingactivity();
        }
        if (activity.getRankingType() == EnumRankingType.ERT_ActivityBoss_Damage) {
            return MailTemplateUsed.getById(GameConst.CONFIG_ID).getBossdamagerank();
        } else if (activity.getRankingType() == EnumRankingType.ERT_DemonDescendsScore) {
            return DemonDescendsConfig.getById(GameConst.CONFIG_ID).getScorerankingtemplate();
        }
        return MailTemplateUsed.getById(GameConst.CONFIG_ID).getRankingactivity();
    }

    private static RewardSourceEnum getRankingTypeRewardResource(ServerActivity activity) {
        if (activity == null) {
            return RewardSourceEnum.RSE_Null;
        }

        if (activity.getRankingType() == EnumRankingType.ERT_DemonDescendsScore) {
            return RewardSourceEnum.RSE_DemonDescends;
        } else if (activity.getRankingType() == EnumRankingType.ERT_ActivityBoss_Damage) {
            return RewardSourceEnum.RSE_ActivityBoss;
        } else {
            return RewardSourceEnum.RSE_Activity;
        }
    }
}

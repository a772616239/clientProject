package server.handler.monthCard;

import cfg.MailTemplateUsed;
import cfg.MonthlyCardConfig;
import cfg.MonthlyCardConfigObject;
import common.GameConst;
import model.reward.RewardUtil;
import platform.logs.ReasonManager;
import protocol.Common;
import protocol.Common.Reward;
import protocol.MonthCard;
import util.EventUtil;
import util.LogUtil;

import java.util.List;

/**
 * @Description
 * @Author hanx
 * @Date2020/4/28 0028 17:35
 **/
public class MonthCardUtil {

    /**
     * 发放月卡每日奖励
     *
     * @param monthCard
     * @return
     */
    public static void doMonthCardDailyReward(String playerId, int monthCard, int remainDays) {

        if ((monthCard != MonthCard.MonthTypeEnum.MTE_Advanced_VALUE
                && monthCard != MonthCard.MonthTypeEnum.MTE_Normal_VALUE) || remainDays < 0) {

            LogUtil.error("doMonthCardDailyReward error,playerId[" + playerId + "],monthCard["
                    + monthCard + "],remainDays[" + remainDays);
            return;
        }
        LogUtil.debug("doMonthCardDailyReward, playerId[" + playerId + "],monthCard:[" + monthCard + "]");
        EventUtil.triggerAddMailEvent(playerId, getMailTemplateByCardType(monthCard), getDailyRewardByCarType(monthCard), getReasonByCarType(monthCard), remainDays + "");
    }

    /**
     * 通过月卡id获取每日奖励
     *
     * @param monthCard
     * @return
     */
    public static List<Reward> getDailyRewardByCarType(int monthCard) {
        MonthlyCardConfigObject config = MonthlyCardConfig.getById(monthCard);

        return RewardUtil.parseRewardIntArrayToRewardList(config.getEverydayrewards());
    }

    /**
     * 通过月卡id获取奖励发放原因
     *
     * @param monthCard
     * @return
     */
    public static ReasonManager.Reason getReasonByCarType(int monthCard) {
        if (monthCard == MonthCard.MonthTypeEnum.MTE_Advanced_VALUE) {
            return ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_AdvanceMonthCardReward);
        }
        return ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_NormalMonthCardReward);
    }

    /**
     * 通过月卡id获取奖励发放模板
     *
     * @param monthCard
     * @return
     */
    public static int getMailTemplateByCardType(int monthCard) {
        if (monthCard == MonthCard.MonthTypeEnum.MTE_Advanced_VALUE) {
            return MailTemplateUsed.getById(GameConst.CONFIG_ID).getMonthhighcard();
        }
        return MailTemplateUsed.getById(GameConst.CONFIG_ID).getMonthcommoncard();
    }

}

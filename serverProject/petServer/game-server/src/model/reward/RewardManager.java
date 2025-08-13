package model.reward;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.util.CollectionUtils;

import cfg.MailTemplateUsed;
import common.GameConst;
import common.GlobalData;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import platform.logs.ReasonManager.Reason;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.Common.RewardTypeEnum;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;

public class RewardManager {
    private static final RewardManager ourInstance = new RewardManager();

    public static RewardManager getInstance() {
        return ourInstance;
    }

    /**
     * @param playerIdx
     * @param rewardId
     * @param source    奖励来源
     * @return 返回奖励列表, null时发放失败
     */
    public List<Reward> doRewardByRewardId(String playerIdx, int rewardId, Reason source, boolean show) {
        if (playerIdx == null) {
            LogUtil.info("RewardManager.doReward, params error, playerIdx is null");
            return null;
        }

        List<Reward> rewards = RewardUtil.getRewardsByRewardId(rewardId);
        if (rewards == null) {
            LogUtil.warn("reward cfg reward is null, rewardId = " + rewardId);
            return null;
        }

        if (!doRewardByList(playerIdx, rewards, source, show)) {
            return null;
        }

        return rewards;
    }

    public boolean doRewardByList(String playerIdx, List<Reward> rewardList, Reason reason, boolean show) {
        LogUtil.debug("RewardManager.doRewardByList, playerIdx:" + playerIdx
                + ",rewardList:" + RewardUtil.toJsonStr(rewardList) + ",reason:" + reason.toString());
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            LogUtil.error("playerIdx" + playerIdx + "is not exist");
            return false;
        }

        if (GameUtil.collectionIsEmpty(rewardList)) {
            return true;
        }

        List<Reward> canDoReward = getCanDoReward(playerIdx, RewardUtil.mergeReward(rewardList), reason);
        Map<RewardTypeEnum, List<Reward>> classifyMap = RewardUtil.classifyByRewardType(canDoReward);

        if (classifyMap == null || classifyMap.isEmpty()) {
            LogUtil.warn("RewardManager.doRewardByList, playerIdx:" + playerIdx + ", do rewardList is null");
            return true;
        }

        for (Map.Entry<RewardTypeEnum, List<Reward>> entry : classifyMap.entrySet()) {
            triggerRewardEvent(playerIdx, entry.getKey(), entry.getValue(), reason);
        }

        if (show) {
            GlobalData.getInstance().sendDisRewardMsg(playerIdx, RewardUtil.excludeAutoUseItem(canDoReward), reason.getSourceEnum());
        }

        //资源发放统计
        RewardSourceMonitorManager.getInstance().recordRewards(playerIdx, reason);
        return true;
    }

    private boolean triggerRewardEvent(String playerIdx, RewardTypeEnum rewardType, List<Reward> typeRewards, Reason reason) {
        if (playerIdx == null || rewardType == null || rewardType == RewardTypeEnum.RTE_Null || typeRewards == null || typeRewards.isEmpty()) {
            LogUtil.warn("model.reward.RewardManager.triggerEvent, error params");
            return false;
        }

        switch (rewardType) {
            case RTE_Gold:
            case RTE_Diamond:
            case RTE_Coupon:
            case RTE_HolyWater:
                EventUtil.triggerAddCurrency(playerIdx, rewardType, RewardUtil.getAllCount(rewardType, typeRewards), reason);
                break;
            case RTE_Item:
                EventUtil.triggerAddItemEvent(playerIdx,
                        RewardUtil.getCfgIdCountMap(rewardType, typeRewards), reason);
                break;
            case RTE_PetFragment:
                EventUtil.triggerAddPetFragment(playerIdx,
                        RewardUtil.getCfgIdCountMap(rewardType, typeRewards), reason);
                sendRewardMarquee(playerIdx, typeRewards, reason);
                break;
            case RTE_Pet:
                EventUtil.triggerAddPet(playerIdx, RewardUtil.getCfgIdCountMap(rewardType, typeRewards), reason);
                sendRewardMarquee(playerIdx, typeRewards, reason);
                break;
            case RTE_Rune:
                EventUtil.triggerAddPetRune(playerIdx, RewardUtil.getCfgIdCountMap(rewardType, typeRewards), reason);
                break;
            case RTE_Avatar:
                EventUtil.triggerAddAvatar(playerIdx, RewardUtil.getCfgIdList(rewardType, typeRewards), reason);
                break;
            case RTE_VIPEXP:
                EventUtil.triggerAddVIPExp(playerIdx, RewardUtil.getAllCount(rewardType, typeRewards));
                break;
            case RTE_EXP:
                EventUtil.triggerAddExp(playerIdx, RewardUtil.getAllCount(rewardType, typeRewards));
                break;
//            case RTE_MistIntegral:
//                EventUtil.triggerAddMistIntegral(playerIdx, RewardUtil.getAllCount(rewardType, typeRewards), reason);
//                break;
            case RTE_AvatarBorder:
                EventUtil.triggerAddAvatarBorder(playerIdx, RewardUtil.getCfgIdList(rewardType, typeRewards), reason);
                break;
            case RTE_Gem:
                EventUtil.triggerAddGem(playerIdx, RewardUtil.getCfgIdCountMap(rewardType, typeRewards), reason);
                break;
            case RTE_Inscription:
                EventUtil.triggerAddInscription(playerIdx, RewardUtil.getCfgIdCountMap(rewardType, typeRewards), reason);
                break;
            case RTE_PointInstance:
                EventUtil.addPointInstance(playerIdx, RewardUtil.getAllCount(RewardTypeEnum.RTE_PointInstance, typeRewards), reason);
                break;
            case RTE_MineExp:
//                EventUtil.triggerAddMineExp(playerIdx, RewardUtil.getAllCount(rewardType, typeRewards), reason);
                break;
            case RTE_RechargeProduct:
                EventUtil.triggerRechargeProduct(playerIdx, RewardUtil.getCfgIdCountMap(rewardType, typeRewards), reason);
                break;
            case RTE_NewTitleSystem:
                EventUtil.addNewTitles(playerIdx, RewardUtil.getCfgIdList(rewardType, typeRewards), reason);
                break;
            case RTE_Train:
                EventUtil.addTrainItem(playerIdx, RewardUtil.getCfgIdCountMap(rewardType, typeRewards), reason);
            	break;
            case RTE_CrossArenaGrade:
            	EventUtil.addCrossGradeGrade(playerIdx, RewardUtil.getAllCount(rewardType, typeRewards), reason);
            	break;
            case RTE_MistMoveEffect:
            	EventUtil.addMistMoveEffect(playerIdx, RewardUtil.getCfgIdList(rewardType, typeRewards), reason);
            	break;
            default:
                break;
        }
        return true;
    }

    public boolean doReward(String playerIdx, Reward reward, Reason source, boolean show) {
        return doRewardByList(playerIdx, Collections.singletonList(reward), source, show);
    }

    /**
     * 返回可以发放的奖励, 其他不可发放的奖励通过邮件发送
     */
    private List<Reward> getCanDoReward(String playerIdx, List<Reward> rewardList, Reason reason) {
        if (rewardList == null || rewardList.isEmpty()) {
            return null;
        }

        List<Reward> remainReward = new ArrayList<>();
        List<Reward> canDoReward = RewardUtil.getCanDoReward(playerIdx, rewardList, remainReward, reason);
        if (!remainReward.isEmpty()) {
            rewardListByMail(playerIdx, remainReward, reason);
        }
        return canDoReward;
    }

    private boolean rewardListByMail(String playerIdx, List<Reward> rewardList, Reason reason) {
        if (playerIdx == null || GameUtil.collectionIsEmpty(rewardList)) {
            LogUtil.debug("RewardManager.rewardByMail,error param rewardList is empty,reason:"
                    + (reason != null ? reason.getSourceEnum().toString() : "null reason"));
            return false;
        }
        return EventUtil.triggerAddMailEvent(playerIdx, MailTemplateUsed.getById(GameConst.CONFIG_ID).getBagfull(),
                rewardList, reason, RewardUtil.getBagFullName(playerIdx, rewardList));
    }

    /**
     * 奖励跑马灯,只播放
     *
     * @param playerIdx
     * @param rewardList
     */
    private void sendRewardMarquee(String playerIdx, List<Reward> rewardList, Reason reason) {
        if (playerIdx == null || CollectionUtils.isEmpty(rewardList) || reason == null) {
            return;
        }

        //暂且只发放抽卡和远古召唤
        if (needMarquee(reason.getSourceEnum())) {
            for (Reward reward : rewardList) {
                if (reward.getRewardType() == RewardTypeEnum.RTE_Pet) {
                    RewardUtil.sendPetGainReward(playerIdx, reward.getId(), reward.getCount());
                } else if (reward.getRewardType() == RewardTypeEnum.RTE_PetFragment) {
                    RewardUtil.sendPetFragmentGainReward(playerIdx, reward.getId(), reward.getCount());
                }
            }
        }
    }

    private static final Set<RewardSourceEnum> NEED_MARQUEE_REWARD_SOURCE;

    static {
        Set<RewardSourceEnum> tempSet = new HashSet<>();
        tempSet.add(RewardSourceEnum.RSE_DrawCard_FriensShip);
        tempSet.add(RewardSourceEnum.RSE_DrawCard_Common);
        tempSet.add(RewardSourceEnum.RSE_DrawCard_High);
        tempSet.add(RewardSourceEnum.RSE_AncientCall);
        tempSet.add(RewardSourceEnum.RSE_DrawCard_New);

        NEED_MARQUEE_REWARD_SOURCE = Collections.unmodifiableSet(tempSet);
    }

    public static boolean needMarquee(RewardSourceEnum rewardSource) {
        if (rewardSource == null) {
            return false;
        }
        return NEED_MARQUEE_REWARD_SOURCE.contains(rewardSource);
    }
}

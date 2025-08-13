package server.handler.activity.pay;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.activity.ActivityManager;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardManager;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import org.apache.commons.collections4.CollectionUtils;
import platform.logs.ReasonManager;
import protocol.Activity;
import protocol.Activity.ActivityTypeEnum;
import protocol.Activity.CS_ClaimDayDayRechargeReward;
import protocol.Activity.RewardList;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Server.ServerActivity;
import protocol.TargetSystemDB.DB_DayDayRecharge;
import protocol.TargetSystemDB.DB_DayDayRecharge.Builder;
import util.GameUtil;
import util.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static protocol.RetCodeId.RetCodeEnum.*;

@MsgId(msgId = MsgIdEnum.CS_ClaimDayDayRechargeReward_VALUE)
public class ClaimDayDayRechargeRewardHandler extends AbstractBaseHandler<CS_ClaimDayDayRechargeReward> {
    @Override
    protected Activity.CS_ClaimDayDayRechargeReward parse(byte[] bytes) throws Exception {
        return Activity.CS_ClaimDayDayRechargeReward.parseFrom(bytes);
    }


    private static final int CLAIM_FREE = 0;

    @Override
    protected void execute(GameServerTcpChannel gsChn, Activity.CS_ClaimDayDayRechargeReward req, int i) {
        Activity.SC_ClaimDayDayRechargeReward.Builder result = Activity.SC_ClaimDayDayRechargeReward.newBuilder();
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");

        LogUtil.info("player:{} ClaimDayDayRechargeReward ,req:{}", playerIdx, req);
        playerEntity player = playerCache.getByIdx(playerIdx);
        targetsystemEntity targetsystemEntity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (targetsystemEntity == null || player == null) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimDayDayRechargeReward_VALUE, result);
            return;
        }
        List<ServerActivity> activities = ActivityManager.getInstance().getOpenActivitiesByType(ActivityTypeEnum.ATE_DayDayRecharge);
        if (CollectionUtils.isEmpty(activities)) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Activity_NotOpen));
            gsChn.send(MsgIdEnum.SC_ClaimDayDayRechargeReward_VALUE, result);
            return;
        }
        ServerActivity serverActivity = activities.get(0);
        int claimIndex = getClaimIndex(req, targetsystemEntity);

        int claimIndexInPlayer = getRecordIndexInPlayer(req.getClaimType(), claimIndex);

        if (player.alreadyClaimed(serverActivity.getActivityId(), claimIndexInPlayer)) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Feats_RewardAlreadyClaim));
            gsChn.send(MsgIdEnum.SC_ClaimDayDayRechargeReward_VALUE, result);
            return;
        }

        RetCodeEnum codeEnum = SyncExecuteFunction.executeFunction(targetsystemEntity, entity -> {

            DB_DayDayRecharge db_dayDayRecharge = targetsystemEntity.getDb_Builder().getSpecialInfo().getDayDayRecharge();

            //领取免费宝箱
            if (req.getClaimType() == CLAIM_FREE) {
                //当日已领取
                if (db_dayDayRecharge.getClaimTodayFree()) {
                    return RCE_Activity_RewardAlreadyClaim;
                }

                List<RewardList> freeRewardsList = serverActivity.getDayDayRecharge().getFreeRewardsList();
                if (claimIndex >= freeRewardsList.size()) {
                    return RCE_Activity_RewardAlreadyClaim;
                }

                targetsystemEntity.getDb_Builder().getSpecialInfoBuilder().getDayDayRechargeBuilder().setClaimTodayFree(true);


                RewardManager.getInstance().doRewardByList(playerIdx, freeRewardsList.get(claimIndex).getRewardList(),
                        ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_DayDayRecharge_Free), true);
                LogUtil.info("player:{} ClaimDayDayRechargeReward free reward success,claimIndex:{}", playerIdx, claimIndex);

            } else {
                //领取支付宝箱
                List<Integer> canClaimIndexList = db_dayDayRecharge.getCanClaimIndexList();
                List<Integer> claimedIndexList = db_dayDayRecharge.getClaimedIndexList();
                if (claimedIndexList.contains(claimIndex)) {
                    return RCE_Activity_RewardAlreadyClaim;
                }
                if (!canClaimIndexList.contains(claimIndex)) {
                    return RCE_Activity_MissionCanNotClaim;
                }
                List<RewardList> rechargeRewardsList = serverActivity.getDayDayRecharge().getRechargeRewardsList();
                if (claimIndex >= rechargeRewardsList.size()) {
                    return RCE_Activity_MissionCanNotClaim;
                }

                //是否领取最后一天奖励 （最后一天奖励领取两个宝箱）
                int lastRewardIndex = rechargeRewardsList.size() - 1;
                boolean claimLastDayRecharge = claimIndex >= lastRewardIndex - 1;
                Builder dayDayRechargeBuilder = targetsystemEntity.getDb_Builder().getSpecialInfoBuilder().getDayDayRechargeBuilder();

                //领取最后一天奖励
                if (claimLastDayRecharge) {
                    List<Reward> rewards = new ArrayList<>();
                    canClaimIndexList = canClaimIndexList.stream().filter(e -> e != lastRewardIndex && e != lastRewardIndex - 1).collect(Collectors.toList());
                    dayDayRechargeBuilder.clearCanClaimIndex().addAllCanClaimIndex(canClaimIndexList).addClaimedIndex(lastRewardIndex).addClaimedIndex(lastRewardIndex - 1);
                    rewards.addAll(rechargeRewardsList.get(lastRewardIndex).getRewardList());
                    rewards.addAll(rechargeRewardsList.get(lastRewardIndex - 1).getRewardList());
                    //发放奖励
                    RewardManager.getInstance().doRewardByList(playerIdx, rewards,
                            ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_DayDayRecharge_Recharge), true);

                } else {
                    //按索引领取奖励
                    canClaimIndexList = canClaimIndexList.stream().filter(e -> claimIndex != e).collect(Collectors.toList());
                    dayDayRechargeBuilder.addClaimedIndex(claimIndex).clearCanClaimIndex().addAllCanClaimIndex(canClaimIndexList);
                    //发放奖励
                    RewardManager.getInstance().doRewardByList(playerIdx, rechargeRewardsList.get(claimIndex).getRewardList(),
                            ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_DayDayRecharge_Recharge), true);
                }
                LogUtil.info("player:{} ClaimDayDayRechargeReward recharge reward success,claimIndex:{}", playerIdx, claimIndex);
            }

            return RCE_Success;
        });
        if (codeEnum == RCE_Success) {
            targetsystemEntity.sendDayDayRechargeUpdate();
            player.increasePlayerRewardRecord(serverActivity.getActivityId(), claimIndexInPlayer);
        }
        result.setRetCode(GameUtil.buildRetCode(codeEnum));
        gsChn.send(MsgIdEnum.SC_ClaimDayDayRechargeReward_VALUE, result);
    }

    private int getRecordIndexInPlayer(int claimType, int claimIndex) {
        return claimType * 100 + claimIndex;

    }


    private int getClaimIndex(CS_ClaimDayDayRechargeReward req, targetsystemEntity target) {

        if (req.getClaimType() == CLAIM_FREE) {
            return target.getDb_Builder().getSpecialInfo().getDayDayRecharge().getCurFreeIndex();
        }
        return req.getClaimIndex();

    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }
}

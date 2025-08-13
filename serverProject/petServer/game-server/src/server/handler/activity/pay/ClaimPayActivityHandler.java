package server.handler.activity.pay;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.activity.ActivityUtil;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardManager;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import org.apache.commons.lang.time.DateUtils;
import platform.logs.ReasonManager;
import protocol.Activity;
import protocol.Common;
import protocol.Common.EnumFunction;
import protocol.MessageId;
import protocol.RetCodeId;
import protocol.TargetSystemDB;
import util.GameUtil;
import util.LogUtil;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import static protocol.RetCodeId.RetCodeEnum.RCE_Activity_DissatisfyAddition;
import static protocol.RetCodeId.RetCodeEnum.RCE_Activity_MissionCanNotClaim;
import static protocol.RetCodeId.RetCodeEnum.RCE_Activity_RewardAlreadyClaim;
import static protocol.RetCodeId.RetCodeEnum.RCE_Success;
import static protocol.RetCodeId.RetCodeEnum.RCE_UnknownError;


/**
 * @Description
 * @Author hanx
 * @Date2020/4/26 0026 20:02
 **/
@MsgId(msgId = MessageId.MsgIdEnum.CS_ClaimPayActivityBonus_VALUE)
public class ClaimPayActivityHandler extends AbstractBaseHandler<Activity.CS_ClaimPayActivityBonus> {
    @Override
    protected Activity.CS_ClaimPayActivityBonus parse(byte[] bytes) throws Exception {
        return Activity.CS_ClaimPayActivityBonus.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, Activity.CS_ClaimPayActivityBonus req, int i) {

        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        LogUtil.info("playerIdx:{} ClaimPayActivityBonus req:{}", playerIdx, req);
        Activity.SC_ClaimPayActivityBonus.Builder result = Activity.SC_ClaimPayActivityBonus.newBuilder();
        int payRewardType = req.getPayRewardType();
        if (payRewardType != 1 && payRewardType != 0) {
            result.setRet(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MessageId.MsgIdEnum.SC_ClaimPayActivityBonus_VALUE, result);
            return;
        }

        long activityId = getActivityId(payRewardType);
        targetsystemEntity targetsystemEntity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        playerEntity player = playerCache.getByIdx(playerIdx);


        if (targetsystemEntity == null || player == null) {
            result.setRet(GameUtil.buildRetCode(RCE_UnknownError));
            gsChn.send(MessageId.MsgIdEnum.SC_ClaimPayActivityBonus_VALUE, result);
            return;
        }
        Map<Integer, TargetSystemDB.PayActivityRecord> payActivityRecordMap = targetsystemEntity.getDb_Builder().getPayActivityRecordMap();
        TargetSystemDB.PayActivityRecord payActivityRecord = payActivityRecordMap.get(payRewardType);
        //充值奖励记录为空/未激活充值奖励
        if (payActivityRecord == null || payActivityRecord.getState() == Activity.PayActivityStateEnum.PAS_NotActive_VALUE) {
            result.setRet(GameUtil.buildRetCode(RCE_Activity_DissatisfyAddition));
            gsChn.send(MessageId.MsgIdEnum.SC_ClaimPayActivityBonus_VALUE, result);
        }

        int rewardIndex = getClaimRewardIndex(payRewardType, targetsystemEntity);
        if (rewardIndex == -1) {
            result.setRet(GameUtil.buildRetCode(RCE_Activity_RewardAlreadyClaim));
            gsChn.send(MessageId.MsgIdEnum.SC_ClaimPayActivityBonus_VALUE, result);
        }

        RetCodeId.RetCodeEnum codeEnum = SyncExecuteFunction.executeFunction(targetsystemEntity, entity -> {
            TargetSystemDB.PayActivityRecord payActivityRecordInDB = targetsystemEntity.getDb_Builder().getPayActivityRecordMap().get(payRewardType);
            Activity.PayActivityBonus curPayBonus = payActivityRecordInDB.getBonus(rewardIndex);

            RetCodeId.RetCodeEnum retCodeEnum = canClaimReward(playerIdx, activityId, player, rewardIndex, payActivityRecordInDB, curPayBonus);
            if (retCodeEnum != RCE_Success) {
                return retCodeEnum;
            }

            updateCache(payRewardType, targetsystemEntity, rewardIndex, payActivityRecordInDB, curPayBonus);

            //发放奖励
            doReward(playerIdx, curPayBonus);

            LogUtil.info("playerIdx:{} finish ClaimPayActivityBonus ", playerIdx);
            //重新推送活动消息
            targetsystemCache.getInstance().sendRechargeActivityShow(playerIdx);
            result.setRet(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
            gsChn.send(MessageId.MsgIdEnum.SC_ClaimPayActivityBonus_VALUE, result);
            return RCE_Success;
        });
        if (RCE_Success == codeEnum) {
            player.increasePlayerRewardRecord(activityId, rewardIndex);
        }
    }

    private RetCodeId.RetCodeEnum canClaimReward(String playerIdx, long activityId, playerEntity player, int rewardIndex, TargetSystemDB.PayActivityRecord payActivityRecordInDB, Activity.PayActivityBonus curPayBonus) {
        LogUtil.info("playerIdx:{} ClaimPayActivityBonus curPayBonus:{},rewardIndex:{}", playerIdx, curPayBonus, rewardIndex);

        //已领取
        if (payActivityRecordInDB.getState() != Activity.PayActivityStateEnum.PAS_SignOn_VALUE) {
            return RCE_Activity_RewardAlreadyClaim;
        }

        if (curPayBonus == null || player.alreadyClaimed(activityId, rewardIndex)) {
            return RCE_Activity_RewardAlreadyClaim;
        }
        //未到奖励领取时间
        if (!(curPayBonus.getClaimTimestamp() > 0 && curPayBonus.getClaimTimestamp() < System.currentTimeMillis())) {
            return RCE_Activity_MissionCanNotClaim;
        }
        return RCE_Success;
    }

    private void doReward(String playerIdx, Activity.PayActivityBonus curPayBonus) {
        List<Common.Reward> rewards = curPayBonus.getBonusList();
        RewardManager.getInstance().doRewardByList(playerIdx, rewards,
                ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_CumuRecharge), true);
    }

    private void updateCache(int payRewardType, targetsystemEntity targetsystemEntity, int rewardIndex, TargetSystemDB.PayActivityRecord payActivityRecordInDB, Activity.PayActivityBonus curPayBonus) {
        TargetSystemDB.PayActivityRecord.Builder recordBuilder = payActivityRecordInDB.toBuilder();
        //替换已领取的奖励entity
        recordBuilder.removeBonus(rewardIndex).addBonus(rewardIndex, curPayBonus.toBuilder().setBonusState(Activity.BonusStateEnum.BSE_AlreadySignOn_VALUE).build());

        if (allClaim(rewardIndex, payActivityRecordInDB)) {
            //所有奖励领取完成
            LogUtil.info("player:{} complete  payRewardType:{} all payRewards ", targetsystemEntity.getLinkplayeridx(), payRewardType);
            recordBuilder.setState(Activity.PayActivityStateEnum.BSE_Finish_VALUE);
        } else {
            openNextReward(targetsystemEntity.getLinkplayeridx(), rewardIndex + 1, recordBuilder);

        }
        targetsystemEntity.getDb_Builder().putPayActivityRecord(payRewardType, recordBuilder.build());
    }

    private boolean allClaim(int rewardIndex, TargetSystemDB.PayActivityRecord payActivityRecordInDB) {
        return rewardIndex >= payActivityRecordInDB.getBonusCount() - 1;
    }

    private int getClaimRewardIndex(int payRewardType, targetsystemEntity targetsystemEntity) {
        Map<Integer, TargetSystemDB.PayActivityRecord> payActivityRecordMap = targetsystemEntity.getDb_Builder().getPayActivityRecordMap();
        TargetSystemDB.PayActivityRecord payActivityRecord = payActivityRecordMap.get(payRewardType);
        ListIterator<Activity.PayActivityBonus> payActivityBonus = payActivityRecord.getBonusList().listIterator();
        int rewardIndex = 0;
        while (payActivityBonus.hasNext()) {
            if (Activity.BonusStateEnum.BSE_WaitSignOn_VALUE == payActivityBonus.next().getBonusState()) {
                return rewardIndex;
            }
            rewardIndex++;

        }
        return -1;
    }

    private long getActivityId(int payRewardType) {
        if (payRewardType == 1) {
            return ActivityUtil.LocalActivityId.FirstRecharge;
        }
        return ActivityUtil.LocalActivityId.CumuRecharge;
    }

    private void openNextReward(String playerIdx, int rewardIndex, TargetSystemDB.PayActivityRecord.Builder recordBuilder) {

        Activity.PayActivityBonus bonus = recordBuilder.getBonus(rewardIndex);

        Activity.PayActivityBonus.Builder builder = bonus.toBuilder()
                .setBonusState(Activity.BonusStateEnum.BSE_WaitSignOn_VALUE)
                .setClaimTimestamp(System.currentTimeMillis() + DateUtils.MILLIS_PER_DAY);
        LogUtil.info("player:{} ClaimPayActivity openNext PayActivityBonus :{} ", playerIdx, builder);
        recordBuilder.removeBonus(rewardIndex).addBonus(rewardIndex, builder);
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }
}

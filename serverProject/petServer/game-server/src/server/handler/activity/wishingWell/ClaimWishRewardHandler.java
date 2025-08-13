package server.handler.activity.wishingWell;

import cfg.WishWellConfig;
import cfg.WishWellConfigObject;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import model.activity.ActivityUtil;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import org.apache.commons.collections4.CollectionUtils;
import platform.logs.ReasonManager;
import protocol.Activity;
import protocol.Activity.SC_ClaimWishReward;
import protocol.Activity.WishStateEnum;
import protocol.Activity.WishingWellItem;
import protocol.Activity.WishingWellItem.Builder;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystemDB.DB_WishingWell;
import util.GameUtil;
import util.LogUtil;
import util.TimeUtil;

import static common.RewardRecordConst.OnceRewardRecord.WishWellDay1;
import static protocol.RetCodeId.RetCodeEnum.RCE_Activity_DissatisfyAddition;
import static protocol.RetCodeId.RetCodeEnum.RCE_ErrorParam;
import static protocol.RetCodeId.RetCodeEnum.RCE_Player_CurrencysNotEnought;
import static protocol.RetCodeId.RetCodeEnum.RCE_Success;
import static protocol.RetCodeId.RetCodeEnum.RSE_ConfigNotExist;

/**
 * @Description 领取许愿池奖励
 * @Author hanx
 * @Date2020/6/2 0002 9:45
 **/
@MsgId(msgId = MsgIdEnum.CS_ClaimWishReward_VALUE)
public class ClaimWishRewardHandler extends AbstractBaseHandler<Activity.CS_ClaimWishReward> {

    private static final int Normal = 0; //正常领取
    private static final int MAKE_UP = 1; //补领领取
    private static final int REPLENISH_SIGN = 2; //补签领取

    @Override
    protected Activity.CS_ClaimWishReward parse(byte[] bytes) throws Exception {
        return Activity.CS_ClaimWishReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, Activity.CS_ClaimWishReward req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        int claimType = req.getClaimType();
        int wishIndex = req.getWishIndex();
        LogUtil.info("playerIdx:{} claimWishReward,claimType:{} wishIndex:{}", playerIdx, claimType, wishIndex);
        WishWellConfigObject config = WishWellConfig.getById(wishIndex);
        Activity.SC_ClaimWishReward.Builder result = Activity.SC_ClaimWishReward.newBuilder();
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);

        playerEntity player = playerCache.getByIdx(playerIdx);

        if (target == null || player == null) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            gsChn.send(MessageId.MsgIdEnum.SC_ClaimWishReward_VALUE, result);
            return;
        }

        if (!checkParams(req, config)) {
            result.setRetCode(GameUtil.buildRetCode(RCE_ErrorParam));
            gsChn.send(MessageId.MsgIdEnum.SC_ClaimWishReward_VALUE, result);
            return;
        }

        if (player.alreadyClaimed(ActivityUtil.LocalActivityId.WishingWell, wishIndex)) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Activity_RewardAlreadyClaim));
            gsChn.send(MsgIdEnum.SC_ClaimWishReward_VALUE, result);
            return;
        }


        RetCodeEnum claimResult = SyncExecuteFunction.executeFunction(target, entity -> {

            List<Reward> rewardList = RewardUtil.getRewardsByRewardId(config.getRewardoptions());
            WishingWellItem wishInDb = target.getDb_Builder().getSpecialInfo().getWishingWell().getWishMapMap().get(wishIndex);

            LogUtil.info("playerIdx:{} wishInDb :{}", playerIdx, wishInDb);

            RetCodeEnum codeEnum = beforeCheck(player, req, target, config, rewardList, wishInDb);
            if (codeEnum != RCE_Success) {
                return codeEnum;
            }

            int rewardIndex = claimType == REPLENISH_SIGN ? req.getRewardIndex() : wishInDb.getRewardIndex();

            LogUtil.info("playerIdx:{} do wishReward rewardIndex :{}", playerIdx, rewardIndex);

            Reward reward = rewardList.get(rewardIndex);

            Builder wishUpdate = wishInDb.toBuilder().setState(WishStateEnum.WSE_Claimed);
            target.getDb_Builder().getSpecialInfoBuilder().getWishingWellBuilder().putWishMap(wishIndex, wishUpdate.build());
            target.sendWishUpdate(wishUpdate);
            RewardManager.getInstance().doReward(playerIdx, reward, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_WishingWell), false);
            return RetCodeEnum.RCE_Success;
        });

        result.setRetCode(GameUtil.buildRetCode(claimResult));
        gsChn.send(MsgIdEnum.SC_ClaimWishReward_VALUE, result);

        if (claimResult != RCE_Success) {
            return;
        }

        WishingWellItem wishingWellItem = target.getDb_Builder().getSpecialInfo().getWishingWell().getWishMapMap().get(wishIndex);
        LogUtil.info("playerIdx:{} claimed wish reward finish,cur wish status: wishIdx:{},wishState:{},rewardIdx:{},claimedTime:{},wishTime:{}",
                playerIdx, wishingWellItem.getWishIndex(), wishingWellItem.getState(), wishingWellItem.getRewardIndex(), wishingWellItem.getClaimTime(), wishingWellItem.getWishTime());

        SyncExecuteFunction.executeConsumer(player, entity -> {
            player.increasePlayerRewardRecord(ActivityUtil.LocalActivityId.WishingWell, wishIndex);
        });


    }

    private RetCodeEnum beforeCheck(playerEntity player, Activity.CS_ClaimWishReward req, targetsystemEntity target, WishWellConfigObject config, List<Reward> rewardList, WishingWellItem wishInDb) {
        int claimType = req.getClaimType();
        DB_WishingWell wishingWell = target.getDb_Builder().getSpecialInfo().getWishingWell();
        long endDisTime = wishingWell.getEndTime();
        //玩家需要到达指定等级||所有的领取方式在活动展示结束后不可领取
        if (!player.functionUnLock(EnumFunction.WishingWell) || endDisTime < GlobalTick.getInstance().getCurrentTime()) {
            return RetCodeEnum.RCE_Activity_NotOpen;
        }
        if (claimType == REPLENISH_SIGN && rewardList.size() < req.getRewardIndex()) {
            return RCE_ErrorParam;
        }
        if (wishInDb == null || wishInDb.getState() == WishStateEnum.WSE_Claimed) {
            return RCE_ErrorParam;
        }
        LogUtil.info("playerIdx:{} cur wishInDb, wishIdx:{},wishState:{},rewardIdx:{},claimedTime:{},wishTime:{}", player.getIdx(),
                wishInDb.getWishIndex(), wishInDb.getState(), wishInDb.getRewardIndex(), wishInDb.getClaimTime(), wishInDb.getWishTime());

        Consume consume = null;

        switch (claimType) {
            // 补领领取
            case MAKE_UP:
                LogUtil.info("playerIdx:{} make up wishWell", player.getIdx());
                if (WishStateEnum.WSE_UnClaim != wishInDb.getState() || !outOfNormalClaimTime(wishInDb)) {
                    return RCE_ErrorParam;
                }
                consume = ConsumeUtil.parseConsume(config.getMakeupprice());
                break;
            case REPLENISH_SIGN:
                //补签领取
                LogUtil.info("playerIdx:{} REPLENISH SIGN wishWell", player.getIdx());
                if (WishStateEnum.WSE_UnChoose != wishInDb.getState() || !outOfWishTime(wishInDb)) {
                    return RCE_ErrorParam;
                }
                consume = ConsumeUtil.parseConsume(config.getReplenishsignprice());
                break;
            case Normal:
                //正常领取
                LogUtil.info("playerIdx:{} claim wishWell by usual", player.getIdx());
                //普通领取活动过期不可领取
              /*  if (outOfNormalClaimTime(wishInDb)) {
                    return RCE_Activity_MissionOutOfTime;
                }*/
                //正常领取
                if (earlyThanNormalClaimTime(wishInDb) || WishStateEnum.WSE_UnClaim != wishInDb.getState()) {
                    return RCE_Activity_DissatisfyAddition;
                }
                break;
            default:
                return RCE_ErrorParam;

        }

        if (CollectionUtils.isEmpty(rewardList)) {
            return RSE_ConfigNotExist;
        }
        if (consume != null) {
            // 购买消耗
            if (!ConsumeManager.getInstance().consumeMaterial(player.getIdx(), consume, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_WishingWell))) {
                return RCE_Player_CurrencysNotEnought;
            }
        }

        return RCE_Success;
    }

    private boolean earlyThanNormalClaimTime(WishingWellItem wishInDb) {
        return wishInDb.getClaimTime() > GlobalTick.getInstance().getCurrentTime();
    }

    private boolean outOfWishTime(WishingWellItem wishInDb) {
        return wishInDb.getWishTime() < GlobalTick.getInstance().getCurrentTime();
    }

    private boolean outOfNormalClaimTime(WishingWellItem wishInDb) {
        return wishInDb.getClaimTime() + TimeUtil.MS_IN_A_DAY < GlobalTick.getInstance().getCurrentTime();
    }

    private boolean checkParams(Activity.CS_ClaimWishReward req, WishWellConfigObject config) {
        if (config == null) {
            return false;
        }
        int claimType = req.getClaimType();
        if (claimType != MAKE_UP && claimType != REPLENISH_SIGN && claimType != Normal) {
            return false;
        }
        return true;
    }


    private int queryRewardRecord(int wishIndex) {
        return WishWellDay1 + wishIndex - 1;
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.WishingWell;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimWishReward_VALUE, SC_ClaimWishReward.newBuilder().setRetCode(retCode));
    }
}

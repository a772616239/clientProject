/*
package server.handler.activity.wishingWell;

import cfg.GameConfig;
import cfg.WishWellConfig;
import cfg.WishWellConfigObject;
import common.AbstractBaseHandler;
import common.GameConst;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.player.util.PlayerUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import protocol.Activity;
import protocol.Activity.CS_WishWellReplenishSign;
import protocol.Activity.WishStateEnum;
import protocol.Activity.WishingWellItem;
import protocol.Activity.WishingWellItem.Builder;
import protocol.Common.Consume;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystemDB.DB_WishingWell;
import util.GameUtil;

*/
/**
 * @Description
 * @Author hanx
 * @Date2020/7/16 0016 15:09
 **//*

@MsgId(msgId = MsgIdEnum.CS_WishWellReplenishSign_VALUE)
public class WishWellReplenishSignHandler extends AbstractBaseHandler<CS_WishWellReplenishSign> {
    @Override
    protected Activity.CS_WishWellReplenishSign parse(byte[] bytes) throws Exception {
        return Activity.CS_WishWellReplenishSign.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, Activity.CS_WishWellReplenishSign req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        int rewardIndex = req.getRewardIndex();
        int wishIndex = req.getWishIndex();
        Activity.SC_WishWellReplenishSign.Builder result = Activity.SC_WishWellReplenishSign.newBuilder();
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        WishWellConfigObject config = WishWellConfig.getById(wishIndex);
        if (target == null) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            gsChn.send(MessageId.MsgIdEnum.SC_WishWellReplenishSign_VALUE, result);
            return;
        }
        int playerLv = PlayerUtil.queryPlayerLv(playerIdx);
        if (playerLv <= GameConfig.getById(GameConst.CONFIG_ID).getWishwellneedlv()) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Activity_NotOpen));
            gsChn.send(MessageId.MsgIdEnum.SC_WishWellReplenishSign_VALUE, result);
            return;
        }
        DB_WishingWell wishingWell = target.getDb_Builder().getSpecialInfo().getWishingWell();
        WishingWellItem wishingWellItem = wishingWell.getWishMapMap().get(rewardIndex);
        if (wishingWellItem == null) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MessageId.MsgIdEnum.SC_WishWellReplenishSign_VALUE, result);
            return;
        }
        if (WishStateEnum.WSE_UnChoose != wishingWellItem.getState()) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Activity_DissatisfyAddition));
            gsChn.send(MsgIdEnum.SC_ClaimWishReward_VALUE, result);
            return;
        }
        if (wishingWell.getEndTime() < GlobalTick.getInstance().getCurrentTime()) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Activity_MissionOutOfTime));
            gsChn.send(MsgIdEnum.SC_ClaimWishReward_VALUE, result);
            return;
        }
        Consume consume = ConsumeUtil.parseConsume(config.getReplenishsignprice());
        Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_WishingWell);
        //购买消耗
        if (!ConsumeManager.getInstance().consumeMaterial(playerIdx, consume, reason)) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_MatieralNotEnough));
            gsChn.send(MsgIdEnum.SC_ClaimWishReward_VALUE, result);
            return;
        }

        SyncExecuteFunction.executeConsumer(target, entity -> {
            Builder updateWish = wishingWellItem.toBuilder().setState(WishStateEnum.WSE_UnClaim).setRewardIndex(rewardIndex);
            target.getDb_Builder().getSpecialInfoBuilder().getWishingWellBuilder().putWishMap(wishIndex, updateWish.build());
            entity.sendWishUpdate(updateWish);
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MessageId.MsgIdEnum.SC_WishWellReplenishSign_VALUE, result);
        });

    }
}
*/

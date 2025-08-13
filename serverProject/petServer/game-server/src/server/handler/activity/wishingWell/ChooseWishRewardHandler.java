package server.handler.activity.wishingWell;

import cfg.GameConfig;
import cfg.WishWellConfig;
import cfg.WishWellConfigObject;
import common.AbstractBaseHandler;
import common.GameConst;
import common.SyncExecuteFunction;
import common.load.ServerConfig;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.util.PlayerUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import protocol.Activity;
import protocol.Activity.SC_ChooseWishReward;
import protocol.Activity.WishStateEnum;
import protocol.Activity.WishingWellItem;
import protocol.Activity.WishingWellItem.Builder;
import protocol.Common.EnumFunction;
import protocol.MessageId;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

/**
 * @Description 选取许愿池奖励
 * @Author hanx
 * @Date2020/6/2 0002 9:45
 **/
@MsgId(msgId = MessageId.MsgIdEnum.CS_ChooseWishReward_VALUE)
public class ChooseWishRewardHandler extends AbstractBaseHandler<Activity.CS_ChooseWishReward> {
    @Override
    protected Activity.CS_ChooseWishReward parse(byte[] bytes) throws Exception {
        return Activity.CS_ChooseWishReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, Activity.CS_ChooseWishReward req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        int rewardIndex = req.getRewardIndex();
        int wishIndex = req.getWishIndex();
        LogUtil.info("playerIdx:{} chooseWishReward,rewardIndex:{} wishIndex:{}", playerIdx, rewardIndex, wishIndex);
        Activity.SC_ChooseWishReward.Builder result = Activity.SC_ChooseWishReward.newBuilder().setWishIndex(wishIndex);
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        WishWellConfigObject config = WishWellConfig.getById(wishIndex);
        if (target == null || config == null) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            gsChn.send(MessageId.MsgIdEnum.SC_ChooseWishReward_VALUE, result);
            return;
        }
        if (PlayerUtil.queryFunctionLock(playerIdx,EnumFunction.WishingWell)) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Activity_NotOpen));
            gsChn.send(MessageId.MsgIdEnum.SC_ChooseWishReward_VALUE, result);
            return;
        }
        SyncExecuteFunction.executeConsumer(target, entity -> {
            WishingWellItem wish = target.getDb_Builder().getSpecialInfo().getWishingWell().getWishMapMap().get(wishIndex);
            if (wish == null) {
                result.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
                gsChn.send(MessageId.MsgIdEnum.SC_ChooseWishReward_VALUE, result);
                return;
            }
            LogUtil.info("cur wishInDb, wishIdx:{},wishState:{},rewardIdx:{},claimedTime:{},wishTime:{}",
                    wish.getWishIndex(), wish.getState(), wish.getRewardIndex(), wish.getClaimTime(), wish.getWishTime());

            if (WishStateEnum.WSE_UnChoose != wish.getState()) {
                result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Activity_DissatisfyAddition));
                gsChn.send(MessageId.MsgIdEnum.SC_ChooseWishReward_VALUE, result);
                return;
            }
            if (wish.getWishTime() > GlobalTick.getInstance().getCurrentTime() || wish.getClaimTime() < GlobalTick.getInstance().getCurrentTime()) {
                result.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen));
                gsChn.send(MessageId.MsgIdEnum.SC_ChooseWishReward_VALUE, result);
                return;
            }
            Builder wishUpdate = wish.toBuilder().setState(WishStateEnum.WSE_UnClaim).setRewardIndex(rewardIndex);
            entity.sendWishUpdate(wishUpdate);
            target.getDb_Builder().getSpecialInfoBuilder().getWishingWellBuilder().putWishMap(wishIndex, wishUpdate.build());
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MessageId.MsgIdEnum.SC_ChooseWishReward_VALUE, result);
        });
        WishingWellItem wishingWellItem = target.getDb_Builder().getSpecialInfo().getWishingWell().getWishMapMap().get(wishIndex);
        LogUtil.info("claimed wish reward finish,cur wish status: wishIdx:{},wishState:{},rewardIdx:{},claimedTime:{},wishTime:{}",
                wishingWellItem.getWishIndex(), wishingWellItem.getState(), wishingWellItem.getRewardIndex(), wishingWellItem.getClaimTime(), wishingWellItem.getWishTime());
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.WishingWell;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ChooseWishReward_VALUE, SC_ChooseWishReward.newBuilder().setRetCode(retCode));
    }
}

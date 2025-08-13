package server.handler.gift;

import protocol.Common.EnumFunction;
import cfg.TimeLimitGiftConfig;
import cfg.TimeLimitGiftConfigObject;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import static protocol.MessageId.MsgIdEnum.CS_BuyTimeLimitGift_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_BuyTimeLimitGift_VALUE;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.CS_BuyTimeLimitGift;
import protocol.TargetSystem.SC_BuyTimeLimitGift;
import protocol.TargetSystem.SC_BuyTimeLimitGift.Builder;
import protocol.TargetSystemDB.DB_TimeLimitGiftInfo;
import protocol.TargetSystemDB.DB_TimeLimitGiftItem;
import util.GameUtil;

import java.util.List;

/**
 * @Description
 * @Author hanx
 * @Date2020/7/8 0008 10:27
 **/
@MsgId(msgId = CS_BuyTimeLimitGift_VALUE)
public class BuyTimeLimitGiftHandler extends AbstractBaseHandler<CS_BuyTimeLimitGift> {
    @Override
    protected CS_BuyTimeLimitGift parse(byte[] bytes) throws Exception {
        return CS_BuyTimeLimitGift.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_BuyTimeLimitGift req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerId);
        Builder result = SC_BuyTimeLimitGift.newBuilder();
        TimeLimitGiftConfigObject giftConfig = TimeLimitGiftConfig.getById(req.getGiftId());
        if (target == null || giftConfig == null) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(SC_BuyTimeLimitGift_VALUE, result);
            return;
        }
        DB_TimeLimitGiftItem giftItem = target.getDb_Builder().getTimeLimitGiftInfo().getGiftsMap().get(req.getGiftId());
        if (giftItem == null || giftItem.getBuy()) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_TimeLimitGift_NotActive));
            gsChn.send(SC_BuyTimeLimitGift_VALUE, result);
            return;
        }
        Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_TimeLimitGift);
        if (!ConsumeManager.getInstance().consumeMaterial(playerId, ConsumeUtil.parseConsume(giftConfig.getPrice()), reason)) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Player_CurrencysNotEnought));
            gsChn.send(MsgIdEnum.SC_BuyTimeLimitGift_VALUE, result);
            return;
        }
        SyncExecuteFunction.executeConsumer(target, e -> {
            DB_TimeLimitGiftInfo.Builder giftInfoBuilder = target.getDb_Builder().getTimeLimitGiftInfoBuilder();
            giftInfoBuilder.putGifts(req.getGiftId(), giftItem.toBuilder().setBuy(true).build());
        });
        List<Reward> rewardsByRewardId = RewardUtil.getRewardsByRewardId(giftConfig.getReward());
        RewardManager.getInstance().doRewardByList(playerId, rewardsByRewardId, reason, true);

        result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(SC_BuyTimeLimitGift_VALUE, result);
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
    }


 }

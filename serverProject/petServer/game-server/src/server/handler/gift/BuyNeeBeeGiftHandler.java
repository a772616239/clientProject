package server.handler.gift;

import protocol.Common.EnumFunction;
import cfg.NewBeeGiftCfg;
import cfg.NewBeeGiftCfgObject;
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
import protocol.Activity.CS_BuyNewBeeGift;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import static protocol.MessageId.MsgIdEnum.CS_BuyNewBeeGift_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_BuyNewBeeGift_VALUE;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.SC_GetAllTimeLimitGift;
import protocol.TargetSystem.SC_GetAllTimeLimitGift.Builder;
import protocol.TargetSystemDB.DB_NeeBeeGift;
import util.GameUtil;

import java.util.List;

/**
 * @Description
 * @Author hanx
 * @Date2020/7/8 0008 10:27
 **/
@MsgId(msgId = CS_BuyNewBeeGift_VALUE)
public class BuyNeeBeeGiftHandler extends AbstractBaseHandler<CS_BuyNewBeeGift> {
    @Override
    protected CS_BuyNewBeeGift parse(byte[] bytes) throws Exception {
        return CS_BuyNewBeeGift.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_BuyNewBeeGift req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerId);
        Builder result = SC_GetAllTimeLimitGift.newBuilder();
        NewBeeGiftCfgObject giftConfig = NewBeeGiftCfg.getById(req.getGiftId());
        if (target == null || giftConfig == null) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(SC_BuyNewBeeGift_VALUE, result);
            return;
        }
        Integer buyTimes = target.getDb_Builder().getSpecialInfoBuilder().getNewBeeGift().getGiftsMap().get(req.getGiftId());
        if (buyTimes != null && buyTimes >= giftConfig.getLimit()) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Store_GoodsBuyUpperLimit));
            gsChn.send(SC_BuyNewBeeGift_VALUE, result);
            return;
        }
        Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_NeeBeeGift);
        if (!ConsumeManager.getInstance().consumeMaterial(playerId, ConsumeUtil.parseConsume(giftConfig.getConsume()), reason)) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Player_CurrencysNotEnought));
            gsChn.send(MsgIdEnum.SC_BuyNewBeeGift_VALUE, result);
            return;
        }
        buyTimes = buyTimes == null ? 1 : buyTimes + 1;
        Integer finalBuyTimes = buyTimes;
        SyncExecuteFunction.executeConsumer(target, e -> {
            DB_NeeBeeGift.Builder giftInfoBuilder = target.getDb_Builder().getSpecialInfoBuilder().getNewBeeGiftBuilder();
            giftInfoBuilder.putGifts(req.getGiftId(), finalBuyTimes);
            target.sendNeeBeeGiftUpdate(req.getGiftId());
        });
        List<Reward> rewardsByRewardId = RewardUtil.parseRewardIntArrayToRewardList(giftConfig.getReward());
        RewardManager.getInstance().doRewardByList(playerId, rewardsByRewardId, reason, true);
        result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(SC_BuyNewBeeGift_VALUE, result);
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
    }


 }

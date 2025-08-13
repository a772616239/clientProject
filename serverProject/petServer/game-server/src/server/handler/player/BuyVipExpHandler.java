package server.handler.player;

import cfg.GameConfig;
import common.AbstractBaseHandler;
import common.GameConst;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerInfo.CS_BuyVipExpCard;
import protocol.PlayerInfo.SC_BuyVipExpCard;
import protocol.PlayerInfo.SC_BuyVipExpCard.Builder;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_BuyVipExpCard_VALUE)
public class BuyVipExpHandler extends AbstractBaseHandler<CS_BuyVipExpCard> {

    @Override
    protected CS_BuyVipExpCard parse(byte[] bytes) throws Exception {
        return CS_BuyVipExpCard.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_BuyVipExpCard req, int codeNum) {
        Builder result = SC_BuyVipExpCard.newBuilder();
        String playerId = String.valueOf(gsChn.getPlayerId1());
        LogUtil.info("receive player:{} buy vipExp", playerId);
        playerEntity player = playerCache.getByIdx(playerId);
        if (player == null) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_BuyVipExpCard_VALUE, result);
            return;
        }
        int vipExpBuyTime = player.getDb_data().getVipExpBuyTime();
        int vipBuyLimit = GameConfig.getById(GameConst.CONFIG_ID).getVipexpdailybuylimit();
        result.setAlreadyBuyTime(vipExpBuyTime);

        if (vipExpBuyTime >= vipBuyLimit) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_VipExpMaxBuyTimeLimit));
            gsChn.send(MsgIdEnum.SC_BuyVipExpCard_VALUE, result);
            return;

        }

        Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_VipExpCard);
        Consume consume = ConsumeUtil.parseConsume(GameConfig.getById(GameConst.CONFIG_ID).getBuyvipexpcost());
        if (!ConsumeManager.getInstance().consumeMaterial(playerId, consume, reason)) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Player_CurrencysNotEnought));
            gsChn.send(MsgIdEnum.SC_BuyVipExpCard_VALUE, result);
            return;
        }

        RewardManager.getInstance().doReward(playerId, RewardUtil.parseReward(GameConfig.getById
                (GameConst.CONFIG_ID).getBuyvipexpgetitem()), reason, true);


        SyncExecuteFunction.executeConsumer(player, cacheTemp -> {
            int expBuyTime = player.getDb_data().getVipExpBuyTime();
            player.getDb_data().setVipExpBuyTime(expBuyTime + 1);
            result.setAlreadyBuyTime(expBuyTime + 1);
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_BuyVipExpCard_VALUE, result);
        });
        LogUtil.info(" player:{} buy vipExp success,now vipLv:{},now exp:{}", playerId,player.getVip(), player.getVipexperience());
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Vip;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_BuyVipExpCard_VALUE, SC_BuyVipExpCard.newBuilder().setRetCode(retCode));
    }
}

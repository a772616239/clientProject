package model.warpServer.crossServer.handler.thewar;

import common.GlobalData;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.consume.ConsumeManager;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.thewar.TheWarConst;
import model.warpServer.crossServer.CrossServerManager;
import platform.logs.ReasonManager;
import protocol.Common.Consume;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_BuyStaminaCheckRet;
import protocol.ServerTransfer.GS_CS_BuyStamina;
import protocol.TheWar.SC_BuyStamia;
import protocol.TheWarDefine.TheWarRetCode;

@MsgId(msgId = MsgIdEnum.CS_GS_BuyStaminaCheckRet_VALUE)
public class BuyStaminaChechRetHandler extends AbstractHandler<CS_GS_BuyStaminaCheckRet> {
    @Override
    protected CS_GS_BuyStaminaCheckRet parse(byte[] bytes) throws Exception {
        return CS_GS_BuyStaminaCheckRet.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_BuyStaminaCheckRet ret, int i) {
        playerEntity player = playerCache.getByIdx(ret.getPlayerIdx());
        if (player == null) {
            return;
        }
        SC_BuyStamia.Builder builder = SC_BuyStamia.newBuilder();
        if (ret.getRetCode() != TheWarRetCode.TWRC_Success) {
            builder.setRetCode(ret.getRetCode());
            GlobalData.getInstance().sendMsg(player.getIdx(), MsgIdEnum.SC_BuyStamia_VALUE, builder);
            return;
        }
        int buyStaminaCount = player.getDb_data().getTheWarData().getDailyBuyStaminaCount();
        Consume consume = TheWarConst.getBuyStaminaConsume(buyStaminaCount);
        if (consume == null) {
            builder.setRetCode(TheWarRetCode.TWRC_ConfigNotFound); // 未找到消耗配置
            GlobalData.getInstance().sendMsg(player.getIdx(), MsgIdEnum.SC_BuyStamia_VALUE, builder);
            return;
        }
        if (!ConsumeManager.getInstance().consumeMaterial(player.getIdx(), consume, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_TheWar))) {
            builder.setRetCode(TheWarRetCode.TWRC_CurrencyNotEnough_BuyBack); // 道具或货币不足
            GlobalData.getInstance().sendMsg(player.getIdx(), MsgIdEnum.SC_BuyStamia_VALUE, builder);
            return;
        }
        SyncExecuteFunction.executeConsumer(player, entity -> entity.getDb_data().getTheWarDataBuilder().setDailyBuyStaminaCount(buyStaminaCount + 1));
        player.sendTheWarBuyStaminaTimes();

        String roomIdx = player.getDb_data().getTheWarRoomIdx();
        GS_CS_BuyStamina.Builder transBuilder = GS_CS_BuyStamina.newBuilder();
        transBuilder.setRoomIdx(roomIdx);
        transBuilder.setPlayerIdx(player.getIdx());
        CrossServerManager.getInstance().sendMsgToWarRoom(roomIdx, MsgIdEnum.GS_CS_BuyStamina_VALUE, transBuilder);

        builder.setRetCode(TheWarRetCode.TWRC_Success);
        GlobalData.getInstance().sendMsg(player.getIdx(), MsgIdEnum.SC_BuyStamia_VALUE, builder);
    }
}

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
import protocol.ServerTransfer.CS_GS_BuyBackCheckRet;
import protocol.ServerTransfer.GS_CS_BuyBackAllPets;
import protocol.TheWar.SC_BuyBack;
import protocol.TheWarDefine.TheWarRetCode;

@MsgId(msgId = MsgIdEnum.CS_GS_BuyBackCheckRet_VALUE)
public class BuyBackCheckRetHandler extends AbstractHandler<CS_GS_BuyBackCheckRet> {
    @Override
    protected CS_GS_BuyBackCheckRet parse(byte[] bytes) throws Exception {
        return CS_GS_BuyBackCheckRet.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_BuyBackCheckRet ret, int i) {
        playerEntity player = playerCache.getByIdx(ret.getPlayerIdx());
        if (player == null) {
            return;
        }
        SC_BuyBack.Builder builder = SC_BuyBack.newBuilder();
        if (ret.getRetCode() != TheWarRetCode.TWRC_Success) {
            builder.setRetCode(ret.getRetCode());
            GlobalData.getInstance().sendMsg(player.getIdx(), MsgIdEnum.SC_BuyBack_VALUE, builder);
            return;
        }
        int buyBackCount = player.getDb_data().getTheWarData().getDailyBuyBackCount();
        Consume consume = TheWarConst.getBuyBackConsume(buyBackCount);
        if (consume == null) {
            builder.setRetCode(TheWarRetCode.TWRC_ConfigNotFound); // 未找到消耗配置
            GlobalData.getInstance().sendMsg(player.getIdx(), MsgIdEnum.SC_BuyBack_VALUE, builder);
            return;
        }
        if (!ConsumeManager.getInstance().consumeMaterial(player.getIdx(), consume, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_TheWar))) {
            builder.setRetCode(TheWarRetCode.TWRC_CurrencyNotEnough_BuyBack); // 道具或货币不足
            GlobalData.getInstance().sendMsg(player.getIdx(), MsgIdEnum.SC_BuyBack_VALUE, builder);
            return;
        }
        SyncExecuteFunction.executeConsumer(player, entity -> entity.getDb_data().getTheWarDataBuilder().setDailyBuyBackCount(buyBackCount + 1));
        player.sendTheWarBuyBackTimes();

        String roomIdx = player.getDb_data().getTheWarRoomIdx();
        GS_CS_BuyBackAllPets.Builder transBuilder = GS_CS_BuyBackAllPets.newBuilder();
        transBuilder.setRoomIdx(roomIdx);
        transBuilder.setPlayerIdx(player.getIdx());
        CrossServerManager.getInstance().sendMsgToWarRoom(roomIdx, MsgIdEnum.GS_CS_BuyBackAllPets_VALUE, transBuilder);

        builder.setRetCode(TheWarRetCode.TWRC_Success);
        GlobalData.getInstance().sendMsg(player.getIdx(), MsgIdEnum.SC_BuyBack_VALUE, builder);
    }
}

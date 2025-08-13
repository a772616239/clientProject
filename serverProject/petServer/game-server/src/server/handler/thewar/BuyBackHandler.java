package server.handler.thewar;

import common.AbstractBaseHandler;
import datatool.StringHelper;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.consume.ConsumeManager;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.thewar.TheWarConst;
import model.warpServer.crossServer.CrossServerManager;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.GS_CS_BuyBackCheck;
import protocol.TheWar.CS_BuyBack;
import protocol.TheWar.SC_BuyBack;
import protocol.TheWarDefine.TheWarRetCode;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_BuyBack_VALUE)
public class BuyBackHandler extends AbstractBaseHandler<CS_BuyBack> {
    @Override
    protected CS_BuyBack parse(byte[] bytes) throws Exception {
        return CS_BuyBack.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_BuyBack req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
        String roomIdx = player.getDb_data().getTheWarRoomIdx();
        if (StringHelper.isNull(roomIdx)) {
            SC_BuyBack.Builder retBuilder = SC_BuyBack.newBuilder();
            retBuilder.setRetCode(TheWarRetCode.TWRC_RoomNotFound);
            gsChn.send(MsgIdEnum.SC_BuyBack_VALUE, retBuilder);
            return;
        }
        int dailyBuyBackTimes = player.getDb_data().getTheWarData().getDailyBuyBackCount();
        if (dailyBuyBackTimes >= TheWarConst.getDailyLimitBuyBackTimes()) {
            SC_BuyBack.Builder retBuilder = SC_BuyBack.newBuilder();
            retBuilder.setRetCode(TheWarRetCode.TWRC_BuyBackTimesUseUp); // 买活次数已用完
            gsChn.send(MsgIdEnum.SC_BuyBack_VALUE, retBuilder);
            return;
        }
        Consume consume = TheWarConst.getBuyBackConsume(dailyBuyBackTimes);
        if (consume == null) {
            SC_BuyBack.Builder retBuilder = SC_BuyBack.newBuilder();
            retBuilder.setRetCode(TheWarRetCode.TWRC_ConfigNotFound); // 未找到买活消耗配置
            gsChn.send(MsgIdEnum.SC_BuyBack_VALUE, retBuilder);
            return;
        }
        if (!ConsumeManager.getInstance().materialIsEnough(playerIdx, consume)) {
            SC_BuyBack.Builder retBuilder = SC_BuyBack.newBuilder();
            retBuilder.setRetCode(TheWarRetCode.TWRC_CurrencyNotEnough_BuyBack); // 道具或货币不足
            gsChn.send(MsgIdEnum.SC_BuyBack_VALUE, retBuilder);
            return;
        }
        GS_CS_BuyBackCheck.Builder builder = GS_CS_BuyBackCheck.newBuilder();
        builder.setPlayerIdx(playerIdx);
        builder.setRoomIdx(roomIdx);
        if (!CrossServerManager.getInstance().sendMsgToWarRoom(roomIdx, MsgIdEnum.GS_CS_BuyBackCheck_VALUE, builder)) {
            SC_BuyBack.Builder retBuilder = SC_BuyBack.newBuilder();
            retBuilder.setRetCode(TheWarRetCode.TWRC_ServerNotFound);
            gsChn.send(MsgIdEnum.SC_BuyBack_VALUE, retBuilder);
        }
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.TheWar;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_BuyBack_VALUE,
                SC_BuyBack.newBuilder().setRetCode(TheWarRetCode.TWRC_AbnormalMaintenance));
    }
}

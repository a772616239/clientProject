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
import protocol.ServerTransfer.GS_CS_BuyStaminaCheck;
import protocol.TheWar.CS_BuyStamia;
import protocol.TheWar.SC_BuyStamia;
import protocol.TheWarDefine.TheWarRetCode;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_BuyStamia_VALUE)
public class BuyStaminaHandler extends AbstractBaseHandler<CS_BuyStamia> {
    @Override
    protected CS_BuyStamia parse(byte[] bytes) throws Exception {
        return CS_BuyStamia.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_BuyStamia req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
        String roomIdx = player.getDb_data().getTheWarRoomIdx();
        if (StringHelper.isNull(roomIdx)) {
            SC_BuyStamia.Builder retBuilder = SC_BuyStamia.newBuilder();
            retBuilder.setRetCode(TheWarRetCode.TWRC_RoomNotFound);
            gsChn.send(MsgIdEnum.SC_BuyStamia_VALUE, retBuilder);
            return;
        }
        int dailyBuyStaminaCount = player.getDb_data().getTheWarData().getDailyBuyStaminaCount();
        if (dailyBuyStaminaCount >= TheWarConst.getDailyLimitBuyStaminaTimes()) {
            SC_BuyStamia.Builder retBuilder = SC_BuyStamia.newBuilder();
            retBuilder.setRetCode(TheWarRetCode.TWRC_BuyStaminaTimesUseUp); // 购买体力次数已用完
            gsChn.send(MsgIdEnum.SC_BuyStamia_VALUE, retBuilder);
            return;
        }
        Consume consume = TheWarConst.getBuyStaminaConsume(dailyBuyStaminaCount);
        if (consume == null) {
            SC_BuyStamia.Builder retBuilder = SC_BuyStamia.newBuilder();
            retBuilder.setRetCode(TheWarRetCode.TWRC_ConfigNotFound); // 未找到购买体力消耗配置
            gsChn.send(MsgIdEnum.SC_BuyStamia_VALUE, retBuilder);
            return;
        }
        if (!ConsumeManager.getInstance().materialIsEnough(playerIdx, consume)) {
            SC_BuyStamia.Builder retBuilder = SC_BuyStamia.newBuilder();
            retBuilder.setRetCode(TheWarRetCode.TWRC_CurrencyNotEnough_BuyStamina); // 道具或货币不足
            gsChn.send(MsgIdEnum.SC_BuyStamia_VALUE, retBuilder);
            return;
        }
        GS_CS_BuyStaminaCheck.Builder builder = GS_CS_BuyStaminaCheck.newBuilder();
        builder.setPlayerIdx(playerIdx);
        builder.setRoomIdx(roomIdx);
        if (!CrossServerManager.getInstance().sendMsgToWarRoom(roomIdx, MsgIdEnum.GS_CS_BuyStaminaCheck_VALUE, builder)) {
            SC_BuyStamia.Builder retBuilder = SC_BuyStamia.newBuilder();
            retBuilder.setRetCode(TheWarRetCode.TWRC_ServerNotFound);
            gsChn.send(MsgIdEnum.SC_BuyStamia_VALUE, retBuilder);
        }
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistForest;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_BuyStamia_VALUE,
                SC_BuyStamia.newBuilder().setRetCode(TheWarRetCode.TWRC_AbnormalMaintenance));
    }
}

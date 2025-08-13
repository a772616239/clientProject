package model.warpServer.crossServer.handler.thewar;

import common.GlobalData;
import common.SyncExecuteFunction;
import datatool.StringHelper;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.thewar.TheWarManager;
import model.warpServer.crossServer.CrossServerManager;
import platform.logs.LogService;
import platform.logs.entity.thewar.TheWarEnterLog;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_PlayerEnterTheWarRet;
import protocol.TheWar.SC_EnterTheWar;
import protocol.TheWarDefine.TheWarRetCode;

@MsgId(msgId = MsgIdEnum.CS_GS_PlayerEnterTheWarRet_VALUE)
public class EnterTheWarHandler extends AbstractHandler<CS_GS_PlayerEnterTheWarRet> {
    @Override
    protected CS_GS_PlayerEnterTheWarRet parse(byte[] bytes) throws Exception {
        return CS_GS_PlayerEnterTheWarRet.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_PlayerEnterTheWarRet ret, int i) {
        String ipPort = gsChn.channel.remoteAddress().toString().substring(1);
        if (StringHelper.isNull(ipPort)) {
            return;
        }
        int serverIndex = CrossServerManager.getInstance().getServerIndexByCsAddr(ipPort);
        if (serverIndex <= 0) {
            return;
        }
        playerEntity player = playerCache.getByIdx(ret.getPlayerIdx());
        if (player == null) {
            return;
        }
        GlobalData.getInstance().sendMsg(ret.getPlayerIdx(), MsgIdEnum.SC_EnterTheWar_VALUE, SC_EnterTheWar.newBuilder().setRetCode(ret.getRetCode()));
        if (ret.getRetCode() == TheWarRetCode.TWRC_Success) {
            if (StringHelper.isNull(player.getDb_data().getTheWarRoomIdx())) {
                SyncExecuteFunction.executeConsumer(player, entity-> {
                    entity.getDb_data().setTheWarRoomIdx(ret.getRoomIdx());
                    entity.getDb_data().getTheWarDataBuilder().setLastSettleTime(ret.getLastSettleTime());
                });
            }
            TheWarManager.getInstance().addJoinedWarPlayer(player);

            CrossServerManager.getInstance().addTheWarPlayer(serverIndex, player.getIdx());
            CrossServerManager.getInstance().addNewAvailableRoomIdx(ret.getRoomIdx(), ret.getServerIndex());
        }
        if (!ret.getIsResume()) {
            LogService.getInstance().submit(new TheWarEnterLog(player, TheWarManager.getInstance().getMapName(), true, ret.getLevel()));
        }
    }
}

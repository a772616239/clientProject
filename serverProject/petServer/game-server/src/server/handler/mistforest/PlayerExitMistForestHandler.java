package server.handler.mistforest;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.warpServer.crossServer.CrossServerManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.CS_LeaveMistForest;
import protocol.MistForest.MistRetCode;
import protocol.MistForest.SC_LeaveMistForest;
import protocol.ServerTransfer.GS_CS_LeaveMistForest;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_LeaveMistForest_VALUE)
public class PlayerExitMistForestHandler extends AbstractBaseHandler<CS_LeaveMistForest> {
    @Override
    protected CS_LeaveMistForest parse(byte[] bytes) throws Exception {
        return CS_LeaveMistForest.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_LeaveMistForest req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
        GS_CS_LeaveMistForest.Builder builder = GS_CS_LeaveMistForest.newBuilder();
        builder.setPlayerIdx(playerIdx);
        if (!CrossServerManager.getInstance().sendMsgToMistForest(playerIdx, MsgIdEnum.GS_CS_LeaveMistForest_VALUE, builder, true)) {
            SC_LeaveMistForest.Builder retBuilder = SC_LeaveMistForest.newBuilder();
            retBuilder.setRetCode(MistRetCode.MRC_NotInMistForest);
            gsChn.send(MsgIdEnum.SC_LeaveMistForest_VALUE, retBuilder);
        }
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistForest;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_LeaveMistForest_VALUE,
                SC_LeaveMistForest.newBuilder().setRetCode(MistRetCode.MRC_AbnormalMaintenance));
    }
}

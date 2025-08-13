package model.warpServer.battleServer.handler;

import common.GlobalData;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.warpServer.battleServer.BattleServerManager;
import protocol.Battle;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_GS_BattleWatch;

@MsgId(msgId = MsgIdEnum.BS_GS_BattleWatch_VALUE)
public class BattlerWatchHandler extends AbstractHandler<BS_GS_BattleWatch> {
    @Override
    protected BS_GS_BattleWatch parse(byte[] bytes) throws Exception {
        return BS_GS_BattleWatch.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, BS_GS_BattleWatch req, int i) {
        playerEntity player = playerCache.getByIdx(req.getPlayerId());
        if (player == null) {
            return;
        }
        Battle.SC_BattleWatch.Builder builder = Battle.SC_BattleWatch.newBuilder();
        if (req.getRetCodeValue() <= 0) {
            builder.setFrameIndex(req.getRevertData().getFrameIndex());
            builder.setEnterFightData(req.getRevertData().getEnterFightData());
            builder.addAllFrameData(req.getRevertData().getFrameDataList());
            BattleServerManager.getInstance().getPlayerWatchInfo().put(player.getIdx(), req.getBsid());
        }
        GlobalData.getInstance().sendMsg(player.getIdx(), MsgIdEnum.SC_BattleWatch_VALUE, builder);
    }
}

package model.warpServer.battleServer.handler;

import common.GlobalData;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.battle.BattleManager;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.Battle.SC_BattleRevertData;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_GS_RevertBattle;

@MsgId(msgId = MsgIdEnum.BS_GS_RevertBattle_VALUE)
public class BattlerRevertHandler extends AbstractHandler<BS_GS_RevertBattle> {
    @Override
    protected BS_GS_RevertBattle parse(byte[] bytes) throws Exception {
        return BS_GS_RevertBattle.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, BS_GS_RevertBattle req, int i) {
        playerEntity player = playerCache.getByIdx(req.getPlayerIdx());
        if (player == null) {
            return;
        }
        SC_BattleRevertData.Builder builder = SC_BattleRevertData.newBuilder();
        if (req.getResult()) {
            builder.setIsBattling(true);
            builder.setFrameIndex(req.getRevertData().getFrameIndex());
            builder.setEnterFightData(req.getRevertData().getEnterFightData());
            builder.addAllFrameData(req.getRevertData().getFrameDataList());
        } else {
            BattleManager.getInstance().clearController(req.getPlayerIdx());
//            SyncExecuteFunction.executeConsumer(player, entity -> entity.getBattleController().clear());
        }
        GlobalData.getInstance().sendMsg(player.getIdx(), MsgIdEnum.SC_BattleRevertData_VALUE, builder);
    }
}

package model.warpServer.crossServer.handler.mistforest;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.battle.BattleManager;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_MistPvpBattleResult;

@MsgId(msgId = MsgIdEnum.CS_GS_MistPvpBattleResult_VALUE)
public class MistPvpBattleResultHandler extends AbstractHandler<CS_GS_MistPvpBattleResult> {
    @Override
    protected CS_GS_MistPvpBattleResult parse(byte[] bytes) throws Exception {
        return CS_GS_MistPvpBattleResult.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_MistPvpBattleResult req, int i) {
        playerEntity player = playerCache.getByIdx(req.getPlayerIdx());
        if (player == null) {
            return;
        }

        BattleManager.getInstance().settleBattle(req.getPlayerIdx(), req.getBattleId(), req.getWinnerCamp());
//
//        SyncExecuteFunction.executeConsumer(player, player1 -> {
//            player1.getBattleController().settlePvpBattle(req.getBattleId(), req.getWinnerCamp());
//        });
    }
}

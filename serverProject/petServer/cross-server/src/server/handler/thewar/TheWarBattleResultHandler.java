package server.handler.thewar;

import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.thewar.warmap.WarMapData;
import model.thewar.warmap.WarMapManager;
import model.thewar.warmap.grid.FootHoldGrid;
import model.thewar.warmap.grid.WarMapGrid;
import model.thewar.warplayer.dbCache.WarPlayerCache;
import model.thewar.warplayer.entity.WarPlayer;
import model.thewar.warroom.dbCache.WarRoomCache;
import model.thewar.warroom.entity.WarRoom;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.GS_CS_TheWarBattleResult;
import protocol.TargetSystem.TargetTypeEnum;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.GS_CS_TheWarBattleResult_VALUE)
public class TheWarBattleResultHandler extends AbstractHandler<GS_CS_TheWarBattleResult> {
    @Override
    protected GS_CS_TheWarBattleResult parse(byte[] bytes) throws Exception {
        return GS_CS_TheWarBattleResult.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_CS_TheWarBattleResult req, int i) {
        try {
            WarPlayer warPlayer = WarPlayerCache.getInstance().queryObject(req.getPlayerIdx());
            if (warPlayer == null) {
                return;
            }
            WarRoom warRoom = WarRoomCache.getInstance().queryObject(warPlayer.getRoomIdx());
            if (warRoom == null || warRoom.needClear()) {
                return;
            }
            WarMapData mapData = WarMapManager.getInstance().getRoomMapData(warPlayer.getRoomIdx());
            if (mapData == null) {
                return;
            }
            WarMapGrid grid = mapData.getMapGridByPos(req.getBattleGridPos());
            if (grid == null || !(grid instanceof FootHoldGrid)) {
                return;
            }
            boolean battleResult = req.getFightStar() >= 0;
            FootHoldGrid footHoldGrid = (FootHoldGrid) grid;
            SyncExecuteFunction.executeConsumer(footHoldGrid, entity-> entity.settleBattle(warPlayer, battleResult, req.getRemainMonstersList(), req.getFightStar()));
            SyncExecuteFunction.executeConsumer(warPlayer, entity-> entity.addTargetProgress(TargetTypeEnum.TTE_TheWar_KillMonsterCount, 0, req.getNewKillPetCount()));
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }
}

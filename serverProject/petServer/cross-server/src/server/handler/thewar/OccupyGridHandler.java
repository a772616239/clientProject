package server.handler.thewar;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.thewar.WarConst.RoomState;
import model.thewar.warmap.WarMapData;
import model.thewar.warmap.WarMapManager;
import model.thewar.warplayer.dbCache.WarPlayerCache;
import model.thewar.warplayer.entity.WarPlayer;
import model.thewar.warroom.dbCache.WarRoomCache;
import model.thewar.warroom.entity.WarRoom;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer.CS_GS_ResOccupyGrid;
import protocol.ServerTransfer.GS_CS_ReqOccupyGrid;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.GS_CS_ReqOccupyGrid_VALUE)
public class OccupyGridHandler extends AbstractHandler<GS_CS_ReqOccupyGrid> {
    @Override
    protected GS_CS_ReqOccupyGrid parse(byte[] bytes) throws Exception {
        return GS_CS_ReqOccupyGrid.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_CS_ReqOccupyGrid req, int i) {
        WarPlayer warPlayer = WarPlayerCache.getInstance().queryObject(req.getPlayerIdx());
        CS_GS_ResOccupyGrid.Builder retBuilder = CS_GS_ResOccupyGrid.newBuilder();
        retBuilder.setPlayerIdx(req.getPlayerIdx());
        if (warPlayer == null) {
            retBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_TheWar_NotJoinTheWar));
            gsChn.send(MsgIdEnum.CS_GS_ResOccupyGrid_VALUE, retBuilder);
            return;
        }
        WarRoom room = WarRoomCache.getInstance().queryObject(warPlayer.getRoomIdx());
        if (room == null || room.needClear()) {
            retBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_TheWar_RoomEnded));
            gsChn.send(MsgIdEnum.CS_GS_ResOccupyGrid_VALUE, retBuilder);
            return;
        }
        if (room.getRoomState() != RoomState.FightingState || room.isPreSettleFlag()) {
            retBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_TheWar_RoomEnded));
            gsChn.send(MsgIdEnum.CS_GS_ResOccupyGrid_VALUE, retBuilder);
            return;
        }
        WarMapData mapData = WarMapManager.getInstance().getRoomMapData(room.getIdx());
        if (mapData == null) {
            retBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_TheWar_NotFoundMap));
            gsChn.send(MsgIdEnum.CS_GS_ResOccupyGrid_VALUE, retBuilder);
            return;
        }
        if (warPlayer.getPlayerData().getBattleData().getEnterFightTime() > 0) {
            retBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_TheWar_AttackOtherGrid));
            gsChn.send(MsgIdEnum.CS_GS_ResOccupyGrid_VALUE, retBuilder);
            return;
        }
        if (!room.checkOwnedAroundPos(warPlayer.getCamp(), req.getPos())) {
            retBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_TheWar_NotFoundAroundTeamGrid));
            gsChn.send(MsgIdEnum.CS_GS_ResOccupyGrid_VALUE, retBuilder);
            return;
        }
        RetCodeEnum retCode = mapData.playerOccupyGrid(warPlayer, req.getPos(), req.getSkipBattle());
        retBuilder.setRetCode(GameUtil.buildRetCode(retCode));
        gsChn.send(MsgIdEnum.CS_GS_ResOccupyGrid_VALUE, retBuilder);
    }
}

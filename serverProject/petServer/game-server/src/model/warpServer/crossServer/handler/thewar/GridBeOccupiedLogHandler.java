package model.warpServer.crossServer.handler.thewar;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.thewar.TheWarManager;
import platform.logs.LogService;
import platform.logs.entity.thewar.TheWarOccupyGridLog;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_TheWarGridBeenOccupiedLog;
import protocol.TheWarDefine.WarCellTagFlag;

@MsgId(msgId = MsgIdEnum.CS_GS_TheWarGridBeenOccupiedLog_VALUE)
public class GridBeOccupiedLogHandler extends AbstractHandler<CS_GS_TheWarGridBeenOccupiedLog> {
    @Override
    protected CS_GS_TheWarGridBeenOccupiedLog parse(byte[] bytes) throws Exception {
        return CS_GS_TheWarGridBeenOccupiedLog.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_TheWarGridBeenOccupiedLog ret, int i) {
        playerEntity player = playerCache.getByIdx(ret.getPlayerIdx());
        if (player == null) {
            return;
        }
        String gridTypeStr = "";
        int gridType = ret.getGridData().getGridType();
        if ((gridType & WarCellTagFlag.WCTF_Normal_Manor_VALUE) > 0) {
            gridTypeStr = "普通领地";
        } else if ((gridType & WarCellTagFlag.WCTF_WarGold_Mine_VALUE) > 0) {
            gridTypeStr = "远征币领地";
        } else if ((gridType & WarCellTagFlag.WCTF_OpenDoor_Mine_VALUE) > 0) {
            gridTypeStr = "圣水领地";
        } else if ((gridType & WarCellTagFlag.WCTF_HolyWater_Mine_VALUE) > 0) {
            gridTypeStr = "开门资源领地";
        } else if ((gridType & WarCellTagFlag.WCTF_Fortress_VALUE) > 0) {
            gridTypeStr = "要塞领地";
        }
        LogService.getInstance().submit(new TheWarOccupyGridLog(player, TheWarManager.getInstance().getMapName(), gridTypeStr,
                ret.getGridData().getGridLevel(), ret.getGridData().getPos().getX(), ret.getGridData().getPos().getY(),
                ret.getAttackerName(), ret.getGridData().getHasTrooped()));
    }
}

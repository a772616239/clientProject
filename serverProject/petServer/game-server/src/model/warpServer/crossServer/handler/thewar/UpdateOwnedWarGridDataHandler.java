package model.warpServer.crossServer.handler.thewar;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_TheWarUpdateOwnedGridData;
import protocol.ServerTransfer.WarGridLogDbData;

@MsgId(msgId = MsgIdEnum.CS_GS_TheWarUpdateOwnedGridData_VALUE)
public class UpdateOwnedWarGridDataHandler extends AbstractHandler<CS_GS_TheWarUpdateOwnedGridData> {
    @Override
    protected CS_GS_TheWarUpdateOwnedGridData parse(byte[] bytes) throws Exception {
        return CS_GS_TheWarUpdateOwnedGridData.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_TheWarUpdateOwnedGridData ret, int i) {
        playerEntity player = playerCache.getByIdx(ret.getPlayerIdx());
        if (player == null) {
            return;
        }
        if (ret.getBAdd()) {
            boolean owned = false;
            WarGridLogDbData.Builder gridData;
            for (int j = 0; j < player.getDb_data().getTheWarDataBuilder().getOwedGridDataCount(); j++) {
                gridData = player.getDb_data().getTheWarDataBuilder().getOwedGridDataBuilder(j);
                if (gridData.getPos().equals(ret.getGridData().getPos())) {
                    owned = true;
                    gridData.setGridType(ret.getGridData().getGridType());
                    gridData.setGridLevel(ret.getGridData().getGridLevel());
                    gridData.setHasTrooped(ret.getGridData().getHasTrooped());
                    break;
                }
            }
            if (!owned) {
                player.getDb_data().getTheWarDataBuilder().addOwedGridData(ret.getGridData());
            }
        } else {
            WarGridLogDbData.Builder gridData;
            for (int j = 0; j < player.getDb_data().getTheWarDataBuilder().getOwedGridDataCount(); j++) {
                gridData = player.getDb_data().getTheWarDataBuilder().getOwedGridDataBuilder(j);
                if (gridData.getPos().equals(ret.getGridData().getPos())) {
                    player.getDb_data().getTheWarDataBuilder().removeOwedGridData(j);
                    break;
                }
            }
        }
    }
}

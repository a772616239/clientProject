package server.handler.mistforest;

import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.mistforest.room.entity.MistRoom;
import model.mistplayer.cache.MistPlayerCache;
import model.mistplayer.entity.MistPlayer;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.MistRetCode;
import protocol.ServerTransfer.CS_GS_LeaveMistForest;
import protocol.ServerTransfer.GS_CS_LeaveMistForest;

@MsgId(msgId = MsgIdEnum.GS_CS_LeaveMistForest_VALUE)
public class LeaveMistForestHandler extends AbstractHandler<GS_CS_LeaveMistForest> {
    @Override
    protected GS_CS_LeaveMistForest parse(byte[] bytes) throws Exception {
        return GS_CS_LeaveMistForest.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_CS_LeaveMistForest req, int i) {
        CS_GS_LeaveMistForest.Builder retBuilder = CS_GS_LeaveMistForest.newBuilder();
        retBuilder.setPlayerIdx(req.getPlayerIdx());
        MistPlayer player = MistPlayerCache.getInstance().queryObject(req.getPlayerIdx());
        if (player == null) {
            retBuilder.setRetCode(MistRetCode.MRC_TargetNotFound);
            gsChn.send(MsgIdEnum.CS_GS_LeaveMistForest_VALUE, retBuilder);
            return;
        }
        MistRoom room = player.getMistRoom();
        if (room == null) {
            retBuilder.setRetCode(MistRetCode.MRC_NotInMistForest);
            gsChn.send(MsgIdEnum.CS_GS_LeaveMistForest_VALUE, retBuilder);
            return;
        }
        MistRetCode retCode = SyncExecuteFunction.executeFunction(room, mistRoom -> mistRoom.onPlayerApplyExit(player));
        if (retCode == MistRetCode.MRC_Success) {
            return;
        }
        retBuilder.setRetCode(retCode);
        gsChn.send(MsgIdEnum.CS_GS_LeaveMistForest_VALUE, retBuilder);

    }
}

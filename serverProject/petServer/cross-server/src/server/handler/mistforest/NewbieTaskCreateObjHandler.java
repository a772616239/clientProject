package server.handler.mistforest;

import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.room.entity.MistRoom;
import model.mistplayer.cache.MistPlayerCache;
import model.mistplayer.entity.MistPlayer;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.GS_CS_NewbieTaskCreateObj;

@MsgId(msgId = MsgIdEnum.GS_CS_NewbieTaskCreateObj_VALUE)
public class NewbieTaskCreateObjHandler extends AbstractHandler<GS_CS_NewbieTaskCreateObj> {
    @Override
    protected GS_CS_NewbieTaskCreateObj parse(byte[] bytes) throws Exception {
        return GS_CS_NewbieTaskCreateObj.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_CS_NewbieTaskCreateObj req, int i) {
        MistPlayer mistPlayer = MistPlayerCache.getInstance().queryObject(req.getPlayerIdx());
        if (mistPlayer == null) {
            return;
        }
        MistRoom mistRoom = mistPlayer.getMistRoom();
        if (mistRoom == null) {
            return;
        }
        MistFighter fighter = mistRoom.getObjManager().getMistObj(mistPlayer.getFighterId());
        if (fighter == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(mistRoom, room -> fighter.initNewbieTask(req.getNewbieTaskId()));
    }
}

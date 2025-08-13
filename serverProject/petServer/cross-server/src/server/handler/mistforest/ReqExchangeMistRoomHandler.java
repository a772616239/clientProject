package server.handler.mistforest;

import cfg.MistWorldMapConfig;
import cfg.MistWorldMapConfigObject;
import common.GameConst.EventType;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.mistforest.room.entity.MistRoom;
import model.mistplayer.cache.MistPlayerCache;
import model.mistplayer.entity.MistPlayer;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.MistRetCode;
import protocol.ServerTransfer.CS_GS_ResExchangeMistRoom;
import protocol.ServerTransfer.GS_CS_ReqExchangeMistRoom;
import server.event.Event;
import server.event.EventManager;

@MsgId(msgId = MsgIdEnum.GS_CS_ReqExchangeMistRoom_VALUE)
public class ReqExchangeMistRoomHandler extends AbstractHandler<GS_CS_ReqExchangeMistRoom> {
    @Override
    protected GS_CS_ReqExchangeMistRoom parse(byte[] bytes) throws Exception {
        return GS_CS_ReqExchangeMistRoom.parseFrom(bytes);
    }


    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_CS_ReqExchangeMistRoom req, int i) {
        CS_GS_ResExchangeMistRoom.Builder builder = CS_GS_ResExchangeMistRoom.newBuilder();
        builder.setIdx(req.getIdx());
        MistPlayer player = MistPlayerCache.getInstance().queryObject(req.getIdx());
        if (player == null) {
            builder.setRetCode(MistRetCode.MRC_TargetNotFound);
            gsChn.send(MsgIdEnum.CS_GS_ResExchangeMistRoom_VALUE, builder);
            return;
        }
        MistWorldMapConfigObject worldMapCfg = MistWorldMapConfig.getByMapid(req.getNewMapId());
        if (worldMapCfg == null) {
            builder.setRetCode(MistRetCode.MRC_NotFoundMap);
            gsChn.send(MsgIdEnum.CS_GS_ResExchangeMistRoom_VALUE, builder);
            return;
        }
        MistRoom room = player.getMistRoom();
        if (room == null) {
            builder.setRetCode(MistRetCode.MRC_TargetNotInRoom);
            gsChn.send(MsgIdEnum.CS_GS_ResExchangeMistRoom_VALUE, builder);
            return;
        }
        MistRetCode ret = SyncExecuteFunction.executeFunction(room, r->r.onPlayerExit(player, true));
        if (ret == MistRetCode.MRC_Success) {
            Event event = Event.valueOf(EventType.ET_ExchangeMistForest, room, player);
            event.pushParam(worldMapCfg);
            EventManager.getInstance().dispatchEvent(event);
        }
        builder.setRetCode(ret);
        gsChn.send(MsgIdEnum.CS_GS_ResExchangeMistRoom_VALUE, builder);

    }
}

package server.handler.mistforest;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.room.entity.MistRoom;
import model.mistplayer.cache.MistPlayerCache;
import model.mistplayer.entity.MistPlayer;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer.CS_GS_ReplyUpdatePetData;
import protocol.ServerTransfer.GS_CS_ApplyUpdatePetData;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.GS_CS_ApplyUpdatePetData_VALUE)
public class ApplyUpdateTeamHandler extends AbstractHandler<GS_CS_ApplyUpdatePetData> {
    @Override
    protected GS_CS_ApplyUpdatePetData parse(byte[] bytes) throws Exception {
        return GS_CS_ApplyUpdatePetData.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_CS_ApplyUpdatePetData req, int i) {
        MistPlayer mistPlayer = MistPlayerCache.getInstance().queryObject(req.getIdx());
        CS_GS_ReplyUpdatePetData.Builder builder = CS_GS_ReplyUpdatePetData.newBuilder();
        builder.setIdx(req.getIdx());
        if (mistPlayer == null) {
            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_MistForest_NotFoundMistPlayer));
            gsChn.send(MsgIdEnum.CS_GS_ReplyUpdatePetData_VALUE, builder);
            return;
        }
        MistRoom room = mistPlayer.getMistRoom();
        if (room == null) {
            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_MistForest_NotInMistRoom));
            gsChn.send(MsgIdEnum.CS_GS_ReplyUpdatePetData_VALUE, builder);
            return;
        }
        MistFighter fighter = room.getObjManager().getMistObj(mistPlayer.getFighterId());
        if (fighter == null) {
            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_MistForest_NotFoundMistFighter));
            gsChn.send(MsgIdEnum.CS_GS_ReplyUpdatePetData_VALUE, builder);
            return;
        }
        builder.setRetCode(GameUtil.buildRetCode(
                fighter.isInSafeRegion() ? RetCodeEnum.RCE_Success : RetCodeEnum.RCE_MistForest_NotInSafeRegion));
        builder.setUpdateTeamData(req.getUpdateTeamData());
        gsChn.send(MsgIdEnum.CS_GS_ReplyUpdatePetData_VALUE, builder);
    }
}

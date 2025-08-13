package server.handler.mistforest;

import common.GameConst.EventType;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.mistplayer.cache.MistPlayerCache;
import model.mistplayer.entity.MistPlayer;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.GS_CS_UpdatePetData;
import server.event.Event;
import server.event.EventManager;

@MsgId(msgId = MsgIdEnum.GS_CS_UpdatePetData_VALUE)
public class UpdateMistBattleTeamHandler extends AbstractHandler<GS_CS_UpdatePetData> {
    @Override
    protected GS_CS_UpdatePetData parse(byte[] bytes) throws Exception {
        return GS_CS_UpdatePetData.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_CS_UpdatePetData req, int i) {
        MistPlayer mistPlayer = MistPlayerCache.getInstance().queryObject(req.getIdx());
        if (mistPlayer == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(mistPlayer, player -> {
            player.updatePetDataList(req.getPetDataList());
            player.updateSkillList(req.getSkillDataList());
            player.updateBaseAdditions(req.getBaseAdditionsMap());
        });
        if (mistPlayer.getMistRoom() != null) {
            Event event = Event.valueOf(EventType.ET_CalcFighterRemainHpRate, mistPlayer, mistPlayer.getMistRoom());
            EventManager.getInstance().dispatchEvent(event);
        }
    }
}

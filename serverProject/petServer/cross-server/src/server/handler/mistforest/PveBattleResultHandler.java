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
import protocol.ServerTransfer.GS_CS_MistPveBattleResult;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.GS_CS_MistPveBattleResult_VALUE)
public class PveBattleResultHandler extends AbstractHandler<GS_CS_MistPveBattleResult> {
    @Override
    protected GS_CS_MistPveBattleResult parse(byte[] bytes) throws Exception {
        return GS_CS_MistPveBattleResult.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_CS_MistPveBattleResult req, int i) {
        MistPlayer player = MistPlayerCache.getInstance().queryObject(req.getIdx());
        if (player == null) {
            return;
        }
        MistRoom room = player.getMistRoom();
        if (room == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(room, room1 -> {
            MistFighter fighter = room1.getObjManager().getMistObj(player.getFighterId());
            if (fighter == null) {
                return;
            }
//            if (!req.getIsWinner()) {
//                Event dropEvent = Event.valueOf(EventType.ET_CalcPlayerDropItem, GameUtil.getDefaultEventSource(), GameUtil.getDefaultEventSource());
//                boolean isPkMode = fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerAttackMode_VALUE) == MistAttackModeEnum.EAME_Attack_VALUE;
//                dropEvent.pushParam(isPkMode, fighter.getOwnerPlayerInSameRoom(), null);
//                EventManager.getInstance().dispatchEvent(dropEvent);
//            }
            fighter.onPveBattleSettle(req.getIsWinner(), req.getPveTypeValue(), req.getDamage(), false);
        });

        SyncExecuteFunction.executeConsumer(player, entity -> entity.setPetRemainHp(req.getIsWinner() ? 1 : 0, req.getRemainPetDataList()));
    }
}

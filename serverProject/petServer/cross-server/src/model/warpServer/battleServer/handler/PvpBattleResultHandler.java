package model.warpServer.battleServer.handler;

import common.GameConst.EventType;
import common.GlobalData;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.mistforest.MistConst;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.room.entity.MistRoom;
import model.mistplayer.cache.MistPlayerCache;
import model.mistplayer.entity.MistPlayer;
import model.warpServer.battleServer.BattleServerManager;
import protocol.Battle.BattleSubTypeEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.ServerTransfer.BS_CS_BattleResult;
import protocol.ServerTransfer.CS_GS_MistPvpBattleResult;
import protocol.ServerTransfer.PvpBattlePlayerInfo;
import protocol.ServerTransfer.PvpBattleResultData;
import server.event.Event;
import server.event.EventManager;

@MsgId(msgId = MsgIdEnum.BS_CS_BattleResult_VALUE)
public class PvpBattleResultHandler extends AbstractHandler<BS_CS_BattleResult> {
    @Override
    protected BS_CS_BattleResult parse(byte[] bytes) throws Exception {
        return BS_CS_BattleResult.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, BS_CS_BattleResult req, int i) {
        if (req.getPvpBattleResultData().getSubBattleType() == BattleSubTypeEnum.BSTE_MistForest) {
            handleMistForestBattleResult(req.getPvpBattleResultData());
        } else if (req.getPvpBattleResultData().getSubBattleType() == BattleSubTypeEnum.BSTE_MineFight) {
            handlerMineFightBattleResult(req.getPvpBattleResultData());
        }
    }

    private void handleMistForestBattleResult(PvpBattleResultData req) {
        MistRoom room = checkPlayerValid(req);
        if (room == null) {
            return;
        }
        CS_GS_MistPvpBattleResult.Builder builder = CS_GS_MistPvpBattleResult.newBuilder();
        builder.setBattleId(req.getBattleId());
        builder.setWinnerCamp(req.getBattleResult().getWinnerCamp());
        boolean robBossKey = false;
        boolean terminate = false;
        boolean beatWantedPlayer = false;
        long jewelryCount = 0;
        long lavaBadge = 0;
        for (PvpBattlePlayerInfo playerData : req.getPlayerListList()) {
            BattleServerManager.getInstance().removePlayerBattleInfo(playerData.getPlayerInfo().getPlayerId());
            MistPlayer player = MistPlayerCache.getInstance().queryObject(playerData.getPlayerInfo().getPlayerId());
            if (player == null) {
                continue;
            }
            builder.setPlayerIdx(player.getIdx());
            GlobalData.getInstance().sendMsgToServer(
                    player.getServerIndex(), MsgIdEnum.CS_GS_MistPvpBattleResult_VALUE, builder);
            if (req.getBattleResult().getWinnerCamp() > 0 && req.getBattleResult().getWinnerCamp() != playerData.getCamp()) {
                MistFighter fighter = room.getObjManager().getMistObj(player.getFighterId());
                if (fighter != null) {
                    if (fighter.getAttribute(MistUnitPropTypeEnum.MUPT_OwningKeyState_VALUE) > 0) {
                        robBossKey = true;
                    }
                    if (fighter.getContinualKillCount() >= MistConst.ContinualKillCount) {
                        terminate = true;
                    }
                    if (fighter.getAttribute(MistUnitPropTypeEnum.MUPT_IsWantedState_VALUE) > 0) {
                        beatWantedPlayer = true;
                    }
                    jewelryCount = fighter.getAttribute(MistUnitPropTypeEnum.MUPT_JewelryCount_VALUE);
                    lavaBadge = fighter.getAttribute(MistUnitPropTypeEnum.MUPT_LavaBadgeCount_VALUE);
                }
            }
        }
        for (PvpBattlePlayerInfo playerData : req.getPlayerListList()) {
            MistPlayer player = MistPlayerCache.getInstance().queryObject(playerData.getPlayerInfo().getPlayerId());
            if (player == null) {
                continue;
            }
            SyncExecuteFunction.executeConsumer(player, entity -> entity.setPetRemainHp(req.getBattleResult().getWinnerCamp() == playerData.getCamp() ? playerData.getCamp() : 0, req.getRemainPetDataList()));

            Event event = Event.valueOf(EventType.ET_SettleMistPvpBattle, player, room);
            event.pushParam(req.getBattleResult().getWinnerCamp(), playerData.getCamp(), robBossKey, terminate, beatWantedPlayer, jewelryCount, lavaBadge);
            EventManager.getInstance().dispatchEvent(event);
        }
    }

    private void handlerMineFightBattleResult(PvpBattleResultData req) {
//        CS_GS_MinePvpFightResult.Builder builder = CS_GS_MinePvpFightResult.newBuilder();
//        builder.setBattleId(req.getBattleId());
//        builder.setWinnerCamp(req.getBattleResult().getWinnerCamp());
//        MineObj mineObj = null;
//        if (req.getParamsCount() > 0) {
//            mineObj = MineObjCache.getByIdx(req.getParams(0));
//        }
//        if (mineObj != null) {
//            // 平局算掠夺方失败
//            SyncExecuteFunction.executeConsumer(mineObj, mineObj1 ->
//                    mineObj1.settleMineBattle(req.getBattleResult().getWinnerCamp() == 1, GlobalTick.getInstance().getCurrentTime()));
//        }
//        for (PvpBattlePlayerInfo playerData : req.getPlayerListList()) {
//            BattleServerManager.getInstance().removePlayerBattleInfo(playerData.getPlayerInfo().getPlayerId());
//            builder.setPlayerIdx(playerData.getPlayerInfo().getPlayerId());
//            GlobalData.getInstance().sendMsgToServer(
//                    playerData.getFromSvrIp(), MsgIdEnum.CS_GS_MinePvpFightResult_VALUE, builder);
//        }
    }

    private MistRoom checkPlayerValid(PvpBattleResultData req) {
        MistRoom room = null;
        for (PvpBattlePlayerInfo playerData : req.getPlayerListList()) {
            MistPlayer player = MistPlayerCache.getInstance().queryObject(playerData.getPlayerInfo().getPlayerId());
            if (player == null || player.getMistRoom() == null) {
                continue;
            }
            if (room == null) {
                room = player.getMistRoom();
            } else if (!room.getIdx().equals(player.getMistRoom().getIdx())) {
                return null;
            }
        }
        return room;
    }
}

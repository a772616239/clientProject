package server.event;

import cfg.CrossConstConfig;
import cfg.MistMapObjConfigObject;
import cfg.MistWorldMapConfig;
import cfg.MistWorldMapConfigObject;
import cfg.TheWarConstConfig;
import cfg.TheWarConstConfigObject;
import cfg.TheWarMonsterRefreshConfig;
import cfg.TheWarMonsterRefreshConfigObject;
import com.alibaba.fastjson.JSONObject;
import common.GameConst;
import common.GameConst.EventType;
import common.GlobalData;
import common.GlobalTick;
import common.HttpRequestUtil;
import common.IdGenerator;
import common.SyncExecuteFunction;
import common.entity.RankingUpdateRequest;
import datatool.StringHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import model.arena.ArenaManager;
import model.mistforest.MistConst;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.mistobj.rewardobj.MistRewardObj;
import model.mistforest.room.cache.MistRoomCache;
import model.mistforest.room.entity.MistGhostBusterRoom.MistGhostBusterRoom;
import model.mistforest.room.entity.MistRoom;
import model.mistforest.team.MistTeam;
import model.mistplayer.cache.MistPlayerCache;
import model.mistplayer.entity.MistPlayer;
import model.obj.BaseObj;
import model.thewar.WarConst;
import model.thewar.warmap.WarMapData;
import model.thewar.warmap.WarMapManager;
import model.thewar.warmap.grid.BossGrid;
import model.thewar.warmap.grid.FootHoldGrid;
import model.thewar.warmap.grid.PortalGrid;
import model.thewar.warmap.grid.WarMapGrid;
import model.thewar.warplayer.dbCache.WarPlayerCache;
import model.thewar.warplayer.entity.WarPlayer;
import model.thewar.warroom.dbCache.WarRoomCache;
import model.thewar.warroom.entity.WarRoom;
import model.timer.TimerConst.TimerTargetType;
import org.springframework.util.CollectionUtils;
import protocol.Battle.BattleRemainPet;
import protocol.Battle.PetBuffData;
import protocol.MessageId.MsgIdEnum;
import protocol.MineFight.EnumMineState;
import protocol.MistForest.EnumMistRuleKind;
import protocol.MistForest.EnumMistTipsType;
import protocol.MistForest.GhostBusterRoomState;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.ProtoVector;
import protocol.PetMessage.PetProperty;
import protocol.Server.DB_TimerParam;
import protocol.ServerTransfer.CS_GS_ArenaDanSettle;
import protocol.ServerTransfer.CS_GS_EnterMistPveBattle;
import protocol.ServerTransfer.CS_GS_GainBossActivityReward;
import protocol.ServerTransfer.CS_GS_TheWarCurrencyLog;
import protocol.ServerTransfer.CS_GS_TheWarCurrencyLog.CurrencyLogData;
import protocol.ServerTransfer.CS_GS_UpdateEliteMonsterRewardTimes;
import protocol.TargetSystem.TargetTypeEnum;
import protocol.TheWar.SC_KickOutFromTheWar;
import protocol.TheWar.StationTroopsInfo;
import protocol.TheWar.WarReward;
import protocol.TheWarDB.GridCacheData;
import protocol.TheWarDB.PlayerCacheData;
import protocol.TheWarDB.RoomCacheData;
import protocol.TheWarDefine.Position;
import protocol.TheWarDefine.SC_UpdateWarPetData;
import protocol.TheWarDefine.TheWarCellPropertyEnum;
import protocol.TheWarDefine.TheWarResourceType;
import protocol.TheWarDefine.WarPetData;
import protocol.TransServerCommon.MistGhostBusterSyncData;
import util.GameUtil;
import util.LogUtil;
import util.TimeUtil;

public class EventListener {
    public static boolean listenEvent() {
        int result = 1;
        result &= listenEvent(EventType.ET_Login, new LoginEventHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_Logout, new LogoutEventHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_Offline, new OfflineEventHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_ExchangeMistForest, new ExchangeMistForestEventHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_EnterMistPveBattle, new EnterMistPveBattleEventHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_SetMistPlayerPetRemainHp, new SetMistPlayerPetRemainHpEventHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_EnterMistPvpBattle, new EnterMistPvpBattleEventHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_SettleMistPvpBattle, new SettleMistPvpBattleEventHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_RemoveMistPlayer, new RemoveMistPlayerEventHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_ConsumeLootPackReward, new ConsumeLootPackRewardEventHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_MonsterBattleCarryReward, new MonsterBattleCarryRewardHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_GainMistCarryReward, new GainMistBagCountEventHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_CalcFighterRemainHpRate, new CalcFighterRemainHpRateEventHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_ChangePlayerHpRate, new ChangePlayerHpRatEventHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_ChangeRecoverHpIntervalRate, new ChangeRecoverHpIntervalRateEventHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_CalcPlayerDropItem, new CalcPlayerDropItemEventHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_UpdateEliteMonsterRewardTimes, new UpdateEliteMonsterRewardTimesEventHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_CreateGhostBusterRoom, new CreateGhostBusterRoomEventHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_AttackerGiveup, new AttackerGiveupEventHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_TimerInvoke, new TimerInvokeHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_RANKING_UPDATE, new UpdateRankingHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_RANKING_CLEAR, new ClearRankingHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_QUERY_ARENA_ROOM_RANKING, new QueryArenaRoomRankingHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_TheWar_AddFootHoldGrid, new AddFootHoldGridHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_TheWar_AddUnsettledCurrency, new AddUnsettledCurrencyHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_TheWar_ModifyPetTroopsData, new ModifyPetTroopsDataHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_TheWar_RemovePetTroopsData, new RemovePetTroopsDataHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_TheWar_SettleAfkReward, new SettleAfkRewardHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_TheWar_AddPlayerCache, new AddPlayerCacheHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_TheWar_AddGridCache, new AddGridCacheHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_TheWar_AddWarGridRecord, new AddWarGridRecordHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_TheWar_RemoveDpResource, new RemoveDpResourceHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_TheWar_AddPosGroupGrid, new AddPosGroupGridHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_TheWar_RemovePosGroupGrid, new RemovePosGroupGridHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_TheWar_AddTargetProgress, new AddTargetProgressHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_TheWar_AddPortalGridProgress, new AddPortalGridProgressHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_TheWar_AddRefreshMonsterGrid, new AddRefreshMonsterGridHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_TheWar_RemoveRefreshMonsterGrid, new RemoveRefreshMonsterGridHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_TheWar_AddRefreshMonsterCount, new AddRefreshMonsterCountHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_TheWar_DecRefreshMonsterCount, new DecRefreshMonsterCountHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_TheWar_ChangePlayerStamina, new ChangePlayerStaminaHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_TheWar_ChangeTargetGridProperty, new ChangeGridPropertyHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_TheWar_AddPlayerBattleState, new AddPlayerBattleStateHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_TheWar_TakeUpRoom, new TakeUpRoomHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_TheWar_RoomSettle, new RoomSettleHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_TheWar_ClearFootHoldGrid, new ClearFootHoldGridHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_TheWar_ClearStationPetGrid, new ClearStationPetGridHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_TheWar_InitWarRoom, new InitWarRoomHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_TheWar_UpdateRemainPetHp, new UpdateRemainPetHpHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_TheWar_UpdateBossPetHp, new UpdateBossPetHpHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_TheWar_RemovePetFromTeam, new RemovePetFromTeamHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_TheWar_RemoveTroopsPetFromGrid, new RemoveTroopsPetFromGridHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_TheWar_AddUnclaimedReward, new AddUnclaimedRewardHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_TheWar_SettleBattleReward, new SettleBattleRewardHandler()) ? 1 : 0;

        return result == 1;
    }

    public static boolean listenEvent(int eventId, EventHandler handler) {
        return EventManager.getInstance().listenEvent(eventId, handler);
    }
}

final class LoginEventHandler implements EventHandler {

    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof MistPlayer && event.checkParamSize(2)) {
                MistPlayer player = (MistPlayer) obj;
                boolean isResume = event.getParam(0);
                return true;
            }
            return false;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class LogoutEventHandler implements EventHandler {

    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof MistPlayer && event.checkParamSize(1)) {
                MistPlayer player = (MistPlayer) obj;
                boolean sendToGS = event.getParam(0);
                player.onPlayerLogout(sendToGS);
                return true;
            }
            return false;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class OfflineEventHandler implements EventHandler {

    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof MistPlayer) {
                MistPlayer player = (MistPlayer) obj;
                player.offline();
                return true;
            }
            return false;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class ExchangeMistForestEventHandler implements EventHandler {

    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof MistPlayer && event.checkParamSize(1)) {
                MistPlayer player = (MistPlayer) obj;
                MistMapObjConfigObject newMapCfg = event.getParam(0);
                player.exchangeMistForest(newMapCfg);
                return true;
            }
            return false;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class EnterMistPveBattleEventHandler implements EventHandler {

    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof MistPlayer && event.getSource() instanceof MistRoom && event.checkParamSize(4)) {
                MistPlayer player = (MistPlayer) obj;
                MistRoom room = (MistRoom) event.getSource();
                int battleType = event.getParam(0);
                int fightMakeId = event.getParam(1);
                List<PetBuffData> extBuffList = event.getParam(2);
                int monsterCfgId = event.getParam(3);

                CS_GS_EnterMistPveBattle.Builder builder = CS_GS_EnterMistPveBattle.newBuilder();
                builder.setIdx(player.getIdx());
                builder.setMistLevel(room.getLevel());
                builder.setPveTypeValue(battleType);
                builder.setFightMakeId(fightMakeId);
                builder.addAllBuffData(extBuffList);
                builder.addPlayerInfo(player.buildPveBattleData());
                builder.setMonsterCfgId(monsterCfgId);
                GlobalData.getInstance().sendMsgToServer(player.getServerIndex(), MsgIdEnum.CS_GS_EnterMistPveBattle_VALUE, builder);
                return true;
            }
            return false;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class SetMistPlayerPetRemainHpEventHandler implements EventHandler {

    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof MistPlayer && event.checkParamSize(2)) {
                MistPlayer player = (MistPlayer) obj;
                int camp = event.getParam(0);
                List<BattleRemainPet> remainPets = event.getParam(1);
                player.setPetRemainHp(camp, remainPets);
                return true;
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
        return false;
    }
}

final class EnterMistPvpBattleEventHandler implements EventHandler {

    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof MistRoom && event.getSource() instanceof MistPlayer && event.checkParamSize(4)) {
                MistRoom room = (MistRoom) obj;
                MistPlayer player = (MistPlayer) event.getSource();
                boolean enterSuccess = event.getParam(0);
                MistFighter fighter = room.getObjManager().getMistObj(player.getFighterId());
                if (enterSuccess) {
                    long targetId = event.getParam(1);
                    long posObjId = event.getParam(2);
                    int battleSide = event.getParam(3);
                    MistFighter target = room.getObjManager().getMistObj(targetId);
                    if (fighter != null && target != null) {
                        ProtoVector pos = posObjId == fighter.getId() ? fighter.getPos().build() : target.getPos().build();
                        fighter.enterPvpBattle(target, pos, battleSide);
                    }
                } else {
                    fighter.setAttribute(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE, 0);
                    fighter.addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE, 0);
                }
                return true;
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
        return false;
    }
}

final class SettleMistPvpBattleEventHandler implements EventHandler {

    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof MistRoom && event.getSource() instanceof MistPlayer && event.checkParamSize(7)) {
                MistRoom room = (MistRoom) obj;
                MistPlayer player = (MistPlayer) event.getSource();
                int winnerCamp = event.getParam(0);
                int playerCamp = event.getParam(1);
                long targetFighterId = event.getParam(2);
                boolean robBossKey = event.getParam(3);
                boolean terminate = event.getParam(4);
                boolean beatWantedPlayer = event.getParam(5);
                long jewelryCount = event.getParam(6);
                long lavalBadgeCount = event.getParam(7);
                MistFighter fighter = room.getObjManager().getMistObj(player.getFighterId());
                if (fighter == null) {
                    return true;
                }
                if (winnerCamp < 0 && fighter.getAttribute(MistUnitPropTypeEnum.MUPT_OwningKeyState_VALUE) > 0) {
                    robBossKey = false;
                    winnerCamp = playerCamp;
                }
                if (winnerCamp != playerCamp) {
                    MistFighter targetFighter = room.getObjManager().getMistObj(targetFighterId);
                    MistPlayer targetPlayer = null;
                    if (targetFighter != null && winnerCamp > 0) {
                        targetPlayer = targetFighter.getOwnerPlayerInSameRoom();
                    }
//                    boolean isPkMode = fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerAttackMode_VALUE) == MistAttackModeEnum.EAME_Attack_VALUE;
//                    Event dropEvent = Event.valueOf(EventType.ET_CalcPlayerDropItem, GameUtil.getDefaultEventSource(), GameUtil.getDefaultEventSource());
//                    dropEvent.pushParam(isPkMode, player, targetPlayer);
//                    EventManager.getInstance().dispatchEvent(dropEvent);
                }
                fighter.onPvpBattleSettle(winnerCamp == playerCamp, targetFighterId, robBossKey, terminate, beatWantedPlayer, jewelryCount, lavalBadgeCount);
                return true;
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
        return false;
    }
}

final class RemoveMistPlayerEventHandler implements EventHandler {

    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof MistRoom && event.getSource() instanceof MistPlayer) {
                MistRoom room = (MistRoom) obj;
                MistPlayer player = (MistPlayer) event.getSource();
                room.removeMember(player);
                return true;
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
        return false;
    }
}

final class ConsumeLootPackRewardEventHandler implements EventHandler {

    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (event.getTarget() instanceof MistPlayer && event.checkParamSize(2)) {
                MistPlayer player = (MistPlayer) event.getTarget();
                int rewardId = event.getParam(0);
                int count = event.getParam(1);
                player.consumeGainReward(rewardId, count);
                return true;
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
        return false;
    }
}

final class MonsterBattleCarryRewardHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (event.getSource() instanceof MistRoom && event.checkParamSize(4)) {
                MistRoom mistRoom = (MistRoom) event.getSource();
                MistPlayer player = event.getParam(0);
                Map<Integer, Integer> rewardMap = event.getParam(1);
                int monsterFightCfgId = event.getParam(2);
                ProtoVector.Builder pos = event.getParam(3);

                MistFighter fighter = mistRoom.getObjManager().getMistObj(player.getFighterId());
                if (fighter == null) {
                    return true;
                }
                Map<Integer, Integer> teammateRewardMap = MistConst.buildMonsterBattleTeamReward(monsterFightCfgId);
                MistTeam team = mistRoom.getTeamById(fighter.getTeamId());
                if (team != null && team.getTeamMemberCount() > 1) {
                    for (Entry<Integer, Integer> entry : teammateRewardMap.entrySet()) {
                        rewardMap.merge(entry.getKey(), entry.getValue(), (oldVal, newVal) -> oldVal + newVal);
                    }
                }
                Map<Integer, Integer> realRewardMap = SyncExecuteFunction.executeFunction(player, entity->entity.addNewGainRewardMap(rewardMap));
                List<MistRewardObj> rewardObjData = SyncExecuteFunction.executeFunction(mistRoom,
                        room->room.buildMonsterBattleRewardObj(fighter, monsterFightCfgId, pos));
                fighter.sendExplodeDropReward(pos, realRewardMap, rewardObjData);
                if (team == null || team.getTeamMemberCount() <= 1) {
                    return true;
                }

                if (realRewardMap != null) {
                    for (Entry<Integer, Integer> entry : realRewardMap.entrySet()) {
                        if (entry.getKey() == MistConst.RefinedStoneItemId) {
                            int refinedStoneCount = entry.getValue() * CrossConstConfig.getById(GameConst.ConfigId).getMistbattleteamrewardrate() / 1000;
                            teammateRewardMap.merge(MistConst.RefinedStoneItemId, refinedStoneCount, (oldVal, newVal) -> oldVal + newVal);
                            break;
                        }
                    }
                }

                if (teammateRewardMap.isEmpty()) {
                    return true;
                }
                MistPlayer teammatePlayer;
                Map<Integer, Integer> realTeamRewardMap;
                for (MistFighter member : team.getAllMembers().values()) {
                    if (member.getId() == fighter.getId()) {
                        continue;
                    }
                    teammatePlayer = member.getOwnerPlayerInSameRoom();
                    if (teammatePlayer == null) {
                        continue;
                    }
                    realTeamRewardMap = SyncExecuteFunction.executeFunction(teammatePlayer, entity -> entity.addNewGainRewardMap(teammateRewardMap));
                    if (realTeamRewardMap == null || realTeamRewardMap.isEmpty()) {
                        continue;
                    }
                    int refinedStoneCount = 0; // 精炼石数量
                    int friendPointCount = 0; // 友情卡数量
                    for (Entry<Integer, Integer> entry : realTeamRewardMap.entrySet()) {
                        if (entry.getKey() == MistConst.RefinedStoneItemId) {
                            refinedStoneCount = entry.getValue();
                        } else if (entry.getKey() == MistConst.FriendPointItemId) {
                            friendPointCount = entry.getValue();
                        }
                    }
                    teammatePlayer.sendMsgToServer(MsgIdEnum.SC_BattleCmd_VALUE, mistRoom.buildMistTips(EnumMistTipsType.EMTT_TeamMonsterReward_VALUE,
                            fighter, member, monsterFightCfgId, refinedStoneCount, friendPointCount));
                }
                return true;
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
        return false;
    }
}

final class GainMistBagCountEventHandler implements EventHandler {

    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof MistPlayer && event.checkParamSize(2)) {
                MistPlayer player = (MistPlayer) obj;
                Map<Integer, Integer> rewardMap = event.getParam(0);
                boolean gainMistActivityBoss = event.getParam(1);

                player.addNewGainRewardMap(rewardMap);
                if (gainMistActivityBoss) {
                    player.setGainActivityBossBoxFlag(true);
                    CS_GS_GainBossActivityReward.Builder builder = CS_GS_GainBossActivityReward.newBuilder();
                    builder.setPlayerIdx(player.getIdx());
                    builder.setMistLevel(player.getMistRoom().getLevel());
                    GlobalData.getInstance().sendMsgToServer(player.getServerIndex(), MsgIdEnum.CS_GS_GainBossActivityReward_VALUE, builder);
                }
                return true;
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
        return false;
    }
}

final class CalcFighterRemainHpRateEventHandler implements EventHandler {

    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof MistRoom && event.getSource() instanceof MistPlayer) {
                MistRoom room = (MistRoom) obj;
                MistPlayer player = (MistPlayer) event.getSource();
                MistFighter fighter = room.getObjManager().getMistObj(player.getFighterId());
                if (fighter == null) {
                    return true;
                }
                long hpRate = player.calcTotalRemainHpRate();
                fighter.setAttribute(MistUnitPropTypeEnum.MUPT_UnitRemainHp_VALUE, hpRate);
                fighter.addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_UnitRemainHp_VALUE, hpRate);
                return true;
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
        return false;
    }
}

final class ChangePlayerHpRatEventHandler implements EventHandler {

    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof MistPlayer && event.checkParamSize(1)) {
                MistPlayer player = (MistPlayer) obj;
                int changeRate = event.getParam(0);
                player.changeCurrentHp(changeRate);
                return true;
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
        return false;
    }
}

final class ChangeRecoverHpIntervalRateEventHandler implements EventHandler {

    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof MistPlayer && event.checkParamSize(1)) {
                MistPlayer player = (MistPlayer) obj;
                int addRate = event.getParam(0);
                int newRate = player.getRecoverIntervalExtRate() + addRate;
                player.setRecoverIntervalExtRate(newRate);
                return true;
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
        return false;
    }
}

final class CalcPlayerDropItemEventHandler implements EventHandler {

    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (event.checkParamSize(2)) {
                boolean isPkMode = event.getParam(0);
                MistPlayer dropPlayer = event.getParam(1);
                MistPlayer gainPlayer = event.getParam(2);
                Map<Integer, Integer> dropItemMap = dropPlayer != null ? dropPlayer.dropGainReward(isPkMode) : null;
                if (gainPlayer != null && !CollectionUtils.isEmpty(dropItemMap)) {
                    gainPlayer.addNewGainRewardMap(dropItemMap);
                }
                return true;
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
        return false;
    }
}

final class UpdateEliteMonsterRewardTimesEventHandler implements EventHandler {

    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof MistPlayer && event.checkParamSize(1)) {
                MistPlayer player = (MistPlayer) obj;
                int newTimes = event.getParam(0);
                player.setEliteMonsterRewardTimes(newTimes);
                CS_GS_UpdateEliteMonsterRewardTimes.Builder builder = CS_GS_UpdateEliteMonsterRewardTimes.newBuilder();
                builder.setPlayerIdx(player.getIdx());
                builder.setNewRewardTimes(newTimes);
                GlobalData.getInstance().sendMsgToServer(player.getServerIndex(), MsgIdEnum.CS_GS_UpdateEliteMonsterRewardTimes_VALUE, builder);
                return true;
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
        return false;
    }
}

final class CreateGhostBusterRoomEventHandler implements EventHandler {

    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (event.checkParamSize(1)) {
                Map<String, MistGhostBusterSyncData> playerDataMap = event.getParam(0);
                MistWorldMapConfigObject mapCfg = MistWorldMapConfig.getInstance().getByRuleAndLevel(EnumMistRuleKind.EMRK_GhostBuster_VALUE, 1);
                if (mapCfg == null || mapCfg.getMaxplayercount() <= 0) {
                    LogUtil.error("CreateGhostBusterRoomEventHandler mapCfg is null");
                    return true;
                }
                String id = IdGenerator.getInstance().generateId();
                MistGhostBusterRoom mistRoom = MistRoomCache.getInstance().createMistRoom(EnumMistRuleKind.EMRK_GhostBuster_VALUE, id);
                if (mistRoom == null) {
                    LogUtil.error("CreateGhostBusterRoomEventHandler create failed");
                    return true;
                }
                mistRoom.init(mapCfg);
                MistPlayer mistPlayer;
                for (Entry<String, MistGhostBusterSyncData> entry : playerDataMap.entrySet()) {
                    mistPlayer = MistPlayerCache.getInstance().queryObject(entry.getKey());
                    if (mistPlayer != null) {
                        continue;
                    }
                    mistPlayer = MistPlayerCache.getInstance().createObject(entry.getKey());
                    if (mistPlayer == null) {
                        continue;
                    }
                    mistPlayer.initGhostBusterPlayer(entry.getValue());
                    MistFighter fighter = mistRoom.initMatchedPlayers(mistPlayer.buildMistPlayerInfo(), entry.getValue().getFromSvrIndex(), entry.getValue().getItemDataList());
                    if (fighter == null) {
                        continue;
                    }

                    mistPlayer.initByMistFighter(fighter);

                    MistPlayerCache.getInstance().manageObject(mistPlayer);
                }
                for (int i = mapCfg.getMaxplayercount() - playerDataMap.size(); i > 0 ; i--) {
                    String idx = IdGenerator.getInstance().generateId();
                    mistPlayer = MistPlayerCache.getInstance().createObject(idx);
                    if (mistPlayer == null) {
                        continue;
                    }
                    mistPlayer.initGhostBusterPlayer(null);
                    MistFighter fighter = mistRoom.initMatchedPlayers(mistPlayer.buildMistPlayerInfo(), 0, null);
                    if (fighter == null) {
                        continue;
                    }
                    mistPlayer.initByMistFighter(fighter);

                    MistPlayerCache.getInstance().manageObject(mistPlayer);
                }
                mistRoom.setRoomState(GhostBusterRoomState.GBRS_ReadyState_VALUE);
                mistRoom.setRoomStateUpdateTime(GlobalTick.getInstance().getCurrentTime() + CrossConstConfig.getById(GameConst.ConfigId).getGhostbustermaxloadingtime() * TimeUtil.MS_IN_A_S);
                MistRoomCache.getInstance().manageObject(mistRoom);

                mistRoom.broadcastEnterRoomInfo();

                SyncExecuteFunction.executeConsumer(mistRoom, room-> room.onPlayerEnterInitPos());
                return true;
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
        return false;
    }
}

final class AttackerGiveupEventHandler implements EventHandler {

    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
//            if (obj instanceof MineObj) {
//                MineObj mine = (MineObj) obj;
//                long curTime = GlobalTick.getInstance().getCurrentTime();
//                if (mine.getMinestate() == EnumMineState.EMS_Challenging_VALUE) {
//                    mine.addTotalPauseTime(curTime - mine.getLastpausetimestamp());
//                    mine.setLastpausetimestamp(0);
//                    mine.clearAttackerInfo();
//                    mine.changeMineState(curTime, EnumMineState.EMS_Producting_VALUE);
//                } else if (mine.getMinestate() == EnumMineState.EMS_UnderAttack_VALUE && !mine.isPvpflag()) {
//                    mine.settleMineBattle(false, curTime);
//                }
//                return true;
//            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
        return false;
    }
}

final class TimerInvokeHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (!event.checkParamSize(2)) {
                LogUtil.error("TimerInvokeHandler, param size is not null");
                return false;
            }

            int targetType = event.getParam(0);
            DB_TimerParam.Builder paramBuilder = event.getParam(1);

            if (targetType == TimerTargetType.TT_RESET_DAILY_DATA) {
                ArenaManager.getInstance().updateDailyData();

            } else if (targetType == TimerTargetType.TT_RESET_WEEK_DATA) {

            } else if (targetType == TimerTargetType.TT_SETTLE_ARENA_DAN) {
                ArenaManager.getInstance().setDanSettleList();

            } else if (targetType == TimerTargetType.TT_ARENA_DAN_SETTLE_ADVICE) {
                sendArenaDanSettleAdviceToAllServer();
            } else {
                LogUtil.error("unSupported timer target type , value = " + targetType);
            }
            return true;
        } catch (
                Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }

    private void sendArenaDanSettleAdviceToAllServer() {
        GlobalData.getInstance().sendMsgToAllServer(MsgIdEnum.CS_GS_ArenaDanSettle_VALUE, CS_GS_ArenaDanSettle.newBuilder());
    }
}

final class UpdateRankingHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (!event.checkParamSize(1)) {
                LogUtil.error("UpdateDanAndAllocationRoomHandler, param size is not enough");
                return false;
            }

            RankingUpdateRequest updateRequest = event.getParam(0);
            boolean updateResult = HttpRequestUtil.updateRanking(updateRequest);
            LogUtil.debug("UpdateRankingHandler, update ranking result:" + updateResult + ", detailsInfo:"
                    + JSONObject.toJSONString(updateRequest));
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class ClearRankingHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (!event.checkParamSize(2)) {
                LogUtil.error("ClearRankingHandler, param size is not enough");
                return false;
            }

            String rankingName = event.getParam(0);
            List<String> keys = event.getParam(1);

            boolean clearKeyResult = HttpRequestUtil.clearCrossRanking(rankingName, keys);
            LogUtil.debug("ClearRankingHandler, clear ranking key result:" + clearKeyResult + ", rankingName:" + rankingName
                    + ", keys:" + GameUtil.collectionToString(keys));

            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class QueryArenaRoomRankingHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (!event.checkParamSize(1)) {
                LogUtil.error("UpdateDanAndAllocationRoomHandler, param size is not enough");
                return false;
            }
            List<String> roomIdList = event.getParam(0);
            ArenaManager.getInstance().queryRoomRanking(roomIdList);
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class AddFootHoldGridHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof WarPlayer && event.getSource() instanceof WarMapGrid && event.checkParamSize(2)) {
                WarPlayer warPlayer = (WarPlayer) obj;
                WarMapGrid grid = (WarMapGrid) event.getSource();
                if (grid instanceof FootHoldGrid) {
                    String ownerName = event.getParam(0);
                    boolean hasTroops = event.getParam(1);
                    warPlayer.addOwnedGridPos(grid);
                    warPlayer.addOccupyGridTargetProp((FootHoldGrid) grid, ownerName, hasTroops);
                }
            }
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class AddUnsettledCurrencyHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof WarPlayer && event.getSource() instanceof WarMapGrid && event.checkParamSize(6)) {
                WarPlayer warPlayer = (WarPlayer) obj;
                WarMapGrid grid = (WarMapGrid) event.getSource();

                boolean removeOwnedGrid = event.getParam(0);
                int unclaimedGold = event.getParam(1);
                int unclaimedDp = event.getParam(2);
                int unclaimedHolyWater = event.getParam(3);
                int dropItemCfgId = event.getParam(4);
                long unsettledItemTime = event.getParam(5);
                if (removeOwnedGrid) {
                    warPlayer.removeOwnedGridPos(grid.getPos());
                }
                warPlayer.addUnclaimedGold(unclaimedGold);
                warPlayer.addUnclaimedDP(unclaimedDp);
                warPlayer.addUnclaimedHolyWater(unclaimedHolyWater);
                warPlayer.addUnsettledItem(dropItemCfgId, unsettledItemTime);
            }
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class ModifyPetTroopsDataHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof WarPlayer && event.getSource() instanceof WarMapGrid && event.checkParamSize(1)) {
                WarPlayer warPlayer = (WarPlayer) obj;
                WarMapGrid grid = (WarMapGrid) event.getSource();
                StationTroopsInfo troopsInfo = event.getParam(0);

                WarPetData petData = warPlayer.getPlayerData().getPlayerPetsMap().get(troopsInfo.getPetIdx());
                if (petData == null) {
                    return true;
                }
                WarPetData.Builder petBuilder = petData.toBuilder();
                petBuilder.setStationIndex(troopsInfo.getIndex()).setStationTroopsPos(grid.getPos());
                warPlayer.getPlayerData().putPlayerPets(troopsInfo.getPetIdx(), petBuilder.build());

                SC_UpdateWarPetData.Builder updateBuilder = SC_UpdateWarPetData.newBuilder();
                updateBuilder.addPetList(petBuilder);
                warPlayer.sendTransMsgToServer(MsgIdEnum.SC_UpdateWarPetData_VALUE, updateBuilder);

                if (!warPlayer.getPlayerData().getCurTroopsGridList().contains(grid.getPos())) {
                    warPlayer.addTroopsGridTargetProp((FootHoldGrid) grid);
                    warPlayer.getPlayerData().addCurTroopsGrid(grid.getPos());
                }
            }
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class RemovePetTroopsDataHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof WarPlayer && event.checkParamSize(1)) {
                WarPlayer warPlayer = (WarPlayer) obj;
                List<String> troopsPetList = event.getParam(0);

                SC_UpdateWarPetData.Builder updateBuilder = SC_UpdateWarPetData.newBuilder();
                List<Position> removePosList = new ArrayList<>();
                for (String petIdx : troopsPetList) {
                    WarPetData petData = warPlayer.getPlayerData().getPlayerPetsMap().get(petIdx);
                    if (petData == null) {
                        return true;
                    }
                    removePosList.add(petData.getStationTroopsPos());
                    WarPetData.Builder petBuilder = petData.toBuilder();
                    petBuilder.setStationIndex(-1).clearStationTroopsPos();
                    warPlayer.getPlayerData().putPlayerPets(petIdx, petBuilder.build());

                    updateBuilder.addPetList(petBuilder);
                }
                for (Position pos : removePosList) {
                    for (int i = 0; i < warPlayer.getPlayerData().getCurTroopsGridCount(); i++) {
                        if (warPlayer.getPlayerData().getCurTroopsGrid(i).equals(pos)) {
                            warPlayer.getPlayerData().removeCurTroopsGrid(i);
                            break;
                        }
                    }
                }
                warPlayer.sendTransMsgToServer(MsgIdEnum.SC_UpdateWarPetData_VALUE, updateBuilder);
            }
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class SettleAfkRewardHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof WarPlayer && event.checkParamSize(2)) {
                WarPlayer warPlayer = (WarPlayer) obj;
                List<WarReward> rewardList = event.getParam(0);
                Map<Integer, Long> itemRewardTimeMap = event.getParam(1);
                warPlayer.settleAfkReward(rewardList, itemRewardTimeMap);
            }
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class AddPlayerCacheHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof WarRoom && event.getSource() instanceof WarPlayer && event.checkParamSize(1)) {
                WarRoom warRoom = (WarRoom) obj;
                WarPlayer warPlayer = (WarPlayer) event.getSource();
                PlayerCacheData playerCache = event.getParam(0);
                warRoom.addPlayerCache(warPlayer.getIdx(), playerCache);
            }
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class AddGridCacheHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof WarRoom && event.getSource() instanceof WarMapGrid && event.checkParamSize(1)) {
                WarRoom warRoom = (WarRoom) obj;
                WarMapGrid warMapGrid = (WarMapGrid) event.getSource();
                GridCacheData gridCache = event.getParam(0);
                warRoom.addGridCache(warMapGrid.getPos(), gridCache);
            }
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class AddWarGridRecordHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (event.getSource() instanceof FootHoldGrid) {
                FootHoldGrid fhGridGrid = (FootHoldGrid) event.getSource();
                WarPlayer attacker = event.checkParamSize(1) ? event.getParam(0) : null;
                WarPlayer owner = event.checkParamSize(2) ? event.getParam(1) : null;


                if (attacker != null) {
                    long curTime = GlobalTick.getInstance().getCurrentTime();
                    SyncExecuteFunction.executeConsumer(attacker, p1 -> p1.addWarGridRecord(curTime, fhGridGrid.getPos(),
                            owner != null ? owner.getIdx() : "", owner != null ? owner.getCamp() : 0, true));
                    if (owner != null) {
                        SyncExecuteFunction.executeConsumer(owner, p2 -> p2.addWarGridRecord(curTime, fhGridGrid.getPos(), attacker.getIdx(), attacker.getCamp(), false));
                    }
                }
            }
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class RemoveDpResourceHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof WarPlayer && event.checkParamSize(3)) {
                WarPlayer warPlayer = (WarPlayer) obj;
                int removeDp = event.getParam(0);
                int exchangeGold = event.getParam(1);
                int exchangeHolyWater = event.getParam(2);

                int oldDp = warPlayer.getPlayerData().getWarDP();
                int newDp = Math.max(0, oldDp - removeDp);
                int oldGold = warPlayer.getPlayerData().getWarGold();
                int newGold = oldGold + exchangeGold;
                warPlayer.getPlayerData().setWarDP(newDp);
                warPlayer.getPlayerData().setWarGold(newGold);
                warPlayer.gainHolyWater(exchangeHolyWater);
                warPlayer.updatePlayerWarCurrency();

                CS_GS_TheWarCurrencyLog.Builder logBuilder = CS_GS_TheWarCurrencyLog.newBuilder();
                CurrencyLogData.Builder dpLogBuilder = CurrencyLogData.newBuilder();
                dpLogBuilder.setCurrencyType(TheWarResourceType.TWRT_WarDoorPoint).setConsume(true).setBeforeAmount(oldDp).setAmount(newDp).setReason("捐献开门资源扣除");
                logBuilder.addLogData(dpLogBuilder);

                CurrencyLogData.Builder goldLogBuilder = CurrencyLogData.newBuilder();
                goldLogBuilder.setCurrencyType(TheWarResourceType.TWRT_WarGold).setBeforeAmount(oldGold).setAmount(newGold).setReason("捐献开门资源奖励");
                logBuilder.addLogData(goldLogBuilder);
                logBuilder.setPlayerIdx(warPlayer.getIdx());
                GlobalData.getInstance().sendMsgToServer(warPlayer.getServerIndex(), MsgIdEnum.CS_GS_TheWarCurrencyLog_VALUE, logBuilder);
            }
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class AddPosGroupGridHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof WarRoom && event.getSource() instanceof WarPlayer && event.checkParamSize(1)) {
                WarRoom warRoom = (WarRoom) obj;
                WarPlayer warPlayer = (WarPlayer) event.getSource();
                WarMapGrid warGrid = event.getParam(0);
                warRoom.calcAddCampPos(warPlayer, warGrid.getPos());

                if (warGrid.getPropValue(TheWarCellPropertyEnum.TWCP_IsRefreshed_VALUE) > 0) {
                    long occupyTime = warGrid.getPropValue(TheWarCellPropertyEnum.TWCP_OccupyTime_VALUE);
                    if (occupyTime > 0) {
                        int cfgId = (int) warGrid.getPropValue(TheWarCellPropertyEnum.TWCP_MonsterRefreshCfgId_VALUE);
                        TheWarMonsterRefreshConfigObject cfg = TheWarMonsterRefreshConfig.getById(cfgId);
                        if (cfg != null) {
                            warRoom.addMonsterExpireInfo(warGrid.getPos(), occupyTime + cfg.getMaxproducttime() * TimeUtil.MS_IN_A_S);
                        }
                    }
                }
                int gridLevel = (int) warGrid.getPropValue(TheWarCellPropertyEnum.TWCP_Level_VALUE);
                if (gridLevel > warRoom.getRoomLevel()) {
                    warRoom.setRoomLevel(gridLevel);
                }
            }
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class RemovePosGroupGridHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof WarRoom && event.getSource() instanceof WarPlayer && event.checkParamSize(1)) {
                WarRoom warRoom = (WarRoom) obj;
                WarPlayer warPlayer = (WarPlayer) event.getSource();
                WarMapGrid warGrid = event.getParam(0);
                warRoom.calcRemoveCampPos(warPlayer, warGrid.getPos());
            }
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class AddTargetProgressHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof WarPlayer && event.checkParamSize(3)) {
                WarPlayer warPlayer = (WarPlayer) obj;
                TargetTypeEnum targetType = event.getParam(0);
                int addition = event.getParam(1);
                int progress = event.getParam(2);
                warPlayer.addTargetProgress(targetType, addition, progress);
            }
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class AddPortalGridProgressHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof PortalGrid && event.checkParamSize(1)) {
                PortalGrid portalGrid = (PortalGrid) obj;
                int addCount = event.getParam(0);
                portalGrid.addOpenProgress(addCount);
            }
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class AddRefreshMonsterGridHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (event.checkParamSize(2)) {
                int cfgId = event.getParam(0);
                List<WarMapGrid> warMapGrids = event.getParam(1);
                if (CollectionUtils.isEmpty(warMapGrids)) {
                    return true;
                }
                TheWarMonsterRefreshConfigObject cfg = TheWarMonsterRefreshConfig.getById(cfgId);
                if (cfg == null) {
                    return true;
                }
                for (WarMapGrid grid : warMapGrids) {
                    SyncExecuteFunction.executeConsumer(grid, entity -> {
                        entity.setPropValue(TheWarCellPropertyEnum.TWCP_WarGoldEfficacy_VALUE, cfg.getGorlrate());
                        entity.setPropValue(TheWarCellPropertyEnum.TWCP_DPEfficacy_VALUE, cfg.getDprate());

                        entity.setPropValue(TheWarCellPropertyEnum.TWCP_CurDpAfkTime_VALUE, 0);
                        entity.setPropValue(TheWarCellPropertyEnum.TWCP_MaxDpAfkTime_VALUE, cfg.getMaxproducttime() * TimeUtil.MS_IN_A_S);
                        entity.setPropValue(TheWarCellPropertyEnum.TWCP_IsRefreshed_VALUE, 1);
                        entity.setPropValue(TheWarCellPropertyEnum.TWCP_IsDoorPointMode_VALUE, 1);

                        int fightMakeId = WarConst.getFightMakeIdByCfgId(cfg.getFithmakeid());
                        entity.setPropValue(TheWarCellPropertyEnum.TWCP_FightMakeCfgId_VALUE, fightMakeId);
                        entity.broadcastPropData();
                    });
                }
            }
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class RemoveRefreshMonsterGridHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof FootHoldGrid && event.checkParamSize(2)) {
                FootHoldGrid fhGrid = (FootHoldGrid) obj;
                fhGrid.settleMonsterAfkReward();
            }
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class AddRefreshMonsterCountHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof WarRoom && event.checkParamSize(1)) {
                WarRoom warRoom = (WarRoom) obj;
                Map<Integer, List<Position>> monsterGrids = event.getParam(0);
                warRoom.addRefreshedMonsterInfo(monsterGrids);
            }
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class DecRefreshMonsterCountHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof WarRoom && event.checkParamSize(1)) {
                WarRoom warRoom = (WarRoom) obj;
                int refreshMonsterCfgId = event.getParam(0);
                warRoom.decreaseRefreshedMonsterCount(refreshMonsterCfgId);
            }
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class ChangePlayerStaminaHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof WarPlayer && event.checkParamSize(2)) {
                WarPlayer warPlayer = (WarPlayer) obj;
                boolean bAdd = event.getParam(0);
                int deltaStamina = event.getParam(1);
                if (bAdd) {
                    warPlayer.addPlayerStamina(deltaStamina);
                } else {
                    warPlayer.decPlayerStamina(deltaStamina);
                }
            }
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class ChangeGridPropertyHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof WarMapGrid && event.checkParamSize(2)) {
                WarMapGrid targetGrid = (WarMapGrid) obj;
                int propType = event.getParam(0);
                long propVal = event.getParam(1);
                targetGrid.setPropValue(propType, propVal);
                targetGrid.broadcastPropData();
            }
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class AddPlayerBattleStateHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof WarPlayer && event.getSource() instanceof FootHoldGrid) {
                WarPlayer warPlayer = (WarPlayer) obj;
                WarMapGrid ftGrid = (WarMapGrid) event.getSource();
                warPlayer.getPlayerData().getBattleDataBuilder().setBattlingTargetPos(ftGrid.getPos());
                warPlayer.getPlayerData().getBattleDataBuilder().setEnterFightTime(GlobalTick.getInstance().getCurrentTime());
            }
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class TakeUpRoomHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (event.checkParamSize(4)) {
                String roomIdx = event.getParam(0);
                byte[] roomCacheData = event.getParam(1);
                Map<byte[], byte[]> playerCacheData = event.getParam(2);
                Map<byte[], byte[]> gridCacheData = event.getParam(3);
                // 解析房间
                RoomCacheData roomData = RoomCacheData.parseFrom(roomCacheData);

                //解析格子并创建地图和格子
                WarMapData mapData = new WarMapData(roomIdx);
                mapData.initMapGrids(roomIdx, roomData.getMapName());
                for (Entry<byte[], byte[]> entry : gridCacheData.entrySet()) {
                    Position pos = Position.parseFrom(entry.getKey());
                    GridCacheData gridData = GridCacheData.parseFrom(entry.getValue());
                    WarMapGrid grid = mapData.getMapGridByPos(pos);
                    if (grid == null) {
                        continue;
                    }
                    grid.setRoomIdx(roomIdx);
                    grid.parseFromCacheData(gridData);
                    // 记录出生点
//                    mapData.addIfIsBornPos(grid);
                    mapData.addNewGrid(grid);
                }

                //解析并创建玩家
                Map<String, WarPlayer> playerMap = new HashMap<>();
                for (Entry<byte[], byte[]> entry : playerCacheData.entrySet()) {
                    String playerIdx = String.valueOf(entry.getKey());
                    PlayerCacheData playerData = PlayerCacheData.parseFrom(entry.getValue());

                    WarPlayer player = WarPlayerCache.getInstance().createObject(playerIdx);
                    player.initByCache(playerData, mapData);
                    playerMap.put(playerIdx, player);
                }

                // 创建房间并初始化
                WarRoom warRoom = WarRoomCache.getInstance().createObject(roomIdx);
                LogUtil.info("Rever create war room,idx=" + roomIdx);
                warRoom.revertByRoomCache(roomData);

                WarMapManager.getInstance().addMapData(mapData);
                WarPlayerCache.getInstance().manageObjectList(playerMap.values());
                WarRoomCache.getInstance().manageObject(warRoom);
            }
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class RoomSettleHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (event.checkParamSize(2)) {
                String roomIdx = event.getParam(0);
                Set<String> playerSet = event.getParam(1);
                WarPlayer warPlayer;
                for (String playerIdx : playerSet) {
                    warPlayer = WarPlayerCache.getInstance().queryObject(playerIdx);
                    if (warPlayer == null) {
                        continue;
                    }
                    if (warPlayer.isOnline()) {
                        warPlayer.sendTransMsgToServer(MsgIdEnum.SC_KickOutFromTheWar_VALUE, SC_KickOutFromTheWar.newBuilder());
                    }
                    SyncExecuteFunction.executeConsumer(warPlayer, entity -> entity.clear());
                }
                WarMapManager.getInstance().clearMapData(roomIdx);
            }
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class ClearFootHoldGridHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (event.checkParamSize(2)) {
                WarPlayer warPlayer = event.getParam(0);
                FootHoldGrid ftGrid = event.getParam(1);
                SyncExecuteFunction.executeConsumer(ftGrid, grid -> grid.clearOwnedPosBySelf(warPlayer));
            }
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class ClearStationPetGridHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof FootHoldGrid && event.getSource() instanceof WarPlayer) {
                FootHoldGrid ftGrid = (FootHoldGrid) obj;
                WarPlayer warPlayer = (WarPlayer) event.getSource();
                ftGrid.clearStationTroopsPet(warPlayer, false);
            }
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class InitWarRoomHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (event.checkParamSize(1)) {
                WarRoom warRoom = event.getParam(0);
//                warRoom.initMap();
            }
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class UpdateRemainPetHpHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof WarPlayer && event.checkParamSize(1)) {
                WarPlayer warPlayer = (WarPlayer) obj;
                List<BattleRemainPet> remainPets = event.getParam(0);
                if (CollectionUtils.isEmpty(remainPets)) {
                    return true;
                }

                WarPetData warPet;
                WarPetData.Builder warPetBuilder;
                SC_UpdateWarPetData.Builder builder = SC_UpdateWarPetData.newBuilder();
                int mineHpRate = TheWarConstConfig.getById(GameConst.ConfigId).getMinpetremainhprate();
                for (BattleRemainPet remainPet : remainPets) {
                    warPet = warPlayer.getWarPetData(remainPet.getPetId());
                    if (warPet == null) {
                        continue;
                    }
                    warPetBuilder = warPet.toBuilder();
                    for (int i = 0; i < warPet.getPropDict().getKeysCount(); i++) {
                        if (warPet.getPropDict().getKeys(i) == PetProperty.Current_Health_VALUE) {
                            warPetBuilder.getPropDictBuilder().setValues(i, Math.max(mineHpRate, remainPet.getRemainHpRate()));
                            warPet =  warPetBuilder.build();
                            builder.addPetList(warPet);
                            warPlayer.getPlayerData().putPlayerPets(warPet.getPetId(), warPet);
                            break;
                        }
                    }
                }
                warPlayer.sendTransMsgToServer(MsgIdEnum.SC_UpdateWarPetData_VALUE, builder);
            }
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class UpdateBossPetHpHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (event.checkParamSize(1)) {
                List<BossGrid> bossGridList = event.getParam(0);
                for (BossGrid bossGrid : bossGridList) {
                    SyncExecuteFunction.executeConsumer(bossGrid, boss -> boss.recoverPetHp());
                }
            }
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class RemovePetFromTeamHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof WarPlayer && event.checkParamSize(2)) {
                int teamType = event.getParam(0);
                String petIdx = event.getParam(1);

                WarPlayer warPlayer = (WarPlayer) obj;
                warPlayer.removePetFromTeam(teamType, petIdx);
            }
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class RemoveTroopsPetFromGridHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (event.getSource() instanceof WarPlayer && event.checkParamSize(1)) {
                Map<Position, Set<WarPetData>> gridDataMap = event.getParam(0);
                WarPlayer warPlayer = (WarPlayer) event.getSource();

                WarRoom warRoom = WarRoomCache.getInstance().queryObject(warPlayer.getRoomIdx());
                if (warRoom == null) {
                    return false;
                }
                WarMapData warMapData = WarMapManager.getInstance().getRoomMapData(warRoom.getIdx());
                if (warMapData == null) {
                    return false;
                }
                WarMapGrid warMapGrid;
                String ownerIdx;
                for (Entry<Position, Set<WarPetData>> entry : gridDataMap.entrySet()) {
                    warMapGrid = warMapData.getMapGridByPos(entry.getKey());
                    if (!(warMapGrid instanceof FootHoldGrid)) {
                        continue;
                    }
                    ownerIdx = GameUtil.longToString(warMapGrid.getPropValue(TheWarCellPropertyEnum.TWCP_OccupierPlayerId_VALUE), "");
                    if (!ownerIdx.equals(warPlayer.getIdx())) {
                        continue;
                    }
                    FootHoldGrid fhGrid = (FootHoldGrid) warMapGrid;
                    fhGrid.clearStationTroopsPet(warPlayer, false);
                }
            }
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class AddUnclaimedRewardHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof WarPlayer && event.checkParamSize(3)) {
                int unclaimedGold = event.getParam(0);
                int unclaimedDP = event.getParam(1);
                int unclaimedHolyWater = event.getParam(2);

                WarPlayer warPlayer = (WarPlayer) obj;
                warPlayer.addUnclaimedGold(unclaimedGold);
                warPlayer.addUnclaimedDP(unclaimedDP);
                warPlayer.addUnclaimedHolyWater(unclaimedHolyWater);
            }
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class SettleBattleRewardHandler implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof WarPlayer && event.getSource() instanceof FootHoldGrid && event.checkParamSize(4)) {
                boolean battleResult = event.getParam(0);
                int fightStar = event.getParam(1);
                String ownerName = event.getParam(2);
                boolean hasTroops = event.getParam(3);

                WarPlayer warPlayer = (WarPlayer) obj;
                FootHoldGrid fhGrid = (FootHoldGrid) event.getSource();

                warPlayer.getPlayerData().setLatestPos(fhGrid.getPos());
//                int battleRewardId = (int) fhGrid.getPropValue(TheWarCellPropertyEnum.TWCP_BattleRewardId_VALUE);
                warPlayer.gainBattleReward(fightStar);

                warPlayer.clearBattleState(false);

                TheWarConstConfigObject mapCfg = TheWarConstConfig.getById(GameConst.ConfigId);
                if (mapCfg != null) {
                    int deltaStamina = StringHelper.isNull(ownerName) ? mapCfg.getMosterFightRecoverEnergyByFightStar(fightStar) : mapCfg.getPlayerFightRecoverEnergyByFightStar(fightStar);
                    if (deltaStamina > 0) {
                        warPlayer.addPlayerStamina(deltaStamina);
                    }
                }

                warPlayer.addAttackGridTargetProp(fhGrid, ownerName, hasTroops, battleResult);
            }
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}
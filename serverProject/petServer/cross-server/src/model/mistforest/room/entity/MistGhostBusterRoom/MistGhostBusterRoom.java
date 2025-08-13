package model.mistforest.room.entity.MistGhostBusterRoom;

import cfg.CrossConstConfig;
import cfg.MistWorldMapConfigObject;
import com.google.protobuf.GeneratedMessageV3.Builder;
import common.GameConst;
import common.GameConst.EventType;
import common.GlobalData;
import common.GlobalTick;
import datatool.StringHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import model.mistforest.MistConst.MistBuffInterruptType;
import model.mistforest.MistConst.MistSkillTiming;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.room.entity.MistRoom;
import model.mistplayer.cache.MistPlayerCache;
import model.mistplayer.entity.MistPlayer;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.EnumMistTipsType;
import protocol.MistForest.GhostBusterRankData;
import protocol.MistForest.GhostBusterRoomState;
import protocol.MistForest.MistItemInfo;
import protocol.MistForest.MistPlayerInfo;
import protocol.MistForest.MistRetCode;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.MistUnitTypeEnum;
import protocol.MistForest.SC_GhostBusterRankData;
import protocol.MistForest.SC_GhostBusterRoomStateTime;
import protocol.ServerTransfer.CS_GS_GhostBusterRoomSettleData;
import protocol.ServerTransfer.CS_GS_MatchGhostBusterRoomInfo;
import protocol.ServerTransfer.CS_GS_MistForestRoomInfo;
import server.event.Event;
import server.event.EventManager;
import util.LogUtil;
import util.TimeUtil;

@Getter
@Setter
public class MistGhostBusterRoom extends MistRoom {
    protected int roomState;
    protected long roomStateUpdateTime;
    protected List<MistFighter> robotFighters;
    protected List<GhostBusterRankData.Builder> rankDataList;

    @Override
    public void init(MistWorldMapConfigObject mapCfg) {
        super.init(mapCfg);
        robotFighters = new ArrayList<>();
        rankDataList = new ArrayList<>();
    }

    protected void initPlayerRankData(MistPlayerInfo.Builder playerInfo) {
        GhostBusterRankData.Builder builder = GhostBusterRankData.newBuilder();
        builder.setPlayerInfo(playerInfo);
        builder.setRank(-1);
        rankDataList.add(builder);
    }

    public MistFighter initMatchedPlayers(MistPlayerInfo.Builder playerInfo, int fromSvrIndex, List<MistItemInfo> itemList) {
        MistFighter newFight = objManager.createObj(MistUnitTypeEnum.MUT_Player_VALUE);
        if (newFight == null) {
            return null;
        }
        int camp = addMember(playerInfo.getId());
        if (camp < -1) {
            return null;
        }
        newFight.getSkillMachine().initItemSkillList(itemList);
        newFight.afterInit(playerInfo.getId(), camp);
        addPlayerFromServer(fromSvrIndex);
        if (fromSvrIndex <= 0) {
            robotFighters.add(newFight);
            newFight.setAttribute(MistUnitPropTypeEnum.MUPT_IsRobotPlayer_VALUE, 1);
            newFight.initRobController();
        }
        newFight.setJoinMistRoomTime(GlobalTick.getInstance().getCurrentTime());
        // 初始化时为惩罚状态
        newFight.setAttribute(MistUnitPropTypeEnum.MUPT_ReadStateFlag_VALUE, 1);
        initPlayerRankData(playerInfo);
        return newFight;
    }

    public void broadcastEnterRoomInfo() {
        CS_GS_MatchGhostBusterRoomInfo.Builder builder = CS_GS_MatchGhostBusterRoomInfo.newBuilder();
        builder.getRoomInfoBuilder().setRoomId(getIdx());
        builder.getRoomInfoBuilder().setMapId(getMistMapId());
        builder.getRoomInfoBuilder().addAllInitMetaData(objGenerator.getInitMetaData());

        MistPlayer player;
        for (String memberIdx : memberList) {
            player = MistPlayerCache.getInstance().queryObject(memberIdx);
            if (player == null) {
                continue;
            }
            builder.getRoomInfoBuilder().addPlayerInfoList(player.buildMistPlayerInfo());
        }
        MistFighter fighter;
        for (String memberIdx : memberList) {
            player = MistPlayerCache.getInstance().queryObject(memberIdx);
            if (player == null || player.isRobot()) {
                continue;
            }
            fighter = objManager.getMistObj(player.getFighterId());
            if (fighter == null) {
                continue;
            }
            builder.getRoomInfoBuilder().clearMistForestItem();
            builder.getRoomInfoBuilder().clearInitMetaData();

            builder.setPlayerIdx(memberIdx);
            List<MistItemInfo> itemSkillList = fighter.getSkillMachine().getAllItemSkillInfo();
            if (!itemSkillList.isEmpty()) {
                builder.getRoomInfoBuilder().addAllMistForestItem(itemSkillList);
            }
            builder.getRoomInfoBuilder().addInitMetaData(fighter.getMetaData(fighter));
            GlobalData.getInstance().sendMsgToServer(player.getServerIndex(), MsgIdEnum.CS_GS_MatchGhostBusterRoomInfo_VALUE, builder);
        }
        updateGhostBustRoomState(null);
        LogUtil.info("BroadcastEnterRoomInfo roomId={}", getIdx());
    }

    public void onPlayerEnterInitPos() {
        MistPlayer player;
        MistFighter fighter;
        for (String memberIdx : memberList) {
            player = MistPlayerCache.getInstance().queryObject(memberIdx);
            if (player == null) {
                continue;
            }
            fighter = objManager.getMistObj(player.getFighterId());
            if (fighter == null) {
                continue;
            }
            getWorldMap().objFirstEnter(fighter);
        }
    }

    protected void onFightStart(long curTime) {
        setRoomState(GhostBusterRoomState.GBRS_FightingState_VALUE);
        roomStateUpdateTime = curTime + CrossConstConfig.getById(GameConst.ConfigId).getGhostbusterfighttime() * TimeUtil.MS_IN_A_S;
        updateGhostBustRoomState(null);
        impunityFighters();
    }

    protected void impunityFighters() {
        MistPlayer player;
        MistFighter fighter;
        for (String memberIdx : memberList) {
            player = MistPlayerCache.getInstance().queryObject(memberIdx);
            if (player == null) {
                continue;
            }
            fighter = objManager.getMistObj(player.getFighterId());
            if (fighter == null) {
                continue;
            }
            fighter.setAttribute(MistUnitPropTypeEnum.MUPT_ReadStateFlag_VALUE, 0);
            fighter.addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_ReadStateFlag_VALUE, 0);
        }
    }

    @Override
    public void updateRemoveRoomTime() {
        if (memberCount > 0) {
            removeRoomTime = 0;
        }
        long curTime = GlobalTick.getInstance().getCurrentTime();
        if (roomState == GhostBusterRoomState.GBRS_FightingState_VALUE && roomStateUpdateTime <= curTime) {
            removeRoomTime = curTime;
        }
    }

    public void updateGhostBustRoomState(MistPlayer player) {
        SC_GhostBusterRoomStateTime.Builder stateBuilder = SC_GhostBusterRoomStateTime.newBuilder();
        stateBuilder.setRoomStateValue(getRoomState());
        stateBuilder.setStateEndTime(getRoomStateUpdateTime());
        if (player == null) {
            broadcastMsgWithoutRobot(MsgIdEnum.SC_GhostBusterRoomStateTime_VALUE, stateBuilder, true);
        } else {
            player.sendMsgToServer(MsgIdEnum.SC_GhostBusterRoomStateTime_VALUE, stateBuilder);
        }
    }

    public void addFighterScore(MistFighter fighter, int score) {
        MistPlayer mistPlayer = fighter.getOwnerPlayerInSameRoom();
        if (mistPlayer == null) {
            return;
        }
        boolean flag = false;
        for (GhostBusterRankData.Builder rankData : rankDataList) {
            if (rankData.getPlayerInfo().getId().equals(mistPlayer.getIdx())) {
                rankData.setScore(rankData.getScore() + score);
                flag = true;
                break;
            }
        }
        GhostBusterRankData.Builder builder;
        if (!flag) {
            builder = GhostBusterRankData.newBuilder();
            builder.setPlayerInfo(mistPlayer.buildMistPlayerInfo());
            builder.setScore(score);
            rankDataList.add(builder);
        }
        rankDataList.sort((o1, o2) -> o2.getScore() - o1.getScore());
        for (int i = 0; i < rankDataList.size(); i++) {
            rankDataList.get(i).setRank(i+1);
        }
        updateRankData();
    }

    public void updateRankData() {
        SC_GhostBusterRankData.Builder builder = SC_GhostBusterRankData.newBuilder();
        for (GhostBusterRankData.Builder rankData : rankDataList) {
            builder.addRandData(rankData);
        }
        broadcastMsgWithoutRobot(MsgIdEnum.SC_GhostBusterRankData_VALUE, builder, true);
    }

    protected void settleRankData(long curTime) {
        CS_GS_GhostBusterRoomSettleData.Builder builder = CS_GS_GhostBusterRoomSettleData.newBuilder();
        builder.setSettleTime(curTime);
        for (GhostBusterRankData.Builder rankData : rankDataList) {
            builder.addRandData(rankData);
        }
        for (Map.Entry<Integer, Integer> entry : fromSvrCountInfo.entrySet()) {
            GlobalData.getInstance().sendMsgToServer(entry.getKey(), MsgIdEnum.CS_GS_GhostBusterRoomSettleData_VALUE, builder);
        }
    }

    public void settleGhostBusterRoom(long curTime) {
        settleRankData(curTime);
        MistPlayer player;
        for (String members : memberList) {
            player = MistPlayerCache.getInstance().queryObject(members);
            if (player == null || player.getMistRoom() == null || !getIdx().equals(player.getMistRoom().getIdx())) {
                return;
            }
            MistFighter fighter = objManager.getMistObj(player.getFighterId());
            if (fighter != null) {
                fighter.dead();
                exitTeam(player);
                fighter.getBufMachine().interruptBuffByType(MistBuffInterruptType.ExitMistRoom);
                fighter.getSkillMachine().triggerPassiveSkills(MistSkillTiming.ExitRoom, fighter, null);
            }
            Event event = Event.valueOf(EventType.ET_Logout, this, player);
            event.pushParam(false);
            EventManager.getInstance().dispatchEvent(event);

            removeMember(player);
            removePlayerFromServer(player.getServerIndex());
            LogUtil.info("GhostBuster settleRoom player[" + members + "] been force kicked from room:" + getIdx());
            setRoomState(GhostBusterRoomState.GBRS_CloseState_VALUE);
        }
    }

    @Override
    public boolean checkRoomNeedRemove(long curTime) {
        return roomState == GhostBusterRoomState.GBRS_CloseState_VALUE;
    }

    public void broadcastMsgWithoutRobot(int msgId, Builder<?> builder, boolean checkOnline) {
        if (memberList.isEmpty() || fromSvrCountInfo.isEmpty()) {
            return;
        }
        CS_GS_MistForestRoomInfo.Builder builder1 = CS_GS_MistForestRoomInfo.newBuilder();
        builder1.setMsgId(msgId);
        builder1.setMsgData(builder.build().toByteString());
        for (String memberIdx : memberList) {
            MistPlayer player = MistPlayerCache.getInstance().queryObject(memberIdx);
            if (player == null || player.isRobot()) {
                continue;
            }
            if (checkOnline && !player.isOnline()) {
                continue;
            }
            builder1.addPlayerId(memberIdx);
        }
        for (Map.Entry<Integer, Integer> entry : fromSvrCountInfo.entrySet()) {
            GlobalData.getInstance().sendMsgToServer(entry.getKey(), MsgIdEnum.CS_GS_MistForestRoomInfo_VALUE, builder1);
        }
    }

    @Override
    public MistRetCode onPlayerExit(MistPlayer player, boolean applyLeave) {
        MistRetCode retCode = super.onPlayerExit(player, applyLeave);
        if (retCode == MistRetCode.MRC_Success) {
            for (GhostBusterRankData.Builder rankData : rankDataList) {
                if (rankData.getPlayerInfo().getId().equals(player.getIdx())) {
                    rankData.setExited(true);
                    rankData.setRank(-1);
                    rankData.setScore(0);
                    updateRankData();
                    break;
                }
            }

            MistFighter fighter = objManager.getMistObj(player.getFighterId());
            if (fighter != null) {
                broadcastMsgWithoutRobot(MsgIdEnum.SC_BattleCmd_VALUE,
                        buildMistTips(EnumMistTipsType.EMTT_PlayerExitGhostRoom_VALUE, fighter, fighter), true);
            }
        }
        return retCode;
    }

    @Override
    public void onTick(long curTime) {
        super.onTick(curTime);
        switch (roomState) {
            case GhostBusterRoomState.GBRS_ReadyState_VALUE: {
                boolean needBegin = false;
                if (roomStateUpdateTime < curTime) {
                    needBegin = true;
                } else {
                    // 所有玩家进入即可开始，咱屏蔽，后续策划大概率要改成这种
//                    boolean allReady = true;
//                    MistPlayer player;
//                    for (String playerIdx : memberList) {
//                        player = MistPlayerCache.getInstance().queryObject(playerIdx);
//                        if (player != null && !player.isRobot() && !player.isReadyState()) {
//                            allReady = false;
//                            break;
//                        }
//                    }
//                    if (allReady) {
//                        needBegin = true;
//                    }
                }
                if (needBegin) {
                    onFightStart(curTime);
                }
                break;
            }
            case GhostBusterRoomState.GBRS_FightingState_VALUE:{
                for (MistFighter robot : robotFighters) {
                    robot.getRobController().onTick(curTime);
                }
                if (roomStateUpdateTime < curTime) {
                    settleGhostBusterRoom(curTime);
                }
                break;
            }
            default:
                break;
        }
    }
}

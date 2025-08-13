package model.mistforest.room.entity;


import cfg.GameConfig;
import cfg.GameConfigObject;
import cfg.MistBattleConfig;
import cfg.MistExplodeConfig;
import cfg.MistExplodeConfigObject;
import cfg.MistJewelryConfig;
import cfg.MistJewelryConfigObject;
import cfg.MistMonsterFightConfig;
import cfg.MistMonsterFightConfigObject;
import cfg.MistSkillConfig;
import cfg.MistTimeLimitActivity;
import cfg.MistTimeLimitActivityObject;
import cfg.MistWorldMapConfigObject;
import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3.Builder;
import common.GameConst;
import common.GameConst.EventType;
import common.GlobalData;
import common.GlobalTick;
import datatool.StringHelper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import model.mistforest.MistConst;
import model.mistforest.MistConst.MistBattleSide;
import model.mistforest.MistConst.MistBossKeyState;
import model.mistforest.MistConst.MistBuffInterruptType;
import model.mistforest.MistConst.MistSkillTiming;
import model.mistforest.MistConst.MistTriggerParamType;
import model.mistforest.buff.Buff;
import model.mistforest.map.WorldMap;
import model.mistforest.mistobj.MistBusinessMan;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.mistobj.MistKey;
import model.mistforest.mistobj.MistMagicCycle;
import model.mistforest.mistobj.MistOasis;
import model.mistforest.mistobj.MistObjGenerator;
import model.mistforest.mistobj.MistObjManager;
import model.mistforest.mistobj.MistObject;
import model.mistforest.mistobj.MistSealColumn;
import model.mistforest.mistobj.activityboss.MistBossSlime;
import model.mistforest.mistobj.activityboss.MistSlimeMonster;
import model.mistforest.mistobj.gridobj.MistFateDoor;
import model.mistforest.mistobj.gridobj.MistMazeDoor;
import model.mistforest.mistobj.rewardobj.MistOptionalBox;
import model.mistforest.mistobj.rewardobj.MistRewardObj;
import model.mistforest.schedule.ScheduleManager;
import model.mistforest.team.MistTeam;
import model.mistplayer.cache.MistPlayerCache;
import model.mistplayer.entity.MistPlayer;
import model.obj.BaseObj;
import model.warpServer.BaseNettyClient;
import model.warpServer.battleServer.BattleServerManager;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.math.RandomUtils;
import protocol.Battle.BattleSubTypeEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.BattleCMD_BroacastTips;
import protocol.MistForest.BattleCMD_Emoji;
import protocol.MistForest.BattleCMD_ShowBossTime;
import protocol.MistForest.BattleCmdData;
import protocol.MistForest.CS_ClientEventInvoke;
import protocol.MistForest.ClientEventEnum;
import protocol.MistForest.EnumMistRuleKind;
import protocol.MistForest.EnumMistTipsType;
import protocol.MistForest.Event_AbsorbTreasureBag;
import protocol.MistForest.Event_CastItemSkill;
import protocol.MistForest.Event_ChangeAttackMode;
import protocol.MistForest.Event_ChangeMagicLightState;
import protocol.MistForest.Event_ChooseAreaRewardIndex;
import protocol.MistForest.Event_ChooseBusinessManResult;
import protocol.MistForest.Event_ClaimBusinessManReward;
import protocol.MistForest.Event_ClearOpeningAreaBoxId;
import protocol.MistForest.Event_ClickOasis;
import protocol.MistForest.Event_OpenFateDoor;
import protocol.MistForest.Event_OpenMazeDoor;
import protocol.MistForest.Event_ResponseCalling;
import protocol.MistForest.Event_SendSnapShot;
import protocol.MistForest.Event_StartBusinessManGame;
import protocol.MistForest.Event_SubmitJewellery;
import protocol.MistForest.Event_TouchTarget;
import protocol.MistForest.MistAttackModeEnum;
import protocol.MistForest.MistBattleCmdEnum;
import protocol.MistForest.MistBossDmgRankData;
import protocol.MistForest.MistBriefTeamInfo;
import protocol.MistForest.MistForestRoomInfo;
import protocol.MistForest.MistItemInfo;
import protocol.MistForest.MistRetCode;
import protocol.MistForest.MistShowData;
import protocol.MistForest.MistTeamInfo;
import protocol.MistForest.MistTipsParma;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.MistUnitTypeEnum;
import protocol.MistForest.MistVipSkillData;
import protocol.MistForest.ProtoVector;
import protocol.MistForest.SC_BattleCmd;
import protocol.MistForest.SC_ExchangeMistForest;
import protocol.MistForest.SC_InviteJoinMistTeamRet;
import protocol.MistForest.SC_MistEnterPlayerInfo;
import protocol.MistForest.SC_MistExitPlayerInfo;
import protocol.MistForest.SC_MistForestPlayerInfo;
import protocol.MistForest.SC_MistForestTeamInfo;
import protocol.MistForest.SC_UpdateMistBossDmgRankData;
import protocol.MistForest.UnitMetadata;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer.ApplyPvpBattleData;
import protocol.ServerTransfer.CS_BS_ApplyPvpBattle;
import protocol.ServerTransfer.CS_GS_JoinMistTeamLog;
import protocol.ServerTransfer.CS_GS_MistForestRoomInfo;
import protocol.ServerTransfer.CS_GS_MistRoomEnterInfo;
import protocol.ServerTransfer.CS_GS_ReqChangeMistStamina;
import protocol.ServerTransfer.CS_GS_UpdateActivityBossDmgRank;
import protocol.ServerTransfer.EnumJoinMistForestType;
import protocol.ServerTransfer.EnumMistPveBattleType;
import protocol.ServerTransfer.GS_CS_JoinMistForest;
import protocol.ServerTransfer.MistBossDmgRankInfo;
import protocol.TransServerCommon.MistActivityBossDmgRankData;
import server.event.Event;
import server.event.EventManager;
import util.LogUtil;
import util.TimeUtil;

public class MistRoom extends BaseObj {
    protected String idx;
    protected int mistMapId;
    protected int mistRule;
    protected int level;
    protected int maxPlayerCount;

    protected long createTime;
    protected WorldMap worldMap;
    protected List<String> memberList;
	protected Map<Integer, MistTeam> teamMap;
    protected MistObjManager objManager;
    protected MistObjGenerator objGenerator;
    protected Map<Integer, Integer> fromSvrCountInfo;

    protected int memberCount;
    protected long removeRoomTime;

//    private PairValue.Builder pvpTimePair;
    protected boolean forcePvpState;

    protected MistObject mistKeyHolder;

    protected List<MistActivityBossDmgRankData.Builder> activityBossDmgRank;
    protected long curActivityBossId;

    protected ScheduleManager scheduleManager;

    public MistRoom() {
        this.worldMap = new WorldMap();
        this.teamMap = new HashMap<>();
        this.objManager = new MistObjManager(this);
        this.objGenerator = new MistObjGenerator(this);
        this.fromSvrCountInfo = new HashMap<>();
    }

    public void init(MistWorldMapConfigObject mapCfg) {
        if (mapCfg == null) {
            return;
        }
        this.mistMapId = mapCfg.getMapid();
        this.mistRule = mapCfg.getMaprule();
        this.level = mapCfg.getLevel();
        this.maxPlayerCount = mapCfg.getMaxplayercount();
        this.memberList = new ArrayList<>(mapCfg.getMaxplayercount());
        this.createTime = GlobalTick.getInstance().getCurrentTime();
        this.worldMap.init(mapCfg.getMaprule(), mapCfg.getLevel());
        this.objGenerator.init();
        this.activityBossDmgRank = new ArrayList<>();
        if (this.mistRule == EnumMistRuleKind.EMRK_Common_VALUE) {
            scheduleManager = new ScheduleManager(this);
        }
    }

    public void clear() {
        LogUtil.info("Mist room clear,id=" + getIdx() + ",rule=" + mistRule);
        createTime = 0;
        memberCount = 0;
        removeRoomTime = 0;
        memberList.clear();
        teamMap.clear();
        objManager.clear();
        objGenerator.clear();
        fromSvrCountInfo.clear();
        worldMap.clear();
//        pvpTimePair.clear();
        mistKeyHolder = null;
        mistMapId = 0;
        mistRule = 0;
        level = 0;
        maxPlayerCount = 0;
        if (scheduleManager != null) {
            scheduleManager.clear();
        }
    }

    @Override
    public String getBaseIdx() {
        return null;
    }

    @Override
    public String getIdx() {
        return idx;
    }

    @Override
    public void setIdx(String idx) {
        this.idx = idx;
    }

    @Override
    public String getClassType() {
        return "MistRoom";
    }

    @Override
    public void putToCache() {

    }

    @Override
    public void transformDBData() {

    }

    public long getCreateTime() {
        return createTime;
    }

    public int getMistMapId() {
        return mistMapId;
    }

    public void setMistMapId(int mistMapId) {
        this.mistMapId = mistMapId;
    }

    public int getMistRule() {
        return mistRule;
    }

    public void setMistRule(int mistRule) {
        this.mistRule = mistRule;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public List<String> getMemberList() {
        return memberList;
    }

    public void setMemberList(List<String> memberList) {
        this.memberList = memberList;
    }

    public MistObjManager getObjManager() {
        return objManager;
    }

    public MistObjGenerator getObjGenerator() {
        return objGenerator;
    }

    public WorldMap getWorldMap() {
        return worldMap;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public long getRemoveRoomTime() {
        return removeRoomTime;
    }

    public long getMaxBattleTime() {
        return getLevel() < 1000 ? MistConst.BattleTimeout : MistConst.NewbieBattleTimeout;
    }

    public int getMaxPlayerCount() {
        return maxPlayerCount;
    }

    public boolean isRoomFull() {
        return memberCount >= getMaxPlayerCount();
    }

    public boolean isForcePvpState() {
        return forcePvpState;
    }

    public void setForcePvpState(boolean forcePvpState) {
        this.forcePvpState = forcePvpState;
    }

    public ScheduleManager getScheduleManager() {
        return scheduleManager;
    }

    public void updateMistKeyHolder(MistObject mistKeyHolder, boolean broadcast) {
        this.mistKeyHolder = mistKeyHolder;
        if (!broadcast) {
            return;
        }
        int state = calcBossKeyState();
        if (state == MistBossKeyState.keyPicked) {
            broadcastMsg(MsgIdEnum.SC_BattleCmd_VALUE, buildShowBossTimeCmd(state), true);
        }
    }

    public Buff getMistBossKeyBuff() {
        if (mistKeyHolder == null) {
            return null;
        }
        return mistKeyHolder.getBufMachine().getBuff(MistConst.MistWaitBossBattleBuffId);
    }

    public int addMember(String playerIdx) {
        if (isRoomFull()) {
            return -1;
        }
        int index = getMemberIndex(playerIdx);
        if (index >= 0) {
            return index + 1;
        }
        String plyIdx;
        for (int i = 0; i < memberList.size(); ++i) {
            plyIdx = memberList.get(i);
            if (StringHelper.isNull(plyIdx) || plyIdx.equals(playerIdx)) {
                memberList.set(i, playerIdx);
                ++memberCount;
                updateRemoveRoomTime();
                return i + 1;
            }
        }
        memberList.add(playerIdx);
        ++memberCount;
        updateRemoveRoomTime();
        return memberList.size();
    }

    public void removeMember(MistPlayer player) {
        if (player == null) {
            return;
        }
        String idx;
        for (int i = 0; i < memberList.size(); ++i) {
            idx = memberList.get(i);
            if (idx.equals(player.getIdx())) {
                memberList.set(i, "");
                --memberCount;
                break;
            }
        }
        updateRemoveRoomTime();
//        removeActivityBossRank(player.getIdx());
    }

    public void updateRemoveRoomTime() {
        if (memberCount > 0) {
            removeRoomTime = 0;
        } else {
            if (getLevel() < 1000) {
                removeRoomTime = GlobalTick.getInstance().getCurrentTime() + MistConst.EmptyRoomTime;
            } else {
                removeRoomTime = GlobalTick.getInstance().getCurrentTime();
            }
        }
    }

    public MistTeam getTeamById(int id) {
        return teamMap.get(id);
    }

    public boolean createTeam(MistPlayer player) {
        if (!isPlayerValid(player)) {
            return false;
        }
        MistFighter fighter = objManager.getMistObj(player.getFighterId());
        if (fighter == null) {
            return false;
        }
        MistTeam team = getTeamById(fighter.getTeamId());
        if (team != null) {
            team.updateTeamInfo(player);
            return false;
        }
        team = getMistAvailableTeam();
        if (team == null) {
            return false;
        }
        if (team.addMembers(fighter, player.getFightPower())) {
            fighter.setTeamId(team.getTeamId());

            // 创建队伍 同步信息给其他玩家
            SC_MistForestPlayerInfo.Builder plyInfo = SC_MistForestPlayerInfo.newBuilder();
            plyInfo.addPlayerInfo(player.buildMistPlayerInfo());
            broadcastMsg(MsgIdEnum.SC_MistForestPlayerInfo_VALUE, plyInfo, true);

            CS_GS_JoinMistTeamLog.Builder builder = CS_GS_JoinMistTeamLog.newBuilder();
            builder.setPlayerIdx(player.getIdx());
            GlobalData.getInstance().sendMsgToServer(player.getServerIndex(), MsgIdEnum.CS_GS_JoinMistTeamLog_VALUE, builder);
        }
        return true;
    }

    public MistRetCode inviteJoinTeam(MistPlayer inviter, MistPlayer target) {
        if (inviter == null || target == null) {
            return MistRetCode.MRC_TargetNotFound; // 未找到目标
        }
        if (!target.isAcceptTeamInvite()) {
            return MistRetCode.MRC_TargetRefuseInvite; // 玩家拒绝邀请
        }
        MistFighter inviteFighter = getObjManager().getMistObj(inviter.getFighterId());
        if (inviteFighter == null) {
            return MistRetCode.MRC_NotInMistForest; // 当前未进入迷雾森林
        }
        MistFighter targetFighter = getObjManager().getMistObj(target.getFighterId());
        if (targetFighter == null) {
            return MistRetCode.MRC_TargetNotInRoom; // 目标未进入迷雾森林
        }
        if (getTeamById(targetFighter.getTeamId()) != null) {
            return MistRetCode.MRC_TargetInTeam;
        }
        MistTeam team = getTeamById(inviteFighter.getTeamId());
        if (team == null) {
            return MistRetCode.MRC_NotInTeam; // 当前无队伍
        }
        if (team.getLeaderId() != inviteFighter.getId()) {
            return MistRetCode.MRC_NotTeamLeader; // 当前不是队长
        }
        if (team.isTeamFull()) {
            return MistRetCode.MRC_TeamFull; // 当前队伍已满
        }
        SC_InviteJoinMistTeamRet.Builder builder = SC_InviteJoinMistTeamRet.newBuilder();
        builder.setInviterId(inviter.getIdx());
        builder.setTeamId(team.getTeamId());
        target.sendMsgToServer(MsgIdEnum.SC_InviteJoinMistTeamRet_VALUE, builder);
        return MistRetCode.MRC_Success;
    }

    public MistRetCode joinTeam(MistPlayer player, int teamId) {
        if (!isPlayerValid(player)) {
            return MistRetCode.MRC_TargetNotFound; // 目标玩家未找到
        }
        MistFighter fighter = objManager.getMistObj(player.getFighterId());
        if (fighter == null) {
            return MistRetCode.MRC_TargetNotInRoom; // 玩家已退出迷雾森林
        }
        MistTeam team = getTeamById(fighter.getTeamId());
        if (team != null) {
            return MistRetCode.MRC_TargetInTeam; // 玩家已有队伍
        }
        team = getTeamById(teamId);
        if (team.addMembers(fighter, player.getFightPower())) {
            fighter.setTeamId(team.getTeamId());

            // 加入队伍 同步信息给其他玩家
            SC_MistForestPlayerInfo.Builder plyInfo = SC_MistForestPlayerInfo.newBuilder();
            plyInfo.addPlayerInfo(player.buildMistPlayerInfo());
            broadcastMsg(MsgIdEnum.SC_MistForestPlayerInfo_VALUE, plyInfo, true);

            CS_GS_JoinMistTeamLog.Builder builder = CS_GS_JoinMistTeamLog.newBuilder();
            builder.setPlayerIdx(player.getIdx());
            GlobalData.getInstance().sendMsgToServer(player.getServerIndex(), MsgIdEnum.CS_GS_JoinMistTeamLog_VALUE, builder);
        }
        return MistRetCode.MRC_Success;
    }

    protected MistTeam getMistAvailableTeam() {
        int maxTeamCount = Integer.max(getMaxPlayerCount() / MistConst.MistRoomMaxTeamMemberSize, 1); // 至少1个队伍
        if (teamMap.size() >= maxTeamCount) {
            return null;
        }
        MistTeam team = null;
        for (int i = 1; i <= maxTeamCount; ++i) {
            team = teamMap.get(i);
            if (team == null) {
                team = new MistTeam(this);
                team.setTeamId(i);
                teamMap.put(team.getTeamId(), team);
                break;
            } else if (team.isTeamEmpty()) {
                break;
            }
        }
        return team;
    }

    public boolean exitTeam(MistPlayer player) {
        if (!isPlayerValid(player)) {
            return false;
        }
        MistFighter fighter = objManager.getMistObj(player.getFighterId());
        if (fighter == null) {
            return false;
        }
        MistTeam team = getTeamById(fighter.getTeamId());
        if (team == null) {
            return false;
        }
        team.removeMembers(fighter, player.getFightPower());
        fighter.setTeamId(0);

        // 退出队伍 同步信息给其他玩家
        SC_MistForestPlayerInfo.Builder plyInfo = SC_MistForestPlayerInfo.newBuilder();
        plyInfo.addPlayerInfo(player.buildMistPlayerInfo());
        broadcastMsg(MsgIdEnum.SC_MistForestPlayerInfo_VALUE, plyInfo, true);

        SC_MistForestTeamInfo.Builder builder = SC_MistForestTeamInfo.newBuilder();
        builder.setTeamInfo(MistTeamInfo.newBuilder());
        player.sendMsgToServer(MsgIdEnum.SC_MistForestTeamInfo_VALUE, builder);
        return true;
    }

    public boolean isPlayerValid(MistPlayer player) {
        if (player == null) {
            return false;
        }
        if (player.getMistRoom() == null) {
            return false;
        }
        if (!player.getMistRoom().getIdx().equals(this.getIdx())) {
            return false;
        }
        return true;
    }

    public int getMemberIndex(String idx) {
        if (StringHelper.isNull(idx)) {
            return -1;
        }
        int index = -1;
        String memberIdx;
        for (int i = 0; i < memberList.size(); ++i) {
            memberIdx = memberList.get(i);
            if (idx.equals(memberIdx)) {
                index = i;
                break;
            }
        }
        return index;
    }

    public boolean containMember(String idx) {
        return getMemberIndex(idx) >= 0;
    }

    public MistFighter onPlayerJoin(MistPlayer player, GS_CS_JoinMistForest req) {
        if (player == null) {
            return null;
        }
        int joinType = req.getJoinTypeValue();
        if (joinType <= 0 || joinType > EnumJoinMistForestType.EJFT_ExchangeJoin_VALUE) {
            return null;
        }
        try {
            MistFighter newFighter = objManager.getMistObj(player.getFighterId());
            if (newFighter == null) {
                newFighter = objManager.createObj(MistUnitTypeEnum.MUT_Player_VALUE);
                if (newFighter == null) {
                    return null;
                }
                int camp = addMember(player.getIdx());
                if (camp < -1) {
                    return null;
                }
                newFighter.setAttribute(MistUnitPropTypeEnum.MUPT_PermitLevel_VALUE, req.getPermitLevel());
                newFighter.setAttribute(MistUnitPropTypeEnum.MUPT_JewelryCount_VALUE, req.getOfflineJewelryCount());
                newFighter.setAttribute(MistUnitPropTypeEnum.MUPT_HiddenEvilId_VALUE, req.getHiddenEvilId());
                newFighter.setAttribute(MistUnitPropTypeEnum.MUPT_HiddenEvilExpireTime_VALUE, req.getHiddenEvilExpireTime());
                newFighter.addAttributes(req.getOffPropDataMap());
                newFighter.afterInit(player.getIdx(), camp);

                newFighter.addMoveEffectBuff(req.getMoveEffectId(), true);

                newFighter.initFighterSpeed(player.getCrossVipLv());
                newFighter.getSkillMachine().initItemSkillList(req.getItemDataList());
                newFighter.getSkillMachine().initOffVipSkillData(player.getCrossVipLv(), req.getVipSkillDataList());
                newFighter.getBufMachine().revertOfflineBuffList(req.getOfflineBuffsList());
                newFighter.initWantedPlayer();

                newFighter.initGoblin();
                newFighter.initNewbieTask(req.getNewbieTaskId());
            } else if (!req.getIsBattling()){
                newFighter.autoSettleBattle();
            }

            MistForestRoomInfo.Builder initData = getRoomInitData(newFighter, player);
            if (joinType == EnumJoinMistForestType.EJFT_InitJoin_VALUE) {
                CS_GS_MistRoomEnterInfo.Builder builder = CS_GS_MistRoomEnterInfo.newBuilder();
                builder.setPlayerIdx(player.getIdx());
                builder.setRoomInfo(initData);
                GlobalData.getInstance().sendMsgToServer(player.getServerIndex(), MsgIdEnum.CS_GS_MistRoomEnterInfo_VALUE, builder);
                sendBossActivityRankToPlayer(player);
            } else if (joinType == EnumJoinMistForestType.EJFT_ExchangeJoin_VALUE) {
                SC_ExchangeMistForest.Builder builder = SC_ExchangeMistForest.newBuilder();
                builder.setRoomInfo(initData);
                player.sendMsgToServer(MsgIdEnum.SC_ExchangeMistForest_VALUE, builder);

                sendBossActivityRankToPlayer(player);
            }
            addPlayerFromServer(player.getServerIndex());
            broadcastNewPlayer(player);
            if (getScheduleManager() != null) {
                getScheduleManager().updateAllScheduleData(player);
            }
            LogUtil.info("player[" + player.getIdx() + "] name=" + player.getName() + " join room:" + getIdx());
            return newFighter;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return null;
        }
    }

    public MistRetCode checkPlayerExit(MistPlayer player) {
        if (player == null) {
            return MistRetCode.MRC_TargetNotFound;
        }
        MistFighter fighter = objManager.getMistObj(player.getFighterId());
        if (fighter != null) {
            if (fighter.isBattling()) {
                return MistRetCode.MRC_Battling;
            }
            if (getMistRule() != EnumMistRuleKind.EMRK_Common_VALUE && !fighter.isInSafeRegion()) {
                return MistRetCode.MRC_NotInSafeRegion;
            }
        }
        return MistRetCode.MRC_Success;
    }

    public MistRetCode onPlayerApplyExit(MistPlayer player) {
        if (player == null) {
            return MistRetCode.MRC_TargetNotFound;
        }
        MistFighter fighter = objManager.getMistObj(player.getFighterId());
        if (fighter == null) {
            removeMember(player);
            broadcastExitPlayer(player);
            removePlayerFromServer(player.getServerIndex());
            LogUtil.error("player[{}] name:{} exit room:{},rule:{}, not found fighter", player.getIdx(), player.getName(), getIdx(), getMistRule());
            return MistRetCode.MRC_Success;
        }
        MistRetCode retCode = fighter.checkCanLeaveMistRoom();
        if (retCode == MistRetCode.MRC_Success) {

            fighter.getSkillMachine().triggerPassiveSkills(MistSkillTiming.ApplyExitRoom, fighter, null);
        }
        return MistRetCode.MRC_Success;
    }

    public MistRetCode onPlayerExit(MistPlayer player, boolean applyLeave) {
        if (player == null) {
            return MistRetCode.MRC_TargetNotFound;
        }
        MistFighter fighter = objManager.getMistObj(player.getFighterId());
        if (fighter != null) {
            if (applyLeave) {
                MistRetCode retCode = fighter.checkCanLeaveMistRoom();
                if (retCode != MistRetCode.MRC_Success) {
                    return retCode;
                }
            }

            fighter.dead();
            exitTeam(player);
            fighter.getBufMachine().interruptBuffByType(MistBuffInterruptType.ExitMistRoom);
            fighter.getSkillMachine().triggerPassiveSkills(MistSkillTiming.ExitRoom, fighter, null);
        }
        removeMember(player);
        broadcastExitPlayer(player);
        removePlayerFromServer(player.getServerIndex());
        LogUtil.info("player[" + player.getIdx() + "] exit room:" + getIdx() + ",rule=" + getMistRule());
        return MistRetCode.MRC_Success;
    }

    public void forceKickPlayer(String playerIdx) {
        MistPlayer player = MistPlayerCache.getInstance().queryObject(playerIdx);
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
        removeMember(player);
        broadcastExitPlayer(player);
        removePlayerFromServer(player.getServerIndex());
        LogUtil.info("player[" + playerIdx + "] been force kicked from room:" + getIdx());
    }

    public MistForestRoomInfo.Builder getRoomInitData(MistFighter fighter, MistPlayer owner) {
        MistForestRoomInfo.Builder builder = MistForestRoomInfo.newBuilder();
        builder.setRoomId(getIdx());
        builder.setMapId(getMistMapId());
        MistTeam team = getTeamById(fighter.getTeamId());
        if (team != null) {
            builder.setTeamInfo(team.buildTeamInfo(null));
            List<UnitMetadata> teamMetaData = team.getTeamMetaData(fighter.getId());
            if (teamMetaData != null) {
                builder.addAllInitMetaData(teamMetaData);
            }
        }
        List<MistItemInfo> itemSkillList = fighter.getSkillMachine().getAllItemSkillInfo();
        if (!itemSkillList.isEmpty()) {
            builder.addAllMistForestItem(itemSkillList);
        }
        builder.addInitMetaData(fighter.getMetaData(fighter));
        builder.addAllInitMetaData(objGenerator.getInitMetaData());
        builder.addAllInitMetaData(objGenerator.getOverallObjMetaData(fighter));

        List<UnitMetadata> selfObjList= fighter.getVisibleMetaData();
        if (selfObjList != null) {
            builder.addAllInitMetaData(selfObjList);
        }

        if (owner != null) {
            builder.addPlayerInfoList(owner.buildMistPlayerInfo());
            int dailyTimes = GameConfig.getById(GameConst.ConfigId).getDailyelitemonsterrewradtimes();
            int newRewardTimes = dailyTimes > owner.getEliteMonsterRewardTimes() ? dailyTimes - owner.getEliteMonsterRewardTimes() : 0;
            builder.setEliteMonsterRewardTimes(newRewardTimes);
        }
        MistPlayer player;
        for (String memberIdx : memberList) {
            player = MistPlayerCache.getInstance().queryObject(memberIdx);
            if (player == null || (owner != null && player.getIdx().equals(owner.getIdx()))) {
                continue;
            }
            builder.addPlayerInfoList(player.buildMistPlayerInfo());
        }
        for (MistTeam briefTeam : teamMap.values()) {
            MistBriefTeamInfo.Builder teamBuilder = MistBriefTeamInfo.newBuilder();
            teamBuilder.setTeamId(briefTeam.getTeamId());

            for (MistFighter teamMember : briefTeam.getAllMembers().values()) {
                player = teamMember.getOwnerPlayerInSameRoom();
                if (player == null) {
                    continue;
                }
                teamBuilder.addMemberIds(player.getIdx());
            }
            teamBuilder.setTeamFightPower(briefTeam.getTeamFightPower());
            teamBuilder.setLeaderName(briefTeam.getLeaderName());
            builder.addBriefTeamList(teamBuilder);
        }
        int bossState = calcBossKeyState();
        if (bossState == MistBossKeyState.keyNotBorn) {
            builder.setShowBossTime(objGenerator.getRemainCreateKeyTime());
        } else if (bossState == MistBossKeyState.keyPicked){
            Buff buff = getMistBossKeyBuff();
            if (buff != null) {
                long curTime = GlobalTick.getInstance().getCurrentTime();
                builder.setShowBossTime((int) buff.getBuffRemainTime(curTime));
                builder.setWaitBossBattleStatePause(buff.isBuffPaused(curTime));
            }
        }
        builder.setBossKeyState(bossState);

        builder.setBoxCountData(objManager.getMistBoxQualityCount());
        builder.addAllAlchemyData(owner.getAlchemyDataMap().values());

        if (owner != null && owner.getCrossVipLv() > 0) {
            List<MistVipSkillData> vipSkills = fighter.getSkillMachine().buildAllVipSkillData();
            if (vipSkills != null) {
                builder.addAllVipSkillData(vipSkills);
            }
        }

        if (fighter.getAttribute(MistUnitPropTypeEnum.MUPT_UsingShowObjState_VALUE) > 0) {
            List<MistShowData> objList = getObjManager().getAllShowObjs();
            if (objList != null) {
                builder.addAllShowData(objList);
            }
        }

        fighter.setJoinMistRoomTime(GlobalTick.getInstance().getCurrentTime());
        return builder;
    }

    protected int calcBossKeyState() {
        List<MistKey> keyObj = objManager.getMistObjList(MistUnitTypeEnum.MUT_Key_VALUE);
        if (keyObj.isEmpty()) {
            return MistBossKeyState.keyNotBorn; // boss钥匙未出现
        }
        MistKey bossKey = keyObj.get(0);
        if (!bossKey.isAlive()) {
            return MistBossKeyState.keyNotBorn; // boss钥匙未出现
        }
        if (!bossKey.isKeyHide()) {
            return MistBossKeyState.keyNotPicked; // boss钥匙已出现，未拾取
        }
        if (mistKeyHolder != null) {
            return MistBossKeyState.keyPicked; // boss钥匙被拾取
        } else {
            return MistBossKeyState.keyNotBorn; // boss钥匙未出现
        }
    }

    protected void addPlayerFromServer(int fromSeverIndex) {
        if (fromSeverIndex <= 0) {
            return;
        }
        Integer count = fromSvrCountInfo.get(fromSeverIndex);
        if (count == null || count <= 0) {
            count = 0;
        }
        fromSvrCountInfo.put(fromSeverIndex, ++count);
    }

    protected void removePlayerFromServer(int fromSeverIndex) {
        if (fromSeverIndex <= 0) {
            return;
        }
        Integer count = fromSvrCountInfo.get(fromSeverIndex);
        if (count != null && count > 1) {
            fromSvrCountInfo.put(fromSeverIndex, --count);
        } else {
            fromSvrCountInfo.remove(fromSeverIndex);
        }
    }

    public void broadcastMsg(int msgId, Builder<?> builder, boolean checkOnline) {
        if (memberList.isEmpty() || fromSvrCountInfo.isEmpty()) {
            return;
        }
        CS_GS_MistForestRoomInfo.Builder builder1 = CS_GS_MistForestRoomInfo.newBuilder();
        builder1.setMsgId(msgId);
        builder1.setMsgData(builder.build().toByteString());
        for (String memberIdx : memberList) {
            if (checkOnline) {
                MistPlayer player = MistPlayerCache.getInstance().queryObject(memberIdx);
                if (player == null || !player.isOnline()) {
                    continue;
                }
            }
            builder1.addPlayerId(memberIdx);
        }
        for (Map.Entry<Integer, Integer> entry : fromSvrCountInfo.entrySet()) {
            GlobalData.getInstance().sendMsgToServer(entry.getKey(), MsgIdEnum.CS_GS_MistForestRoomInfo_VALUE, builder1);
        }
    }

    public void broadcastMsgExcludePlayer(int msgId, Builder<?> builder, MistPlayer excludePlayer, boolean checkOnline) {
        if (memberList.isEmpty() || fromSvrCountInfo.isEmpty()) {
            return;
        }
        int excludeSvrIndex = 0;
        if (excludePlayer != null) {
            Integer count = fromSvrCountInfo.get(excludePlayer.getServerIndex());
            if (count != null && count <= 1) {
                excludeSvrIndex = excludePlayer.getServerIndex();
            }
        }
        CS_GS_MistForestRoomInfo.Builder builder1 = CS_GS_MistForestRoomInfo.newBuilder();
        builder1.setMsgId(msgId);
        builder1.setMsgData(builder.build().toByteString());
        for (String memberIdx : memberList) {
            if (StringHelper.isNull(memberIdx)) {
                continue;
            }
            if (excludePlayer != null && excludePlayer.getIdx().equals(memberIdx)) {
                continue;
            }
            if (checkOnline) {
                MistPlayer player = MistPlayerCache.getInstance().queryObject(memberIdx);
                if (player == null || !player.isOnline()) {
                    continue;
                }
            }
            builder1.addPlayerId(memberIdx);
        }

        for (Map.Entry<Integer, Integer> entry : fromSvrCountInfo.entrySet()) {
            if (excludeSvrIndex > 0 && excludeSvrIndex == entry.getKey()) {
                continue;
            }
            GlobalData.getInstance().sendMsgToServer(entry.getKey(), MsgIdEnum.CS_GS_MistForestRoomInfo_VALUE, builder1);
        }
    }

    public void broadcastNewPlayer(MistPlayer newPlayer) {
        if (newPlayer == null) {
            return;
        }
        SC_MistEnterPlayerInfo.Builder builder = SC_MistEnterPlayerInfo.newBuilder();
        builder.addPlayerInfo(newPlayer.buildMistPlayerInfo());
        broadcastMsgExcludePlayer(MsgIdEnum.SC_MistEnterPlayerInfo_VALUE, builder, newPlayer, true);
    }

    public void broadcastExitPlayer(MistPlayer exitPlayer) {
        if (exitPlayer == null) {
            return;
        }
        SC_MistExitPlayerInfo.Builder builder = SC_MistExitPlayerInfo.newBuilder();
        builder.addPlayerId(exitPlayer.getIdx());
        broadcastMsgExcludePlayer(MsgIdEnum.SC_MistExitPlayerInfo_VALUE, builder, exitPlayer,true);
    }

    public void broadcastToPartFighters(int msgId, Builder<?> builder, Collection<Long> fighters, boolean checkOnline) {
        if (CollectionUtils.isEmpty(fighters)) {
            return;
        }
        MistFighter fighter;
        MistPlayer player;
        CS_GS_MistForestRoomInfo.Builder builder1 = CS_GS_MistForestRoomInfo.newBuilder();
        builder1.setMsgId(msgId);
        builder1.setMsgData(builder.build().toByteString());
        for (Long fighterId : fighters) {
            fighter = getObjManager().getMistObj(fighterId);
            if (fighterId == null) {
                continue;
            }
            player = fighter.getOwnerPlayerInSameRoom();
            if (player == null) {
                continue;
            }
            if (checkOnline && !player.isOnline()) {
                continue;
            }
            builder1.addPlayerId(player.getIdx());
        }

        for (Map.Entry<Integer, Integer> entry : fromSvrCountInfo.entrySet()) {
            GlobalData.getInstance().sendMsgToServer(entry.getKey(), MsgIdEnum.CS_GS_MistForestRoomInfo_VALUE, builder1);
        }
    }

    public SC_BattleCmd.Builder buildShowBossTimeCmd(int bossState) {
        SC_BattleCmd.Builder builder = SC_BattleCmd.newBuilder();
        BattleCmdData.Builder cmdBuilder = BattleCmdData.newBuilder();
        cmdBuilder.setCMDType(MistBattleCmdEnum.MBC_ShowBossTime);
        BattleCMD_ShowBossTime.Builder showBossBuilder = BattleCMD_ShowBossTime.newBuilder();
        if (bossState == MistBossKeyState.keyNotBorn) {
            showBossBuilder.setShowBossTime(objGenerator.getRemainCreateKeyTime());
        } else if (bossState == MistBossKeyState.keyPicked) {
            long curTime = GlobalTick.getInstance().getCurrentTime();
            Buff buff = getMistBossKeyBuff();
            if (buff != null) {
                showBossBuilder.setShowBossTime((int) buff.getBuffRemainTime(curTime));
                showBossBuilder.setWaitBossBattleStatePause(buff.isBuffPaused(curTime));
            }
        }
        showBossBuilder.setBossKeyState(bossState);
        cmdBuilder.setCMDContent(showBossBuilder.build().toByteString());
        builder.addCMDList(cmdBuilder);
        return builder;
    }

    public SC_BattleCmd.Builder buildMistTips(int tipsType, MistObject user, MistObject target, Object... params) {
        SC_BattleCmd.Builder builder = SC_BattleCmd.newBuilder();
        try {
            BattleCmdData.Builder cmdBuilder = BattleCmdData.newBuilder();
            cmdBuilder.setCMDType(MistBattleCmdEnum.MBC_BroadcastTips);
            BattleCMD_BroacastTips.Builder tipsCmd = BattleCMD_BroacastTips.newBuilder();
            tipsCmd.setTipsTypeValue(tipsType);
            switch (tipsType) {
                case EnumMistTipsType.EMTT_ContinualKill_VALUE:
                    if (!(user instanceof MistFighter)) {
                        break;
                    }
                    MistFighter fighter = (MistFighter) user;

                    MistPlayer player = fighter.getOwnerPlayerInSameRoom();
                    String playerName = player != null ? player.getName() : "";
                    MistTipsParma.Builder param1 = MistTipsParma.newBuilder();
                    param1.setPramType(1); // 0为int32，1为string，2为int64，3为float
                    param1.setParamVal(playerName);
                    tipsCmd.addTipsParams(param1);

                    MistTipsParma.Builder param2 = MistTipsParma.newBuilder();
                    param2.setPramType(0);
                    param2.setParamVal(String.valueOf(fighter.getContinualKillCount()));
                    tipsCmd.addTipsParams(param2);
                    break;
                case EnumMistTipsType.EMTT_OpenBossKey_VALUE:
                    if (!(user instanceof MistFighter)) {
                        break;
                    }
                    fighter = (MistFighter) user;

                    player = fighter.getOwnerPlayerInSameRoom();
                    playerName = player != null ? player.getName() : "";
                    param1 = MistTipsParma.newBuilder();
                    param1.setPramType(1);
                    param1.setParamVal(playerName);
                    tipsCmd.addTipsParams(param1);

                    param2 = MistTipsParma.newBuilder();
                    param2.setPramType(0);
                    param2.setParamVal(String.valueOf(fighter.getPos().getX()));
                    tipsCmd.addTipsParams(param2);

                    MistTipsParma.Builder param3 = MistTipsParma.newBuilder();
                    param3.setPramType(0);
                    param3.setParamVal(String.valueOf(fighter.getPos().getY()));
                    tipsCmd.addTipsParams(param3);
                    break;
                case EnumMistTipsType.EMTT_KillPlayer_VALUE:
                case EnumMistTipsType.EMTT_KillPlayerWithKey_VALUE:
                case EnumMistTipsType.EMTT_TerminateKill_VALUE:
                    if (!(user instanceof MistFighter) || !(target instanceof MistFighter)) {
                        break;
                    }
                    MistFighter userFighter = (MistFighter) user;
                    MistFighter targetFighter = (MistFighter) target;
                    MistPlayer userPlayer = userFighter.getOwnerPlayerInSameRoom();
                    MistPlayer targetPlayer = targetFighter.getOwnerPlayerInSameRoom();
                    String userName = userPlayer != null ? userPlayer.getName() : "";
                    String targetName = targetPlayer != null ? targetPlayer.getName() : "";

                    param1 = MistTipsParma.newBuilder();
                    param1.setPramType(1);
                    param1.setParamVal(userName);
                    tipsCmd.addTipsParams(param1);

                    param2 = MistTipsParma.newBuilder();
                    param2.setPramType(1);
                    param2.setParamVal(targetName);
                    tipsCmd.addTipsParams(param2);
                    break;
                case EnumMistTipsType.EMTT_KillBossSuccess_VALUE:
                case EnumMistTipsType.EMTT_KillBossFailed_VALUE:
                case EnumMistTipsType.EMTT_TeammateKillBossSuccess_VALUE:
                case EnumMistTipsType.EMTT_PlayerExitGhostRoom_VALUE:
                    if (!(user instanceof MistFighter)) {
                        break;
                    }
                    fighter = (MistFighter) user;
                    player = fighter.getOwnerPlayerInSameRoom();
                    playerName = player != null ? player.getName() : "";
                    param1 = MistTipsParma.newBuilder();
                    param1.setPramType(1);
                    param1.setParamVal(playerName);
                    tipsCmd.addTipsParams(param1);
                    break;
                case EnumMistTipsType.EMTT_PickupBox_VALUE:
                case EnumMistTipsType.EMTT_ItemUsed_VALUE:
                case EnumMistTipsType.EMTT_TrapTriggered_VALUE:
                case EnumMistTipsType.EMTT_UnitTriggered_VALUE:
                case EnumMistTipsType.EMTT_CaughtByWolf_VALUE:
                case EnumMistTipsType.EMTT_HitByLavaLord_VALUE:
                    if (!(user instanceof MistFighter) || params.length < 1) {
                        break;
                    }
                    fighter = (MistFighter) user;
                    player = fighter.getOwnerPlayerInSameRoom();
                    playerName = player != null ? player.getName() : "";
                    param1 = MistTipsParma.newBuilder();
                    param1.setPramType(1);
                    param1.setParamVal(playerName);
                    tipsCmd.addTipsParams(param1);

                    param2 = MistTipsParma.newBuilder();
                    param2.setPramType(0);
                    param2.setParamVal(String.valueOf(params[0]));
                    tipsCmd.addTipsParams(param2);
                    break;
                case EnumMistTipsType.EMTT_TouchTrap_VALUE:
                    if (!(user instanceof MistFighter) || params.length < 1) {
                        break;
                    }
                    param1 = MistTipsParma.newBuilder();
                    param1.setPramType(0);
                    param1.setParamVal(String.valueOf(params[0]));
                    tipsCmd.addTipsParams(param1);
                    break;
                case EnumMistTipsType.EMTT_TeamMonsterReward_VALUE:
                case EnumMistTipsType.EMTT_LuckyBoxReward_VALUE:
                    if (!(user instanceof MistFighter) || params.length < 1) {
                        break;
                    }
                    fighter = (MistFighter) user;
                    player = fighter.getOwnerPlayerInSameRoom();
                    playerName = player != null ? player.getName() : "";
                    param1 = MistTipsParma.newBuilder();
                    param1.setPramType(1);
                    param1.setParamVal(playerName);
                    tipsCmd.addTipsParams(param1);

                    param2 = MistTipsParma.newBuilder();
                    param2.setPramType(0);
                    param2.setParamVal(String.valueOf(params[0]));
                    tipsCmd.addTipsParams(param2);

                    param3 = MistTipsParma.newBuilder();
                    param3.setPramType(0);
                    param3.setParamVal(String.valueOf(params[1]));
                    tipsCmd.addTipsParams(param3);

                    MistTipsParma.Builder param4 = MistTipsParma.newBuilder();
                    param4.setPramType(0);
                    param4.setParamVal(String.valueOf(params[2]));
                    tipsCmd.addTipsParams(param4);
                    break;
                case EnumMistTipsType.EMTT_PlayerTouchGhost_VALUE:
                    if (!(user instanceof MistFighter) || params.length < 1) {
                        break;
                    }
                    fighter = (MistFighter) user;
                    player = fighter.getOwnerPlayerInSameRoom();
                    playerName = player != null ? player.getName() : "";
                    param1 = MistTipsParma.newBuilder();
                    param1.setPramType(1);
                    param1.setParamVal(playerName);
                    tipsCmd.addTipsParams(param1);

                    param2 = MistTipsParma.newBuilder();
                    param2.setPramType(0);
                    param2.setParamVal(String.valueOf(params[0]));
                    tipsCmd.addTipsParams(param2);

                    param3 = MistTipsParma.newBuilder();
                    param3.setPramType(0);
                    param3.setParamVal(String.valueOf(params[1]));
                    tipsCmd.addTipsParams(param3);
					break;
                case EnumMistTipsType.EMTT_WantedPlayer_VALUE:
                    if (!(target instanceof MistFighter)) {
                        break;
                    }
                    fighter = (MistFighter) target;
                    player = fighter.getOwnerPlayerInSameRoom();
                    playerName = player != null ? player.getName() : "";
                    param1 = MistTipsParma.newBuilder();
                    param1.setPramType(1);
                    param1.setParamVal(playerName);
                    tipsCmd.addTipsParams(param1);

                    param2 = MistTipsParma.newBuilder();
                    param2.setPramType(0);
                    param2.setParamVal(String.valueOf(fighter.getPos().getX()));
                    tipsCmd.addTipsParams(param2);

                    param3 = MistTipsParma.newBuilder();
                    param3.setPramType(0);
                    param3.setParamVal(String.valueOf(fighter.getPos().getY()));
                    tipsCmd.addTipsParams(param3);
                    break;
                case EnumMistTipsType.EMTT_EliteMonsterAppear_VALUE:
                case EnumMistTipsType.EMTT_EliteMonsterDead_VALUE:
                case EnumMistTipsType.EMTT_EliteMonsterDisappear_VALUE: {
                    if (params.length < 1) {
                        break;
                    }
                    param1 = MistTipsParma.newBuilder();
                    param1.setPramType(0);
                    param1.setParamVal(String.valueOf(params[0]));
                    tipsCmd.addTipsParams(param1);
                    break;
                }
                default:
                    break;
            }
            cmdBuilder.setCMDContent(tipsCmd.build().toByteString());
            builder.addCMDList(cmdBuilder);
            return builder;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return builder;
        }
    }

    public void onPlayerEnterPvpBattle(MistFighter fighter1, MistFighter fighter2) {
        if (fighter1 == null || fighter2 == null) {
            return;
        }
        MistPlayer ply1 = fighter1.getOwnerPlayerInSameRoom();
        MistPlayer ply2 = fighter2.getOwnerPlayerInSameRoom();
        if (ply1 == null || ply2 == null) {
            return;
        }
        if (!fighter1.checkCanAttack(fighter2)) {
            return;
        }
//        int fightMakeId = MistBattleConfig.getFightMakeId(getLevel(), 0);
//        if (fightMakeId <= 0) {
//            return;
//        }
//        int serverIndex = BattleServerManager.getInstance().getAvailableBattleServer();
//        if (serverIndex <= 0) {
//            return;
//        }
//        BaseNettyClient nettyClient = BattleServerManager.getInstance().getActiveNettyClient(serverIndex);
//        if (nettyClient == null || nettyClient.getState() != 2) {
//            return;
//        }
        fighter1.setAttribute(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE, 1);
        fighter1.addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE, 1);

        fighter2.setAttribute(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE, 1);
        fighter2.addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE, 1);

        fighter1.enterPvpBattle(fighter2, fighter1.getPos().build(), MistBattleSide.leftSide);
        fighter2.enterPvpBattle(fighter1, fighter1.getPos().build(), MistBattleSide.rightSide);

//        BattleServerManager.getInstance().addPlayerBattleInfo(ply1.getIdx(), serverIndex);
//        BattleServerManager.getInstance().addPlayerBattleInfo(ply2.getIdx(), serverIndex);
//
//        CS_BS_ApplyPvpBattle.Builder builder = CS_BS_ApplyPvpBattle.newBuilder();
//        ApplyPvpBattleData.Builder applyBuilder = builder.getApplyPvpBattleDataBuilder();
//        applyBuilder.setFightMakeId(fightMakeId);
//        applyBuilder.setSubBattleType(BattleSubTypeEnum.BSTE_MistForest);
//        applyBuilder.addPlayerInfo(ply1.buildPvpBattleData(1));
//        applyBuilder.addPlayerInfo(ply2.buildPvpBattleData(2));
//        nettyClient.send(MsgIdEnum.CS_BS_ApplyPvpBattle_VALUE, builder);

//        boolean userWinState = fighter1.getAttribute(MistUnitPropTypeEnum.MUPT_WinState_VALUE) > 0;
//        boolean targetWinState = fighter2.getAttribute(MistUnitPropTypeEnum.MUPT_WinState_VALUE) > 0;
//        if (userWinState && !targetWinState) {
//            fighter1.setAttribute(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE, 1);
//            fighter1.addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE, 1);
//
//            fighter2.setAttribute(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE, 1);
//            fighter2.addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE, 1);
//
//            fighter1.enterPvpBattle(fighter2, fighter1.getPos().build(), 1);
//            fighter2.enterPvpBattle(fighter1, fighter1.getPos().build(), 2);
//
//            boolean robBossKey = fighter2.getAttribute(MistUnitPropTypeEnum.MUPT_OwningKeyState_VALUE) > 0;
//            boolean terminate = fighter2.getContinualKillCount() >= MistConst.ContinualKillCount;
//            boolean beatWantedPlayer = fighter2.getAttribute(MistUnitPropTypeEnum.MUPT_IsWantedState_VALUE) > 0;
//
//            fighter1.onPvpBattleSettle(true, robBossKey, terminate, beatWantedPlayer, 0);
//            Event event1 = Event.valueOf(EventType.ET_SetMistPlayerPetRemainHp, this, fighter1.getOwnerPlayerInSameRoom());
//            event1.pushParam(1, Collections.emptyList());
//            EventManager.getInstance().dispatchEvent(event1);
//
//            fighter2.onPvpBattleSettle(false,false, false, beatWantedPlayer, 0);
//            Event event2 = Event.valueOf(EventType.ET_SetMistPlayerPetRemainHp, this, fighter2.getOwnerPlayerInSameRoom());
//            event2.pushParam(0, Collections.emptyList());
//            EventManager.getInstance().dispatchEvent(event2);
//        } else if (!userWinState && targetWinState) {
//            fighter1.setAttribute(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE, 1);
//            fighter1.addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE, 1);
//
//            fighter2.setAttribute(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE, 1);
//            fighter2.addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE, 1);
//
//            fighter1.enterPvpBattle(fighter2, fighter1.getPos().build(), 1);
//            fighter2.enterPvpBattle(fighter1, fighter1.getPos().build(), 2);
//
//            boolean robBossKey = fighter1.getAttribute(MistUnitPropTypeEnum.MUPT_OwningKeyState_VALUE) > 0;
//            boolean terminate = fighter1.getContinualKillCount() >= MistConst.ContinualKillCount;
//            boolean beatWantedPlayer = fighter1.getAttribute(MistUnitPropTypeEnum.MUPT_IsWantedState_VALUE) > 0;
//
//            fighter2.onPvpBattleSettle(true, robBossKey, terminate, beatWantedPlayer, 0);
//            Event event1 = Event.valueOf(EventType.ET_SetMistPlayerPetRemainHp, this, fighter2.getOwnerPlayerInSameRoom());
//            event1.pushParam(1, Collections.emptyList());
//            EventManager.getInstance().dispatchEvent(event1);
//
//            fighter1.onPvpBattleSettle(false, false, false, beatWantedPlayer, 0);
//            Event event2 = Event.valueOf(EventType.ET_SetMistPlayerPetRemainHp, this, fighter1.getOwnerPlayerInSameRoom());
//            event2.pushParam(0, Collections.emptyList());
//            EventManager.getInstance().dispatchEvent(event2);
//        } else {
//            int fightMakeId = MistBattleConfig.getFightMakeId(getLevel(), 0);
//            if (fightMakeId <= 0) {
//                return;
//            }
//            BaseNettyClient nettyClient = BattleServerManager.getInstance().getAvailableBattleServer();
//            if (nettyClient == null || nettyClient.getState() != 2) {
//                return;
//            }
//            fighter1.setAttribute(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE, 1);
//            fighter1.addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE, 1);
//
//            fighter2.setAttribute(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE, 1);
//            fighter2.addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE, 1);
//
//            BattleServerManager.getInstance().addPlayerBattleInfo(ply1.getIdx(), nettyClient.getIpPort());
//            BattleServerManager.getInstance().addPlayerBattleInfo(ply2.getIdx(), nettyClient.getIpPort());
//
//            CS_BS_ApplyPvpBattle.Builder builder = CS_BS_ApplyPvpBattle.newBuilder();
//            ApplyPvpBattleData.Builder applyBuilder = builder.getApplyPvpBattleDataBuilder();
//            applyBuilder.setFightMakeId(fightMakeId);
//            applyBuilder.setSubBattleType(BattleSubTypeEnum.BSTE_MistForest);
//            applyBuilder.addPlayerInfo(ply1.buildPvpBattleData(1));
//            applyBuilder.addPlayerInfo(ply2.buildPvpBattleData(2));
//            nettyClient.send(MsgIdEnum.CS_BS_ApplyPvpBattle_VALUE, builder);
//        }
    }

    public void handleCommandMsg(long fighterId, ByteString msgData) {
        try {
            MistFighter fighter = objManager.getMistObj(fighterId);
            if (fighter == null) {
                return;
            }
            long startTime = System.currentTimeMillis();
            CS_ClientEventInvoke clientEvent = CS_ClientEventInvoke.parseFrom(msgData);
            switch (clientEvent.getEventTypeValue()) {
                case ClientEventEnum.CET_SendSnapShot_VALUE:
                    Event_SendSnapShot snapShot = Event_SendSnapShot.parseFrom(clientEvent.getEventData());
                    MistObject mistObj = objManager.getMistObj(snapShot.getSnapShot().getUnitId());
                    if (mistObj != null && mistObj.checkSnapShot(snapShot.getSnapShot())) {
                        mistObj.updateSnapShot(snapShot.getSnapShot());
                    }
                    break;
                case ClientEventEnum.CET_TouchTarget_VALUE:
                    Event_TouchTarget touchEvent = Event_TouchTarget.parseFrom(clientEvent.getEventData());
                    MistObject target = objManager.getMistObj(touchEvent.getTargetId());
                    if (fighter == null || target == null) {
                        break;
                    }
                    MistObject toucher = objManager.getMistObj(touchEvent.getToucherId());
                    if (toucher.getType() == MistUnitTypeEnum.MUT_Player_VALUE) {
                        fighter = (MistFighter) toucher;
                        fighter.touchObj(target);
                    } else if (toucher.getType() == MistUnitTypeEnum.MUT_SlimeMonster_VALUE) {
                        MistSlimeMonster monster = (MistSlimeMonster) toucher;
                        MistBossSlime bossSlime = monster.getRoom().getObjManager().getMistObj(monster.getAttribute(MistUnitPropTypeEnum.MUPT_MasterTargetId_VALUE));
                        if (bossSlime != null) {
                            monster.touchMaster(bossSlime);
                        } else {
                            monster.dead();
                        }
                    }
                    break;
                case ClientEventEnum.CET_CastItemSkill_VALUE:
                    Event_CastItemSkill castItemSkillEvent = Event_CastItemSkill.parseFrom(clientEvent.getEventData());
                    fighter = objManager.getMistObj(castItemSkillEvent.getCasterId());
                    if (fighter == null) {
                        break;
                    }
                    int index = castItemSkillEvent.getIndex();
                    if (fighter.isBattling() || !fighter.getSkillMachine().canUseItemSkill(index)) {
                        break;
                    }
                    HashMap<Integer, Long> params = new HashMap<>();
                    params.put(MistTriggerParamType.UseItemSkillIndex, (long) index);
                    fighter.getBufMachine().interruptBuffByType(MistBuffInterruptType.CastItemSkill);
                    fighter.getSkillMachine().triggerPassiveSkills(MistSkillTiming.CastItemSkill, fighter, params);
                    break;
                case ClientEventEnum.CET_SendEmoji_VALUE:
                    BattleCMD_Emoji emoj = BattleCMD_Emoji.parseFrom(clientEvent.getEventData());
                    fighter = objManager.getMistObj(emoj.getTargetId());
                    if (fighter == null) {
                        break;
                    }
                    fighter.updateEmoj(emoj);
                    break;
                case ClientEventEnum.CET_ChangeAttackMode_VALUE:
                    Event_ChangeAttackMode changeModeEvent = Event_ChangeAttackMode.parseFrom(clientEvent.getEventData());
                    fighter = objManager.getMistObj(changeModeEvent.getTargetId());
                    if (fighter == null || getWorldMap().isInSafeRegion(fighter.getPos().getX(), fighter.getPos().getY())) {
                        break;
                    }
                    if (getLevel() < GameConfig.getById(GameConst.ConfigId).getMistunlockpvplevel()) {
                        // 低等级不能任意攻击玩家
                        if (changeModeEvent.getNewMode() == MistAttackModeEnum.EAME_Attack) {
                            break;
                        }
                    } else if (isForcePvpState() || fighter.getAttribute(MistUnitPropTypeEnum.MUPT_IsWantedState_VALUE) > 0) {
                        break;
                    }

                    long curTime = GlobalTick.getInstance().getCurrentTime();
                    if (changeModeEvent.getNewMode() == MistAttackModeEnum.EAME_Attack) {
                        long expireTime = curTime + GameConfig.getById(GameConst.ConfigId).getMistpkchangeinterval() * TimeUtil.MS_IN_A_S;
                        fighter.setAttribute(MistUnitPropTypeEnum.MUPT_ChangeAttackModeExpire_VALUE, expireTime);
                        fighter.addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_ChangeAttackModeExpire_VALUE, expireTime);
                    } else {
                        int curMode = (int) fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerAttackMode_VALUE);
                        if (curMode == MistAttackModeEnum.EAME_Attack_VALUE) {
                            long changeAttackModeExpire = fighter.getAttribute(MistUnitPropTypeEnum.MUPT_ChangeAttackModeExpire_VALUE);
                            if (changeAttackModeExpire > curTime) {
//                                MistPlayer owner = fighter.getOwnerPlayerInSameRoom();
//                                Event event = Event.valueOf(EventType.ET_CalcPlayerDropItem, GameUtil.getDefaultEventSource(), GameUtil.getDefaultEventSource());
//                                event.pushParam(true, owner, null);
//                                EventManager.getInstance().dispatchEvent(event);
                            }
                        }
                    }

                    fighter.setAttribute(MistUnitPropTypeEnum.MUPT_PlayerAttackMode_VALUE, changeModeEvent.getNewModeValue());
                    fighter.addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_PlayerAttackMode_VALUE, changeModeEvent.getNewModeValue());
                    break;
                case ClientEventEnum.CET_BackToSafeRegion_VALUE:
                    if (fighter.isInSafeRegion() || fighter.getAttribute(MistUnitPropTypeEnum.MUPT_OwningKeyState_VALUE) > 0) {
                        break;
                    }
                    if (fighter.getAttribute(MistUnitPropTypeEnum.MUPT_IsWantedState_VALUE) > 0) {
                        break;
                    }
                    fighter.getBufMachine().interruptBuffByType(MistBuffInterruptType.BackToSafeRegion);
                    fighter.getSkillMachine().triggerPassiveSkills(MistSkillTiming.BackToSafeRegion, fighter, null);
                    break;
                case ClientEventEnum.CET_AbsorbTreasureBag_VALUE:
                    Event_AbsorbTreasureBag absorbEvent = Event_AbsorbTreasureBag.parseFrom(clientEvent.getEventData());
                    fighter = objManager.getMistObj(absorbEvent.getTakerId());
                    if (fighter == null) {
                        break;
                    }
                    fighter.absorbTreasureBagList(absorbEvent.getAbsorbBagIdList());
                    break;
                case ClientEventEnum.CET_ResponseCalling_VALUE:
                    Event_ResponseCalling responseEvent = Event_ResponseCalling.parseFrom(clientEvent.getEventData());
                    if (fighter == null || !fighter.isAlive() || fighter.isBattling()) {
                        break;
                    }
                    if (responseEvent.getAcceptCalling()) {
                        long invokerId = fighter.getAttribute(MistUnitPropTypeEnum.MUPT_TeamInvokerId_VALUE);
                        MistFighter caller = objManager.getMistObj(invokerId);
                        if (caller != null) {
                            boolean isCallerInSafeRegion = caller.isInSafeRegion();
                            if (isCallerInSafeRegion && fighter.getAttribute(MistUnitPropTypeEnum.MUPT_OwningKeyState_VALUE) > 0) {
                                fighter.sendTipsCmd(EnumMistTipsType.EMTT_BossKeyCannotBeCalledInSafeRegion_VALUE);
                            } else if (isCallerInSafeRegion && MistAttackModeEnum.EAME_Attack_VALUE == fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerAttackMode_VALUE)) {
                                fighter.sendTipsCmd(EnumMistTipsType.EMTT_PvpModeCannotBeCalledInSafeRegion_VALUE);
                            } else if (isCallerInSafeRegion && fighter.getAttribute(MistUnitPropTypeEnum.MUPT_IsWantedState_VALUE) > 0) {
                                fighter.sendTipsCmd(EnumMistTipsType.EMTT_WantedPlayerCannotBeCalled_VALUE);
                            } else {
                                int oldX = fighter.getPos().getX();
                                int oldY = fighter.getPos().getY();
                                fighter.setPos(caller.getPos().build());
                                getWorldMap().objMove(fighter, oldX, oldY);
                                fighter.addChangePosInfoCmd(fighter.getPos().build(), fighter.getToward().build());
                            }
                        } else {
                            fighter.sendTipsCmd(EnumMistTipsType.EMTT_CallerExitMist_VALUE);
                        }
                    }
                    fighter.getBufMachine().interruptBuffByType(MistBuffInterruptType.ResponseTeamInvoke);
                    break;
                case ClientEventEnum.CET_ClearOpeningAreaBoxId_VALUE: {
                    Event_ClearOpeningAreaBoxId clearOpeningAreaBoxEvent = Event_ClearOpeningAreaBoxId.parseFrom(clientEvent.getEventData());
                    if (fighter.getAttribute(MistUnitPropTypeEnum.MUPT_OpeningAreaBoxId_VALUE) != clearOpeningAreaBoxEvent.getTargetId()) {
                        break;
                    }
                    fighter.setAttribute(MistUnitPropTypeEnum.MUPT_OpeningAreaBoxId_VALUE, 0);
                    fighter.addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_OpeningAreaBoxId_VALUE, 0);
                    break;
                }
                case ClientEventEnum.CET_ChooseAreaRewardIndex_VALUE: {
                    Event_ChooseAreaRewardIndex chooseAreaRewardEvent = Event_ChooseAreaRewardIndex.parseFrom(clientEvent.getEventData());
                    MistOptionalBox optionalBox = objManager.getMistObj(chooseAreaRewardEvent.getTargetId());
                    if (optionalBox == null) {
                        break;
                    }
                    RetCodeEnum retCode = optionalBox.claimReward(fighter, chooseAreaRewardEvent.getChooseIndexList());
                    if (RetCodeEnum.RCE_Success != retCode) {
                        MistPlayer owner = fighter.getOwnerPlayerInSameRoom();
                        if (owner != null) {
                            owner.sendRetCodeMsg(retCode);
                        }
                    }
                    break;
                }
                case ClientEventEnum.CET_SummonHiddenEvil_VALUE: {
                    if (fighter.isBattling()) {
                        break;
                    }
                    MistPlayer player = fighter.getOwnerPlayerInSameRoom();
                    if (player == null) {
                        break;
                    }
                    if (fighter.isInSafeRegion()) {
                        break;
                    }
                    int cfgId = (int) fighter.getAttribute(MistUnitPropTypeEnum.MUPT_HiddenEvilId_VALUE);
                    MistJewelryConfigObject cfg = MistJewelryConfig.getById(cfgId);
                    if (cfg != null) {
                        long expireTime = fighter.getAttribute(MistUnitPropTypeEnum.MUPT_HiddenEvilExpireTime_VALUE);
                        if (expireTime > GlobalTick.getInstance().getCurrentTime()) {
                            player.sendRetCodeMsg(RetCodeEnum.RCE_MistForest_HiddenEvilSummoned);
                            break;
                        }
                    }
                    fighter.summonEvilMonster();
//                    fighter.getSkillMachine().triggerPassiveSkills(MistSkillTiming.SummonEvilBattle_VALUE, fighter, null);
                    break;
                }
                case ClientEventEnum.CET_EnterHiddenEvilBattle_VALUE: {
                    if (fighter.isBattling()) {
                        break;
                    }
                    MistPlayer player = fighter.getOwnerPlayerInSameRoom();
                    if (player == null) {
                        break;
                    }
                    int cfgId = (int) fighter.getAttribute(MistUnitPropTypeEnum.MUPT_HiddenEvilId_VALUE);
                    MistJewelryConfigObject cfg = MistJewelryConfig.getById(cfgId);
                    if (cfg == null) {
                        player.sendRetCodeMsg(RetCodeEnum.RCE_MistForest_HiddenEvilNotFound);
                        break;
                    }
                    long expireTime = fighter.getAttribute(MistUnitPropTypeEnum.MUPT_HiddenEvilExpireTime_VALUE);
                    if (GlobalTick.getInstance().getCurrentTime() >= expireTime) {
                        player.sendRetCodeMsg(RetCodeEnum.RCE_MistForest_HiddenEvilExpired);
                        break;
                    }
                    if (cfg.getNeedstamina() > player.getMistStamina()) {
                        player.sendRetCodeMsg(RetCodeEnum.RCE_Mist_StaminaNotEnough);
                        break;
                    }
                    fighter.enterPveBattle(EnumMistPveBattleType.EMPBT_SummonEvilBattle_VALUE, fighter);

                    CS_GS_ReqChangeMistStamina.Builder builder = CS_GS_ReqChangeMistStamina.newBuilder();
                    builder.setPlayerIdx(player.getIdx());
                    builder.setChangeValue(-cfg.getNeedstamina());
                    GlobalData.getInstance().sendMsgToServer(player.getServerIndex(), MsgIdEnum.CS_GS_ReqChangeMistStamina_VALUE, builder);
                    break;
                }
                case ClientEventEnum.CET_ClickOasis_VALUE: {
                    if (fighter.isBattling()) {
                        break;
                    }
                    Event_ClickOasis clickOasisEvent = Event_ClickOasis.parseFrom(clientEvent.getEventData());
                    MistOasis oasis = objManager.getMistObj(clickOasisEvent.getTargetId());
                    if (oasis == null) {
                        break;
                    }
                    if (!MistConst.checkInDistance((int) oasis.getAttribute(MistUnitPropTypeEnum.MUPT_MaxStaticTouchDis_VALUE), oasis.getPos(), fighter.getPos())){
                        break;
                    }
                    oasis.clickByFighter(fighter);
                    break;
                }
                case ClientEventEnum.CET_SubmitJewellery_VALUE: {
                    if (fighter.isBattling()) {
                        break;
                    }
                    MistPlayer player = fighter.getOwnerPlayerInSameRoom();
                    if (player == null) {
                        break;
                    }
                    Event_SubmitJewellery submitJewelleryEvent = Event_SubmitJewellery.parseFrom(clientEvent.getEventData());
                    MistSealColumn sealColumn = objManager.getMistObj(submitJewelleryEvent.getTargetId());
                    if (sealColumn == null) {
                        break;
                    }
                    if (!MistConst.checkInDistance((int) sealColumn.getAttribute(MistUnitPropTypeEnum.MUPT_MaxStaticTouchDis_VALUE), sealColumn.getPos(), fighter.getPos())){
                        break;
                    }
                    RetCodeEnum retCode;
                    if (player.checkRewardEnough(submitJewelleryEvent.getJewelleryRewardId(), 1)) {
                        retCode = sealColumn.submitJewelry(fighter, submitJewelleryEvent.getJewelleryRewardId());
                    } else {
                        retCode = RetCodeEnum.RCE_MistForest_NotEnoughJewelry;
                    }
                    player.sendRetCodeMsg(retCode);
                    break;
                }
                case ClientEventEnum.CET_StartBusinessManGame_VALUE: {
                    if (fighter.isBattling()) {
                        break;
                    }
                    Event_StartBusinessManGame startBusinessManGame = Event_StartBusinessManGame.parseFrom(clientEvent.getEventData());
                    MistBusinessMan businessMan = objManager.getMistObj(startBusinessManGame.getTargetId());
                    if (businessMan == null) {
                        break;
                    }
                    businessMan.startGamble(fighter);
                    break;
                }
                case ClientEventEnum.CET_ChooseBusinessManResult_VALUE: {
                    if (fighter.isBattling()) {
                        break;
                    }
                    Event_ChooseBusinessManResult chooseBusinessManResult = Event_ChooseBusinessManResult.parseFrom(clientEvent.getEventData());
                    MistBusinessMan businessMan = objManager.getMistObj(chooseBusinessManResult.getTargetId());
                    if (businessMan == null) {
                        break;
                    }
                    businessMan.getGambleResult(fighter, chooseBusinessManResult.getResult());
                    break;
                }
                case ClientEventEnum.CET_ClaimBusinessManReward_VALUE: {
                    if (fighter.isBattling()) {
                        break;
                    }
                    Event_ClaimBusinessManReward claimBusinessManReward = Event_ClaimBusinessManReward.parseFrom(clientEvent.getEventData());
                    MistBusinessMan businessMan = objManager.getMistObj(claimBusinessManReward.getTargetId());
                    if (businessMan == null) {
                        break;
                    }
                    businessMan.getGambleReward(fighter);
                    break;
                }
                case ClientEventEnum.CET_ChangeMagicLightState_VALUE: {
                    if (fighter.isBattling()) {
                        break;
                    }
                    Event_ChangeMagicLightState changeMagicLightState = Event_ChangeMagicLightState.parseFrom(clientEvent.getEventData());
                    MistMagicCycle magicCycle = objManager.getMistObj(changeMagicLightState.getTargetId());
                    if (magicCycle == null) {
                        break;
                    }
                    magicCycle.changeCycleFlag(fighter, changeMagicLightState.getChangeIndex());
                    break;
                }
                case ClientEventEnum.CET_OpenMazeDoor_VALUE: {
                    if (!fighter.canBeTouch()) {
                        break;
                    }
                    Event_OpenMazeDoor openMazeDoorEvent = Event_OpenMazeDoor.parseFrom(clientEvent.getEventData());
                    MistMazeDoor mazeDoor = objManager.getMistObj(openMazeDoorEvent.getTargetId());
                    if (mazeDoor == null) {
                        break;
                    }
                    mazeDoor.openMazeDoor(fighter);
                    break;
                }
                case ClientEventEnum.CET_OpenFateDoor_VALUE: {
                    if (!fighter.canBeTouch()) {
                        break;
                    }
                    Event_OpenFateDoor openFateDoorEvent = Event_OpenFateDoor.parseFrom(clientEvent.getEventData());
                    MistFateDoor fate = objManager.getMistObj(openFateDoorEvent.getTargetId());
                    if (fate == null) {
                        break;
                    }
                    fate.openFateDoor(fighter);
                    break;
                }
                default:
                    break;
            }
            long endTime = System.currentTimeMillis();
            long costTime = endTime - startTime;
            if (costTime > 100) {
                LogUtil.debug("Mist event["+clientEvent.getEventType()+"] cost too much time:"+ costTime);
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    public void provideMistItem(MistPlayer player, int itemType) {
        if (player == null) {
            return;
        }
        MistFighter fighter = getObjManager().getMistObj(player.getFighterId());
        if (fighter == null) {
            return;
        }
        int itemSkillId = MistSkillConfig.getSkillByItemType(itemType);
        int index = fighter.getSkillMachine().addItemSkill(itemSkillId);
        if (index >= 0) {
            fighter.sendUpdateItemSkillCmd(true);
        }
    }

    public void updateForcePvpState(long curTime) {
        GameConfigObject cfg = GameConfig.getById(GameConst.ConfigId);
        if (cfg == null || getLevel() < cfg.getMistunlockpvplevel()) {
            return;
        }
        // 取消强制pvp，暂时注释掉
//        if (isForcePvpState()) {
//            if (curTime < pvpTimePair.getLeft()) {
//                setForcePvpState(false);
//            } else if (curTime >= pvpTimePair.getRight()) {
//                setForcePvpState(MistConst.isPvpState(cfg.getMistforcepvpstarttime(), pvpTimePair));
//            }
//        } else {
//            if (curTime >= pvpTimePair.getLeft() && curTime < pvpTimePair.getRight()) {
//                setForcePvpState(true);
//            } else if (pvpTimePair.getRight() <= 0) {
//                setForcePvpState(MistConst.isPvpState(cfg.getMistforcepvpstarttime(), pvpTimePair));
//            }
//        }
    }

    public boolean checkRoomNeedRemove(long curTime) {
        return getRemoveRoomTime() > 0 && getRemoveRoomTime() < curTime;
    }

    public void sendBossActivityRankToPlayer(MistPlayer player) {
        if (CollectionUtils.isEmpty(activityBossDmgRank)) {
            return;
        }
        SC_UpdateMistBossDmgRankData.Builder builder = SC_UpdateMistBossDmgRankData.newBuilder();
        MistBossDmgRankData.Builder updateRankData = MistBossDmgRankData.newBuilder();
        for (int i = 0; i < activityBossDmgRank.size(); i++) {
            MistActivityBossDmgRankData.Builder rankInfo = activityBossDmgRank.get(i);
            updateRankData.setPlayerIdx(rankInfo.getPlayerId());
            updateRankData.setPlayerName(rankInfo.getPlayerName());
            updateRankData.setRank(i);
            updateRankData.setDamage(rankInfo.getDamage());
            builder.addRankData(updateRankData);
        }
        if (null != player) {
            player.sendMsgToServer(MsgIdEnum.SC_UpdateMistBossDmgRankData_VALUE, builder);
        } else {
            broadcastMsg(MsgIdEnum.SC_UpdateMistBossDmgRankData_VALUE, builder, true);
        }

    }

    public void updateBossActivityRank(MistPlayer player, long damage) {
        MistActivityBossDmgRankData.Builder rankDataBuilder = null;
        for (MistActivityBossDmgRankData.Builder rankData : activityBossDmgRank) {
            if (rankData.getPlayerId().equals(player.getIdx())) {
                rankDataBuilder = rankData;
                break;
            }
        }
        if (null == rankDataBuilder) {
            rankDataBuilder = MistActivityBossDmgRankData.newBuilder();
            rankDataBuilder.setPlayerId(player.getIdx());
            rankDataBuilder.setPlayerName(player.getName());
            rankDataBuilder.setFromSvrIndex(player.getServerIndex());
            activityBossDmgRank.add(rankDataBuilder);
        }
        rankDataBuilder.setDamage(rankDataBuilder.getDamage() + damage);
        activityBossDmgRank.sort((e1, e2) -> (int) (e2.getDamage() - e1.getDamage()));

        SC_UpdateMistBossDmgRankData.Builder builder = SC_UpdateMistBossDmgRankData.newBuilder();
        MistBossDmgRankData.Builder updateRankData = MistBossDmgRankData.newBuilder();
        for (int i = 0; i < activityBossDmgRank.size(); i++) {
            MistActivityBossDmgRankData.Builder rankInfo = activityBossDmgRank.get(i);
            updateRankData.setPlayerIdx(rankInfo.getPlayerId());
            updateRankData.setPlayerName(rankInfo.getPlayerName());
            updateRankData.setRank(i);
            updateRankData.setDamage(rankInfo.getDamage());
            builder.addRankData(updateRankData);
        }
        broadcastMsg(MsgIdEnum.SC_UpdateMistBossDmgRankData_VALUE, builder, true);
    }

    protected void removeActivityBossRank(String playerIdx) {
        activityBossDmgRank.removeIf(e->e.getPlayerId().equals(playerIdx));
    }



    public void settleActivityRankData() {
        LogUtil.info("Room[{}] settle bossDamageRank size={}", getIdx(), activityBossDmgRank.size());
        CS_GS_UpdateActivityBossDmgRank.Builder builder = CS_GS_UpdateActivityBossDmgRank.newBuilder();
        builder.setMistLevel(getLevel());

        if (!activityBossDmgRank.isEmpty()) {
            Set<Integer> svrIndexSet = new HashSet<>();
            for (int i = 0; i < activityBossDmgRank.size(); i++) {
                MistActivityBossDmgRankData.Builder rankData = activityBossDmgRank.get(i);
                MistBossDmgRankInfo.Builder rankBuilder = MistBossDmgRankInfo.newBuilder();
                rankBuilder.setPlayerIdx(rankData.getPlayerId());
                rankBuilder.setRank(i + 1);
                builder.addRankData(rankBuilder);
                svrIndexSet.add(rankData.getFromSvrIndex());
            }
            for (Integer svrIndex : svrIndexSet) {
                GlobalData.getInstance().sendMsgToServer(svrIndex, MsgIdEnum.CS_GS_UpdateActivityBossDmgRank_VALUE, builder);
            }
        }
        sendBossActivityRankToPlayer(null);
        activityBossDmgRank.clear();
    }

    public List<MistRewardObj> buildMonsterBattleRewardObj(MistFighter fighter, int monsterFightCfgId, ProtoVector.Builder pos) {
        if (fighter == null && pos == null) {
            return null;
        }
        if (pos == null) {
            pos = fighter.getPos();
        }
        MistMonsterFightConfigObject monsterFightCfg = MistMonsterFightConfig.getById(monsterFightCfgId);
        if (monsterFightCfg == null) {
            return null;
        }
        int[][] battleRewardObjData = monsterFightCfg.getBatterrewardobj();
        if (battleRewardObjData == null || battleRewardObjData.length <= 0) {
            return null;
        }
        int rewardObjCount = 0;
        int rand = 0;
        // 得到对应随机奖励对象数量
        Map<Integer, Integer> rewardObjMap = null;
        for (int i = 0; i < battleRewardObjData.length; i++) {
            if (battleRewardObjData[i] == null || battleRewardObjData[i].length < 3) {
                continue;
            }
            rand = RandomUtils.nextInt(1000);
            if (rand > battleRewardObjData[i][0]) {
                continue;
            }
            if (rewardObjMap == null) {
                rewardObjMap = new HashMap<>();
            }
            rewardObjMap.put(battleRewardObjData[i][1], battleRewardObjData[i][2]);
            rewardObjCount += battleRewardObjData[i][2];
        }

        if (rewardObjMap == null) {
            return null;
        }
        List<ProtoVector.Builder> posList = new ArrayList<>();
        // 搜索附近的空闲位置,大于9个奖励就搜周围2格，小于9格就搜周围1格
        int searchPosIndex = rewardObjCount > 9 ? 2 : 1; //
        for (int i = -searchPosIndex; i <= searchPosIndex; i++) {
            for (int j = -searchPosIndex; j <= searchPosIndex; j++) {
                if (!getWorldMap().isPosReachable(pos.getX() + i, pos.getY() + j)) {
                    continue;
                }
                posList.add(ProtoVector.newBuilder().setX(pos.getX() + i).setY(pos.getY() + j));
            }
        }
        if (posList.size() < rewardObjCount) {
            for (int i = posList.size(); i < rewardObjCount; i++) {
                posList.add(ProtoVector.newBuilder().mergeFrom(pos.build()));
            }
        }
        Collections.shuffle(posList);

        //创建对应奖励对象
        MistExplodeConfigObject explodeCfg;
        List<MistRewardObj> objectList = new ArrayList<>();
        int posIndex = 0;
        for (Entry<Integer, Integer> entry : rewardObjMap.entrySet()) {
            explodeCfg = MistExplodeConfig.getById(entry.getKey());
            if (explodeCfg == null) {
                continue;
            }
            for (int i = 0; i < entry.getValue(); i++) {
                if (posIndex >= rewardObjCount) {
                    return objectList;
                }

                ProtoVector.Builder newPos = posList.get(posIndex);
                MistObject obj = getObjManager().createObj(explodeCfg.getObjtype());

                obj.addAttributes(explodeCfg.getInitprop());
                if (fighter != null) {
                    obj.initByMaster(fighter);
                }
                obj.setPos(newPos.build());
                obj.afterInit(new int[]{newPos.getX(), newPos.getY()}, null);
                getWorldMap().objFirstEnter(obj);
                if (obj instanceof MistRewardObj) {
                    objectList.add((MistRewardObj)obj);
                }
                posIndex++;
            }
        }
        return objectList;
    }

    public void onGameServerClose(int serverIndex) {
        if (serverIndex <= 0) {
            return;
        }
        MistPlayer mistPlayer;
        for (String playerIdx : getMemberList()) {
            mistPlayer = MistPlayerCache.getInstance().queryObject(playerIdx);
            if (mistPlayer == null || serverIndex != mistPlayer.getServerIndex()) {
                continue;
            }
            MistFighter fighter = objManager.getMistObj(mistPlayer.getFighterId());
            if (fighter != null) {
                fighter.autoSettleBattle();
                onPlayerExit(mistPlayer, false);
            }
            Event event = Event.valueOf(EventType.ET_Logout, this, mistPlayer);
            event.pushParam(false);
            EventManager.getInstance().dispatchEvent(event);
            LogUtil.error("Gameserver[" + serverIndex + "] close, force kick out player,id=" + playerIdx + ",name=" + mistPlayer.getName());
        }
    }

    public void onTick(long curTime) {
//        updateForcePvpState(curTime);
        objManager.onTick(curTime);
        objGenerator.onTick(curTime);
        if (scheduleManager != null) {
            scheduleManager.onTick(curTime);
        }
    }
}

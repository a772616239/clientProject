package model.mistforest.mistobj;

import cfg.CrossArenaLvCfg;
import cfg.CrossArenaLvCfgObject;
import cfg.CrossConstConfig;
import cfg.GameConfig;
import cfg.MistBagConfig;
import cfg.MistBagConfigObject;
import cfg.MistBattleConfig;
import cfg.MistBattleReward;
import cfg.MistBattleRewardObject;
import cfg.MistCommonConfig;
import cfg.MistCommonConfigObject;
import cfg.MistGoblinConfig;
import cfg.MistGoblinConfigObject;
import cfg.MistJewelryConfig;
import cfg.MistJewelryConfigObject;
import cfg.MistLootPackCarryConfig;
import cfg.MistLootPackCarryConfigObject;
import cfg.MistMonsterFightConfig;
import cfg.MistMonsterFightConfigObject;
import cfg.MistMoveEffectConfig;
import cfg.MistMoveEffectConfigObject;
import cfg.MistNewbieTaskConfig;
import cfg.MistNewbieTaskConfigObject;
import cfg.MistSkillConfig;
import cfg.MistSkillConfigObject;
import cfg.MistWorldMapConfig;
import cfg.MistWorldMapConfigObject;
import cfg.PlayerLevelConfig;
import cfg.PlayerLevelConfigObject;
import common.GameConst;
import common.GameConst.EventType;
import common.GlobalData;
import common.GlobalTick;
import common.load.ServerConfig;
import datatool.StringHelper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import model.mistforest.MistConst;
import model.mistforest.MistConst.MistAdditionBuffType;
import model.mistforest.MistConst.MistBattleSide;
import model.mistforest.MistConst.MistBuffInterruptType;
import model.mistforest.MistConst.MistSkillTiming;
import model.mistforest.MistConst.MistTriggerParamType;
import model.mistforest.MistConst.MistVipSkillType;
import model.mistforest.ai.RobotController;
import model.mistforest.map.Aoi.AoiNode;
import model.mistforest.map.grid.Grid;
import model.mistforest.mistobj.activityboss.MistActivityBoss;
import model.mistforest.mistobj.activityboss.MistManEaterMonster;
import model.mistforest.mistobj.activityboss.MistManEaterPhantom;
import model.mistforest.mistobj.activityboss.MistSlimeMonster;
import model.mistforest.mistobj.rewardobj.MistBaseBox;
import model.mistforest.mistobj.rewardobj.MistRewardObj;
import model.mistforest.mistobj.rewardobj.MistTreasureBag;
import model.mistforest.room.entity.MistGhostBusterRoom.MistGhostBusterRoom;
import model.mistforest.room.entity.MistRoom;
import model.mistforest.skill.Skill;
import model.mistforest.skill.SkillMachine;
import model.mistforest.task.MistNpcTask;
import model.mistforest.team.MistTeam;
import model.mistplayer.MistPlayerConstant;
import model.mistplayer.cache.MistPlayerCache;
import model.mistplayer.entity.MistPlayer;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.math.RandomUtils;
import protocol.Battle.BattleTypeEnum;
import protocol.Battle.PetBuffData;
import protocol.Common.RewardTypeEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.BattleCMD_ChangePos;
import protocol.MistForest.BattleCMD_Emoji;
import protocol.MistForest.BattleCMD_ExplodeReward;
import protocol.MistForest.BattleCMD_OpenDoor;
import protocol.MistForest.BattleCMD_SnapShotList;
import protocol.MistForest.BattleCMD_TreasureBagTakeIn;
import protocol.MistForest.BattleCMD_TreasureBagTakeIn.BeTakenInBagInfo;
import protocol.MistForest.BattleCMD_UpdateItemSkill;
import protocol.MistForest.BattleCMD_UseItem;
import protocol.MistForest.BattleCmdData;
import protocol.MistForest.EnumMistRuleKind;
import protocol.MistForest.EnumMistTipsType;
import protocol.MistForest.LifeStateEnum;
import protocol.MistForest.MistAttackModeEnum;
import protocol.MistForest.MistBattleCmdEnum;
import protocol.MistForest.MistForestRoomInfo;
import protocol.MistForest.MistRetCode;
import protocol.MistForest.MistScheduleTypeEnum;
import protocol.MistForest.MistShowData;
import protocol.MistForest.MistTaskTargetType;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.MistUnitTypeEnum;
import protocol.MistForest.ProtoVector;
import protocol.MistForest.SC_BattleCmd;
import protocol.MistForest.SC_LavaBadgeCombine;
import protocol.MistForest.SC_UpdateMistShowData;
import protocol.MistForest.UnitMetadata;
import protocol.MistForest.UnitSnapShot;
import protocol.PlayerInfo.MistMoveEffectInfo;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer.CS_GS_MistDirectSettleBattleData;
import protocol.ServerTransfer.CS_GS_MistRoomEnterInfo;
import protocol.ServerTransfer.CS_GS_MistTargetMissionData;
import protocol.ServerTransfer.CS_GS_UpdateHiddenEvilData;
import protocol.ServerTransfer.CS_GS_UpdateJewelryCountData;
import protocol.ServerTransfer.CS_GS_UpdateMistItemData;
import protocol.ServerTransfer.EnumMistPveBattleType;
import protocol.TargetSystem.TargetTypeEnum;
import protocol.TransServerCommon.MistBornPosInfo;
import server.event.Event;
import server.event.EventManager;
import util.GameUtil;
import util.LogUtil;
import util.TimeUtil;

public class MistFighter extends MistObject {
    protected SkillMachine skillMachine;

    protected int continualKillCount;

    protected int teamId;

    protected HashSet<Long> dropTrapSet; // 还包括画地为牢的牢笼

    protected int battleType; // 1为pvp,2为boss战斗,3为怪物战斗,4为召唤宝珠战斗

    protected Map<Long, Integer> absorbedBagIdList;

    protected long lastDealBagTime;

    protected long sendClockTime;

    protected long joinMistRoomTime;

    protected Map<Integer, Integer> battleBuffMap; // 战斗buff<buffId, 层数>
    protected Map<Integer, Integer> additionBuffData; // 加成buff信息，用于战力比对

    protected RobotController robController;

    protected Set<Long> selfVisibleTargetList;

    protected Set<Long> beTouchedSnowBalls;

    protected MistNpcTask npcTask;

    public MistFighter(MistRoom room, int objType) {
        super(room, objType);
        this.skillMachine = new SkillMachine(this);
        this.dropTrapSet = new HashSet<>();
        this.absorbedBagIdList = new HashMap<>();
        this.battleBuffMap = new HashMap<>();
        this.additionBuffData = new HashMap<>();
        this.selfVisibleTargetList = new HashSet<>();
        this.beTouchedSnowBalls = new HashSet<>();
        this.npcTask = new MistNpcTask(this);
    }

    public void clear() {
        super.clear();
        skillMachine.clear();
        dropTrapSet.clear();
        battleBuffMap.clear();
        additionBuffData.clear();
        selfVisibleTargetList.clear();
        beTouchedSnowBalls.clear();
        npcTask.clear();
        continualKillCount = 0;
        teamId = 0;
        lastDealBagTime = 0;
        sendClockTime = 0;
//        speedUpTime = 0;
//        recvSnapShotCount = 0;
//        checkSnapShotTime = 0;
        joinMistRoomTime = 0;
        battleType = 0;
    }

    @Override
    public void initPos(int[] initialPos, int[] initialToward) {
        MistBornPosInfo posObj = room.getObjGenerator().getRandomBornPosObj(getType(), true, false);
        if (posObj != null) {
            setInitPos(posObj.getPos().getX(), posObj.getPos().getY());
        }
        setPos(initPos.build());
        setToward(initToward.build());
    }

    public void initWantedPlayer() {
        if (getAttribute(MistUnitPropTypeEnum.MUPT_IsWantedState_VALUE) <= 0) {
            return;
        }
        MistBornPosInfo objPos = room.getObjGenerator().getRandomBornPosObj(getType(), false, false);
        if (objPos == null) {
            return;
        }
        setPos(objPos.getPos().getX(), objPos.getPos().getY());
        setAttribute(MistUnitPropTypeEnum.MUPT_PlayerAttackMode_VALUE, MistAttackModeEnum.EAME_Attack_VALUE);
    }

    @Override
    public void dead() {
        setAttribute(MistUnitPropTypeEnum.MUPT_LifeState_VALUE, LifeStateEnum.LSE_Dead_VALUE);
        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_LifeState_VALUE, LifeStateEnum.LSE_Dead_VALUE);

        AoiNode aoiNode = room.getWorldMap().getAoiNodeById(getAoiNodeKey());
        if (aoiNode != null) {
            aoiNode.onObjLeave(this, null);
        }
        Grid grid = room.getWorldMap().getGridByPos(getPos().getX(), getPos().getY());
        if (grid != null) {
            grid.onObjLeave(this);
        }
        setDeadTimeStamp(GlobalTick.getInstance().getCurrentTime());
        clearSelfVisibleTargets();
        if (getAttribute(MistUnitPropTypeEnum.MUPT_UsingShowObjState_VALUE) > 0) {
            getRoom().getObjManager().removeShowTreasureFighter(getId());
        }
    }

    public void afterInit(String playerIdx, int camp) {
        setAttribute(MistUnitPropTypeEnum.MUPT_Group_VALUE, camp);
        if (!StringHelper.isNull(playerIdx)) {
            long ownerId = Long.valueOf(playerIdx);
            setAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE, ownerId);
        }
        super.afterInit(null, null);
        if (room.getMistRule() == EnumMistRuleKind.EMRK_Maze_VALUE) {
            setAttribute(MistUnitPropTypeEnum.MUPT_MazeAreaLevel_VALUE, 1l); // 迷宫初始为1层
        }
        initSkill();
        skillMachine.triggerPassiveSkills(MistSkillTiming.JoinRoom, this, null);
        setAttribute(MistUnitPropTypeEnum.MUPT_UnitRemainHp_VALUE, getAttribute(MistUnitPropTypeEnum.MUPT_UnitMaxHp_VALUE));
    }

    public void initGoblin() {
        if (getRoom().getMistRule() != EnumMistRuleKind.EMRK_Common_VALUE) {
            return;
        }
        int odds = CrossConstConfig.getById(GameConst.ConfigId).getGoblingenerateodds();
        if (RandomUtils.nextInt(1000) > odds) {
            return;
        }
        MistGoblin goblin = getRoom().getObjManager().createObj(MistUnitTypeEnum.MUT_Goblin_VALUE);
        if (null == goblin) {
            return;
        }
        MistGoblinConfigObject cfg = MistGoblinConfig.getById(getRoom().getLevel());
        if (cfg == null) {
            return;
        }
        goblin.setAttribute(MistUnitPropTypeEnum.MUPT_UnitConfigId_VALUE, cfg.getId());
        goblin.setAttribute(MistUnitPropTypeEnum.MUPT_VisiblePlayerUnitId_VALUE, getId());
        goblin.afterInit(null, null);
        addSelfVisibleTarget(goblin.getId());

        goblin.addCreateObjCmd();
    }

    public void initNewbieTask(int newbieTaskId) {
        MistNewbieTaskConfigObject cfg = MistNewbieTaskConfig.getById(newbieTaskId);
        if (cfg == null || cfg.getUnittype() <= 0) {
            return;
        }
        MistObject newObj = room.getObjManager().createObj(cfg.getUnittype());
        newObj.addAttributes(cfg.getUnitprop());
        newObj.setAttribute(MistUnitPropTypeEnum.MUPT_VisiblePlayerUnitId_VALUE, getId());
        newObj.setAttribute(MistUnitPropTypeEnum.MUPT_NewbieTaskId_VALUE, newbieTaskId);
        newObj.afterInit(cfg.getUnitpos(), null);
        newObj.addCreateObjCmd();
        addSelfVisibleTarget(newObj.getId());
    }

    public void initFighterSpeed(int crossVipLv) {
        CrossArenaLvCfgObject cfg = CrossArenaLvCfg.getByLv(crossVipLv);
        if (cfg == null) {
            return;
        }
        long baseSpeed = getAttribute(MistUnitPropTypeEnum.MUPT_Speed_VALUE);
        baseSpeed += baseSpeed * cfg.getMistspeeduprate() / 1000;
        setAttribute(MistUnitPropTypeEnum.MUPT_Speed_VALUE, baseSpeed);
    }

    public void addSelfVisibleTarget(long targetId) {
        selfVisibleTargetList.add(targetId);
    }

    public void removeSelfVisibleTarget(long targetId) {
        if (selfVisibleTargetList.contains(targetId)) {
            selfVisibleTargetList.remove(targetId);
        }
    }

    public void clearSelfVisibleTargets() {
        if (!CollectionUtils.isEmpty(selfVisibleTargetList)) {
            return;
        }
        for (Long targetId : selfVisibleTargetList) {
            MistObject obj = getRoom().getObjManager().getMistObj(targetId);
            if (obj == null || !obj.isAlive()) {
                continue;
            }
            obj.dead();
        }
    }

    public void addBeTouchedSnowBall(long id) {
        beTouchedSnowBalls.add(id);
    }

    public void removeBeTouchedSnowBall(long id) {
        beTouchedSnowBalls.remove(id);
    }

    public boolean checkBeTouchedSnowBall(long id) {
        return beTouchedSnowBalls.contains(id);
    }

    public void initSkill() {
        skillMachine.clearPassiveSkill();
        for (MistSkillConfigObject cfg : MistSkillConfig.getInstance().getInitSkillList()) {
            skillMachine.addPassiveSkill(cfg.getSkilltriggertiming(), new Skill(cfg.getId(), this, cfg));
        }
    }

    public void initRobController() {
        robController = new RobotController(this);
    }

    public boolean isRobotFighter() {
        return getAttribute(MistUnitPropTypeEnum.MUPT_IsRobotPlayer_VALUE) > 0;
    }

    public MistPlayer getOwnerPlayer() {
        String playerIdx = GameUtil.longToString(getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE), "");
        return MistPlayerCache.getInstance().queryObject(playerIdx);
    }

    public MistPlayer getOwnerPlayerInSameRoom() {
        String playerIdx = GameUtil.longToString(getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE), "");
        MistPlayer mistPlayer = MistPlayerCache.getInstance().queryObject(playerIdx);
        if (mistPlayer == null || mistPlayer.getMistRoom() == null || !mistPlayer.getMistRoom().getIdx().equals(getRoom().getIdx())) {
            return null;
        }
        return mistPlayer;
    }

    public SkillMachine getSkillMachine() {
        return skillMachine;
    }

    public void setSkillMachine(SkillMachine skillMachine) {
        this.skillMachine = skillMachine;
    }

    public int getContinualKillCount() {
        return continualKillCount;
    }

    public void setContinualKillCount(int continualKillCount) {
        this.continualKillCount = continualKillCount;
    }


    public long getJoinMistRoomTime() {
        return joinMistRoomTime;
    }

    public void setJoinMistRoomTime(long joinMistRoomTime) {
        this.joinMistRoomTime = joinMistRoomTime;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
        if (teamId > 0) {
            changeCamp(-teamId);
        } else {
            changeCamp(0);
        }
    }

    public int getBattleType() {
        return battleType;
    }

    public void setBattleType(int battleType) {
        this.battleType = battleType;
    }

    public void addDropTrap(long trapId) {
        dropTrapSet.add(trapId);
    }

    public void removeDropTrap(long trapId) {
        dropTrapSet.remove(trapId);
    }

    public MistNpcTask getNpcTask() {
        return npcTask;
    }

    @Override
    public void changeCamp(long newCamp) {
        if (newCamp == 0) {
            // 还原回原阵营
            String playerId = GameUtil.longToString(getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE), "");
            MistPlayer player = MistPlayerCache.getInstance().queryObject(playerId);
            if (player != null) {
                setAttribute(MistUnitPropTypeEnum.MUPT_Group_VALUE, player.getCamp());
                addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_Group_VALUE, player.getCamp());
            }
        } else {
            setAttribute(MistUnitPropTypeEnum.MUPT_Group_VALUE, newCamp);
            addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_Group_VALUE, newCamp);
        }
        changeDropTrapCamp();
    }

    public void changeDropTrapCamp() {
        MistObject obj;
        long newCamp = getAttribute(MistUnitPropTypeEnum.MUPT_Group_VALUE);
        for (Long trapId : dropTrapSet) {
            obj = room.getObjManager().getMistObj(trapId);
            if (obj != null) {
                obj.changeCamp(newCamp);
            }
        }
    }

    public boolean canBeTouch() {
        if (!isAlive()) {
            return false;
        }
        if (getAttribute(MistUnitPropTypeEnum.MUPT_IsBornProtected_VALUE) > 0) {
            return false;
        }
        if (isBattling()) {
            return false;
        }
        return true;
    }

    public boolean canBeAttack() {
        if (!isAlive()) {
            return false;
        }
        if (getAttribute(MistUnitPropTypeEnum.MUPT_IsBornProtected_VALUE) > 0) {
            return false;
        }
        if (isBattling()) {
            return false;
        }
        if (getAttribute(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE) > 0) {
            return false;
        }
        return true;
    }

    public boolean checkTouchDis(MistObject target, boolean isDynamic) {
        int dis;
        if (isDynamic) {
            dis = (int) (target.getAttribute(MistUnitPropTypeEnum.MUPT_MaxDynamicTouchDis_VALUE));
            if (dis <= 0) {
                dis = MistConst.MistDynamicTouchMaxDistance;
            }
        } else {
            dis = (int) (target.getAttribute(MistUnitPropTypeEnum.MUPT_MaxStaticTouchDis_VALUE));
            if (dis <= 0) {
                dis = MistConst.MistStaticTouchMaxDistance;
            }
        }
        return MistConst.checkInRoughDistance(dis, getPos().build(), target.getPos().build());
    }

    public void touchObj(MistObject target) {
        if (target == null || !target.isAlive()) {
            LogUtil.debug("fighter touch obj failed:target null not alive");
            return;
        }
        if (!isAlive() || isBattling()) {
            return;
        }
        MistPlayer owner = getOwnerPlayerInSameRoom();
        if (owner == null) {
            return;
        }
        if (getAttribute(MistUnitPropTypeEnum.MUPT_IsBornProtected_VALUE) > 0) {
            return;
        }
        if (!checkTouchDis(target, isMoving())) {
            return;
        }
        long targetTouchFilterFlag = target.getAttribute(MistUnitPropTypeEnum.MUPT_TouchMask_VALUE);
        if ((targetTouchFilterFlag & getType() + 1) > 0) {
            return;
        }

        if (target instanceof MistFighter) {
            MistFighter fighter = (MistFighter) target;
            if (!canBeAttack() || !fighter.canBeAttack()) {
                return;
            }
            skillMachine.triggerPassiveSkills(MistSkillTiming.TouchPlayer, fighter, null);
        } else if (target instanceof MistBaseBox) {
            MistBaseBox box = (MistBaseBox) target;
            RetCodeEnum retCode = box.canTouch(this, owner.getMistStamina());
            if (retCode != RetCodeEnum.RCE_Success) {
                return;
            }
            box.beTouch(this);
        } else {
            target.beTouch(this);
        }
    }


    public void absorbTreasureBagList(List<Long> bagIds) {
        if (bagIds == null || bagIds.isEmpty()) {
            return;
        }
        MistPlayer owner = getOwnerPlayerInSameRoom();
        if (owner == null || owner.isGainBagBeyondCount()) {
            return;
        }
        if (!isAlive() || isBattling() || getAttribute(MistUnitPropTypeEnum.MUPT_IsBornProtected_VALUE) > 0) {
            return;
        }
        MistTreasureBag bag;
        MistBagConfigObject bagCfg;
        int count = Integer.min(10, bagIds.size()); // 最多处理10个
        int maxDistance = (int) getAttribute(MistUnitPropTypeEnum.MUPT_BagIntakeRadius_VALUE);
        BattleCmdData.Builder cmdBuilder = BattleCmdData.newBuilder();
        cmdBuilder.setCMDType(MistBattleCmdEnum.MBC_TreasureBagTakeIn);
        BattleCMD_TreasureBagTakeIn.Builder absorbBagCmd = BattleCMD_TreasureBagTakeIn.newBuilder();
        int progress = 0;
        for (int i = 0; i < count; i++) {
            bag = room.getObjManager().getMistObj(bagIds.get(i));
            if (bag == null) {
                continue;
            }
            if (!MistConst.checkInRoughDistance(maxDistance / 1000 + 2, getPos().build(), bag.getPos().build())) {
                LogUtil.debug("Too far to absorb bag, pos=" + getPos() + ",bag pos=" + bag.getPos());
                continue;
            }
            int bagType = (int) bag.getAttribute(MistUnitPropTypeEnum.MUPT_BagType_VALUE);
            bagCfg = MistBagConfig.getByBagid(bagType);
            if (bagCfg == null || bagCfg.getRewardcount() <= 0) {
                LogUtil.debug("bag cfg not found or reward is null");
                continue;
            }
            if (!bag.isQualifiedPlayer(this)) {
                continue;
            }
            bag.beAbsorbed();
            absorbedBagIdList.put(bag.getId(), bagType);
            progress++;

            BeTakenInBagInfo.Builder builder = BeTakenInBagInfo.newBuilder();
            builder.setTakerId(getId());
            builder.setTargetId(bag.getId());
            builder.setBagConfigId(bagType);
            absorbBagCmd.addBagInfo(builder);
        }
        cmdBuilder.setCMDContent(absorbBagCmd.build().toByteString());
        battleCmdList.addCMDList(cmdBuilder);
        getNpcTask().doNpcTask(MistTaskTargetType.MTTT_GainTreasureBag_VALUE, progress, 0);
        doMistTargetProg(TargetTypeEnum.TTE_MistSeasonTask_GainBagCount, 0, progress);
    }

    public boolean isTeammate(long fighterId) {
        if (teamId <= 0 || getId() == fighterId) {
            return false;
        }
        MistTeam team = room.getTeamById(teamId);
        if (team == null) {
            return false;
        }
        return team.isTeammate(getId(), fighterId);
    }

    public boolean isBattling() {
        return battleType > 0;
    }

    public boolean notNeedToBroadCastMsg() {
        return battleType > 0 && getAttribute(MistUnitPropTypeEnum.MUPT_FightExpireTimestamp_VALUE) == 0;
    }

    @Override
    public void sendSnapShotToPlayers(UnitSnapShot snapShot) {
        SC_BattleCmd.Builder builder = SC_BattleCmd.newBuilder();
        BattleCmdData.Builder cmdBuilder = BattleCmdData.newBuilder().setCMDType(MistBattleCmdEnum.MBC_SnapShotList);
        BattleCMD_SnapShotList.Builder snapShotBuilder = BattleCMD_SnapShotList.newBuilder();
        snapShotBuilder.addSnapShotList(snapShot);
        cmdBuilder.setCMDContent(snapShotBuilder.build().toByteString());
        builder.addCMDList(cmdBuilder);
        broadcastCommand(builder);
    }

    protected Map<Integer, Set<String>> getNeedBroadcastPlayer() {
        AoiNode aoiNode = room.getWorldMap().getAoiNodeById(getAoiNodeKey());
        Map<Integer ,Set<String>> playerMap = null;
        if (aoiNode != null) {
            playerMap = new HashMap<>();
            aoiNode.getAllAroundPlayers(playerMap, this);
        }
        MistTeam team = room.getTeamById(getTeamId());
        if (team != null) {
            if (playerMap == null) {
                playerMap = new HashMap<>();
            }
            MistConst.mergePlayerMap(playerMap, team.getTeamIpPlayerIds(this));
        }
        return playerMap;
    }

    protected Map<Integer, Set<String>> getNeedBroadcastPlayerExcludeSelf() {
        AoiNode aoiNode = room.getWorldMap().getAoiNodeById(getAoiNodeKey());
        Map<Integer ,Set<String>> playerMap = null;
        if (aoiNode != null) {
            playerMap = new HashMap<>();
            aoiNode.getAllAroundPlayersExcludeSelf(playerMap, this);
        }
        MistTeam team = room.getTeamById(getTeamId());
        if (team != null) {
            if (playerMap == null) {
                playerMap = new HashMap<>();
            }
            MistConst.mergePlayerMap(playerMap, team.getTeamIpPlayerIds(this));
        }
        return playerMap;
    }

    public void broadcastCommand(SC_BattleCmd.Builder builder) {
        Map<Integer, Set<String>> ipPlayerMap = getNeedBroadcastPlayer();
        if (ipPlayerMap != null) {
            for (Entry<Integer, Set<String>> entry : ipPlayerMap.entrySet()) {
                GlobalData.getInstance().sendMistMsgToServer(entry.getKey(), MsgIdEnum.SC_BattleCmd_VALUE, entry.getValue(), builder);
            }
        }
    }

    public void broadcastCmdExcludeSelf(SC_BattleCmd.Builder builder) {
        Map<Integer, Set<String>> ipPlayerMap = getNeedBroadcastPlayerExcludeSelf();
        if (ipPlayerMap != null) {
            for (Entry<Integer, Set<String>> entry : ipPlayerMap.entrySet()) {
                GlobalData.getInstance().sendMistMsgToServer(entry.getKey(), MsgIdEnum.SC_BattleCmd_VALUE, entry.getValue(), builder);
            }
        }
    }

    @Override
    protected void updateBattleCmd() {
        if (battleCmdList.getCMDListCount() <= 0) {
            return;
        }
        broadcastCommand(battleCmdList);
    }

    @Override
    public boolean checkSnapShot(UnitSnapShot snapShot) {
        if (snapShot.getUnitId() != getId()) {
            return false;
        }
        if (isRobotFighter()) {
            return false;
        }
//        if (speedUpTime > 0) {
//            backToPos();
//            return false;
//        }
        int posX = snapShot.getPos().getX() / 1000;
        int posY = snapShot.getPos().getY() / 1000;
        if (isBattling()) {
            long directFightExpireTime = getAttribute(MistUnitPropTypeEnum.MUPT_FightExpireTimestamp_VALUE);
            if (directFightExpireTime <= 0 || !MistConst.checkInDistance(2, getPos().getX(), getPos().getY(), posX, posY)) {
                backToPos();
                return false;
            }
        }

        if (!room.getWorldMap().isPosReachable(this, posX, posY)) {
            backToPos();
            return false;
        }
        if (room.getWorldMap().isInSafeRegion(posX, posY) ) {
            if (getAttribute(MistUnitPropTypeEnum.MUPT_OwningKeyState_VALUE) > 0 || getAttribute(MistUnitPropTypeEnum.MUPT_IsWantedState_VALUE) > 0) {
                backToPos();
                return false;
            }
        } else {
            if (getAttribute(MistUnitPropTypeEnum.MUPT_IsPunishing_VALUE) > 0 || getAttribute(MistUnitPropTypeEnum.MUPT_ReadStateFlag_VALUE) > 0) {
                backToPos();
                return false;
            }
        }
        long curTime = GlobalTick.getInstance().getCurrentTime();
        long deltaTime = curTime - getJoinMistRoomTime();
        if (deltaTime + ServerConfig.getInstance().getTimeTickCycle() < snapShot.getGameDuration()) {
            backToPos();
            LogUtil.error("fighter[" + getId() + "] check SnapShotCount, Server Duration=" + deltaTime + ",Client Duration="
                    + snapShot.getGameDuration());
            setJoinMistRoomTime(curTime - snapShot.getGameDuration());
            return false;
        }
//        if (snapShot.getIsMoving())
//        }{
//            long snapShotCycle = ServerConfig.getInstance().getMistSnapShotCycle();
//            if (recvSnapShotCount > 0 && (curTime - checkSnapShotTime) / snapShotCycle + 2 < recvSnapShotCount) {
//                LogUtil.error("fighter[" + getId() + "] check SnapShotCount failed, recvCount=" + recvSnapShotCount
//                        + ",threshold=" + ((curTime - checkSnapShotTime) / snapShotCycle + 2));
//                backToPos();
//                speedUpTime = curTime + 2 * MistConst.CheckClientClockTime;
//                recvSnapShotCount = 0;
//                checkSnapShotTime = 0;
//                return false;
//            } else if (recvSnapShotCount == 0) {
//                checkSnapShotTime = curTime;
//            }
//            LogUtil.debug("fighter[" + getId() + "] add check SnapShotCount, recvCount=" + recvSnapShotCount + ",threshold="
//                    + ((curTime - checkSnapShotTime) / snapShotCycle + 2));
//            ++recvSnapShotCount;
//        } else {
//            recvSnapShotCount = 0;
//            checkSnapShotTime = 0;
//            LogUtil.debug("fighter["+getId()+"] clear check SnapShotCount");
//        }
        return true;
    }

    public boolean isNearPlayer(MistFighter other) {
        AoiNode aoiNode = room.getWorldMap().getAoiNodeById(getAoiNodeKey());
        if (aoiNode == null) {
            return false;
        }
        return aoiNode.getKey() == other.getAoiNodeKey() || aoiNode.isAroundAoiNode(other.getAoiNodeKey());
    }

    // 发现玩家的位置不可达返回当前位置回去
    protected void backToPos() {
        MistPlayer player = getOwnerPlayerInSameRoom();
        if (player == null) {
            return;
        }
        SC_BattleCmd.Builder builder = SC_BattleCmd.newBuilder();
        BattleCmdData.Builder cmdBuilder = BattleCmdData.newBuilder();
        cmdBuilder.setCMDType(MistBattleCmdEnum.MBC_ChangePos);
        BattleCMD_ChangePos.Builder changePosBuilder = BattleCMD_ChangePos.newBuilder();
        changePosBuilder.setTargetId(getId());
        ProtoVector.Builder builder1 = ProtoVector.newBuilder();
        builder1.setX(getPos().getX() * 1000);
        builder1.setY(getPos().getY() * 1000);
        changePosBuilder.setPos(builder1);
        changePosBuilder.setTowards(getToward());
        cmdBuilder.setCMDContent(changePosBuilder.build().toByteString());
        builder.addCMDList(cmdBuilder);
        player.sendMsgToServer(MsgIdEnum.SC_BattleCmd_VALUE, builder);
    }

    public void changeFighterPos(Long posData) {
        if (posData != null) {
            int posX = GameUtil.getHighLong(posData);
            int posY = GameUtil.getLowLong(posData);
            if (!getRoom().getWorldMap().isPosReachable(posX, posY)) {
                return;
            }
            int oldX = getPos().getX();
            int oldY = getPos().getY();


            setPos(posX, posY);
            getRoom().getWorldMap().objMove(this, oldX, oldY);
        }
    }

    public void sendUpdateItemSkillCmd(boolean bAdd) {
        MistPlayer owner = getOwnerPlayerInSameRoom();
        if (owner == null) {
            return;
        }
        SC_BattleCmd.Builder cmdList = SC_BattleCmd.newBuilder();
        BattleCmdData.Builder cmdBuilder = BattleCmdData.newBuilder();
        cmdBuilder.setCMDType(MistBattleCmdEnum.MBC_UpdateItemSkill);
        BattleCMD_UpdateItemSkill.Builder updateSkills = BattleCMD_UpdateItemSkill.newBuilder();
        updateSkills.setHostId(id);
        updateSkills.addAllMistItemData(skillMachine.getAllItemSkillInfo());
        cmdBuilder.setCMDContent(updateSkills.build().toByteString());
        cmdList.addCMDList(cmdBuilder);
        owner.sendMsgToServer(MsgIdEnum.SC_BattleCmd_VALUE, cmdList);

        CS_GS_UpdateMistItemData.Builder builder = CS_GS_UpdateMistItemData.newBuilder();
        builder.setPlayerIdx(owner.getIdx());
        builder.setAddItem(bAdd);
        builder.addAllItemData(skillMachine.getAllItemSkillInfo());
        builder.setMistRuleValue(room.getMistRule());
        GlobalData.getInstance().sendMsgToServer(
                owner.getServerIndex(), MsgIdEnum.CS_GS_UpdateMistItemData_VALUE, builder);
    }

    public void addUseItemCmd(int itemCfgId) {
        if (itemCfgId <= 0) {
            return;
        }
        BattleCmdData.Builder useItemCmd = BattleCmdData.newBuilder();
        useItemCmd.setCMDType(MistBattleCmdEnum.MBC_UseItem);
        BattleCMD_UseItem.Builder useItemBuilder = BattleCMD_UseItem.newBuilder();
        useItemBuilder.setTargetId(id);
        useItemBuilder.setItemCfgId(itemCfgId);
        useItemCmd.setCMDContent(useItemBuilder.build().toByteString());

        battleCmdList.addCMDList(useItemCmd);
    }

    public void sendTipsCmd(int tipsType) {
        MistPlayer owner = getOwnerPlayerInSameRoom();
        if (owner == null) {
            return;
        }
        owner.sendMsgToServer(MsgIdEnum.SC_BattleCmd_VALUE, room.buildMistTips(tipsType, this, this));
    }

    public void updateEmoj(BattleCMD_Emoji emoji) {
        SC_BattleCmd.Builder builder = SC_BattleCmd.newBuilder();
        BattleCmdData.Builder cmdBuilder = BattleCmdData.newBuilder().setCMDType(MistBattleCmdEnum.MBC_Emoji);
        cmdBuilder.setCMDContent(emoji.toByteString());
        builder.addCMDList(cmdBuilder);

        broadcastCmdExcludeSelf(builder);
    }

    public void sendExplodeDropReward(ProtoVector.Builder explodePos, Map<Integer, Integer> rewardMap, List<MistRewardObj> rewardObjs) {
        if ((rewardMap == null || rewardMap.isEmpty()) && CollectionUtils.isEmpty(rewardObjs)) {
            return;
        }
        MistPlayer owner = getOwnerPlayerInSameRoom();
        if (owner == null) {
            return;
        }
        SC_BattleCmd.Builder builder = SC_BattleCmd.newBuilder();
        BattleCmdData.Builder cmdBuilder = BattleCmdData.newBuilder().setCMDType(MistBattleCmdEnum.MBC_ExplodeReward);
        BattleCMD_ExplodeReward.Builder dropRewardCmd = BattleCMD_ExplodeReward.newBuilder();
        dropRewardCmd.setExplodePos(explodePos);
        dropRewardCmd.setPlayerUnitId(getId());
        if (rewardMap != null) {
            for (Entry<Integer, Integer> entry : rewardMap.entrySet()) {
                dropRewardCmd.getDropRewardBuilder().addCarryRewardId(entry.getKey());
                dropRewardCmd.getDropRewardBuilder().addCount(entry.getValue());
            }
        }
        if (rewardObjs != null) {
            for (MistRewardObj rewardObj : rewardObjs) {
                dropRewardCmd.addDropObjs(rewardObj.getMetaData(this));
            }
        }
        cmdBuilder.setCMDContent(dropRewardCmd.build().toByteString());
        builder.addCMDList(cmdBuilder);
        owner.sendMsgToServer(MsgIdEnum.SC_BattleCmd_VALUE, builder);
    }

    public void gainReward(int rewardId) {
        MistPlayer owner = getOwnerPlayerInSameRoom();
        if (owner == null) {
            return;
        }
        MistLootPackCarryConfigObject config = MistLootPackCarryConfig.getById(rewardId);
        if (config == null) {
            return;
        }
        HashMap<Integer, Integer> rewardMap = new HashMap<>();
        rewardMap.put(config.getId(), 1);
        Event event = Event.valueOf(EventType.ET_GainMistCarryReward, room, owner);
        event.pushParam(rewardMap, false);
        EventManager.getInstance().dispatchEvent(event);
    }

    public void gainRewardBox(int rewardId, int count) {
        MistPlayer owner = getOwnerPlayerInSameRoom();
        if (owner == null) {
            return;
        }
        MistLootPackCarryConfigObject config = MistLootPackCarryConfig.getById(rewardId);
        if (config == null) {
            return;
        }
        HashMap<Integer, Integer> rewardMap = new HashMap<>();
        rewardMap.put(config.getId(), count);
        Event event = Event.valueOf(EventType.ET_GainMistCarryReward, room, owner);
        event.pushParam(rewardMap, false);
        EventManager.getInstance().dispatchEvent(event);
    }

    public void gainActivityBossReward(int rewardId) {
        MistPlayer owner = getOwnerPlayerInSameRoom();
        if (owner == null) {
            return;
        }
        MistLootPackCarryConfigObject config = MistLootPackCarryConfig.getById(rewardId);
        if (config == null) {
            return;
        }
        HashMap<Integer, Integer> rewardMap = new HashMap<>();
        rewardMap.put(config.getId(), 1);
        Event event = Event.valueOf(EventType.ET_GainMistCarryReward, room, owner);
        event.pushParam(rewardMap, true);
        EventManager.getInstance().dispatchEvent(event);
    }

    public void sendOpenDoorCmd(int newMistLevel) {
        MistPlayer player = getOwnerPlayerInSameRoom();
        if (Objects.isNull(player)) {
            return;
        }
        MistWorldMapConfigObject cfg = MistWorldMapConfig.getInstance().getByRuleAndLevel(room.getMistRule(), newMistLevel);
        if (cfg == null) {
            return;
        }
        SC_BattleCmd.Builder builder = SC_BattleCmd.newBuilder();
        BattleCmdData.Builder openDoorCmd = BattleCmdData.newBuilder();
        openDoorCmd.setCMDType(MistBattleCmdEnum.MBC_OpenDoor);
        BattleCMD_OpenDoor.Builder openDoorBuilder = BattleCMD_OpenDoor.newBuilder();
        openDoorBuilder.setNewMapId(cfg.getMapid());
        openDoorCmd.setCMDContent(openDoorBuilder.build().toByteString());
        builder.addCMDList(openDoorCmd);
        player.sendMsgToServer(MsgIdEnum.SC_BattleCmd_VALUE, builder);
    }

    public void addBattleBuff(int battleBuffId) {
        battleBuffMap.merge(battleBuffId, 1, (preVal, deltaVal) -> preVal + deltaVal);
    }

    public void removeBattleBuff(int battleBuffId) {
        Integer count = battleBuffMap.get(battleBuffId);
        if (count == null || count <= 1) {
            battleBuffMap.remove(battleBuffId);
        }
        battleBuffMap.put(battleBuffId, --count);
    }

    public void clearBattleBuff(int battleBuffId) {
        if (!battleBuffMap.containsKey(battleBuffId)) {
            return;
        }
        battleBuffMap.remove(battleBuffId);
    }

    public List<PetBuffData> getExtendBuffList(boolean isPveBattle) {
        List<PetBuffData> buffList = new ArrayList<>();
        if (isPveBattle) {
            MistTeam team = room.getTeamById(teamId);
            if (team != null && team.getAllMembers().size() > 1) {
                PetBuffData.Builder buffData = PetBuffData.newBuilder();
                buffData.setBuffCfgId(CrossConstConfig.getById(GameConst.ConfigId).getMistteambattlebuff());
                buffData.setBuffCount(team.getAllMembers().size() - 1);
                buffList.add(buffData.build());
            }
        } else {
            for (Map.Entry<Integer, Integer> entry : battleBuffMap.entrySet()) {
                if (entry.getValue() > 0) {
                    PetBuffData.Builder buffData = PetBuffData.newBuilder();
                    buffData.setBuffCfgId(entry.getKey());
                    buffData.setBuffCount(entry.getValue());
                    buffList.add(buffData.build());
                }
            }
        }
        return buffList;
    }

    public void addAdditionBuffData(int additionType, int rate) {
        additionBuffData.merge(additionType, rate, (preVal, deltaVal) -> {
            int newVal = preVal + deltaVal;
            return newVal != 0 ? newVal : null;
        });
    }

    public long calcAdditionBuffRate() {
        int attackRate = 0;
        int defendRate = 0;
        int weekRate = 0;
        for (Entry<Integer, Integer> entry : additionBuffData.entrySet()) {
            switch (entry.getKey()) {
                case MistAdditionBuffType.attackRate:{
                    attackRate = entry.getValue();
                    break;
                }
                case MistAdditionBuffType.defendRate:{
                    defendRate = entry.getValue();
                    break;
                }
                case MistAdditionBuffType.weekRate:{
                    weekRate = entry.getValue();
                    break;
                }
                default:
                    break;
            }
        }
        return (long) (1000 + (5 * 1000 / 11 * (1000 + attackRate - weekRate) + 5 * 1000 / 22 * (1000 + defendRate)) / 1000);
    }

    public long getPvpFightPower() {
        MistPlayer player = getOwnerPlayerInSameRoom();
        if (player == null) {
            return 0;
        }
        long additionBuffRate = calcAdditionBuffRate();
        long remainHpRate = getAttribute(MistUnitPropTypeEnum.MUPT_UnitRemainHp_VALUE);
        long fightPower = player.getFightPower() * (1000 + additionBuffRate - 7 * 1000 / 22 * (1000 - remainHpRate) / 1000) / 1000;
        return Math.max(0, fightPower);
    }

    public boolean enterPveBattle(int battleType, MistObject mistObj) {
        MistPlayer player = getOwnerPlayerInSameRoom();
        if (player == null) {
            return false;
        }

        if (isBattling()) {
            return false;
        }
        if (isInSafeRegion()) {
            return false;
        }
        int fightMakeId = MistBattleConfig.getFightMakeId(room.getLevel(), battleType);

        int fightCfgId = (int) mistObj.getAttribute(MistUnitPropTypeEnum.MUPT_MonsterFightCfgId_VALUE);
        MistMonsterFightConfigObject config = MistMonsterFightConfig.getById(fightCfgId);
        if (config != null && config.getFightmakeid() != null && config.getFightmakeid().length > 0) {
            int index = RandomUtils.nextInt(config.getFightmakeid().length);
            fightMakeId = config.getFightmakeid()[index];
        }
        if (fightMakeId <= 0) {
            return false;
        }
        bufMachine.interruptBuffByType(MistBuffInterruptType.EnterPveBattle);

        long battlePos = MistConst.protoPosToLongPos(getPos().build());
        setAttribute(MistUnitPropTypeEnum.MUPT_BattlingPos_VALUE, battlePos);
        if (battleType == EnumMistPveBattleType.EMPBT_MonsterBattle_VALUE || battleType == EnumMistPveBattleType.EMPBT_EliteMonsterBattle_VALUE){
            setAttribute(MistUnitPropTypeEnum.MUPT_BattlingTargetId_VALUE, mistObj.getId());
        } else {
            setAttribute(MistUnitPropTypeEnum.MUPT_BattlingTargetId_VALUE, -1);
        }
        setBattleType(battleType + 1);

        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_BattlingPos_VALUE, battlePos);
        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_BattlingTargetId_VALUE, mistObj.getId()); // 须在battlingPos和BattlingSide之后通知

        setAttribute(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE, 1);
        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE, 1);

        if (config != null && config.getDirectsettledecreasehp() > 0) {
            long expireTime = GlobalTick.getInstance().getCurrentTime() + TimeUtil.MS_IN_A_S * 3;
            setAttribute(MistUnitPropTypeEnum.MUPT_FightExpireTimestamp_VALUE, expireTime);
            addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE, expireTime);
        } else {
            Event event = Event.valueOf(EventType.ET_EnterMistPveBattle, room, player);
            event.pushParam(battleType);
            event.pushParam(fightMakeId);
            event.pushParam(getExtendBuffList(true));
            event.pushParam(fightCfgId);
            EventManager.getInstance().dispatchEvent(event);
        }
        return true;
    }

    public boolean enterMagicGuardBattle(int battleType, MistMagicGuard magicGuard, Collection<Integer> extBuffIdList) {
        MistPlayer player = getOwnerPlayerInSameRoom();
        if (player == null) {
            return false;
        }

        if (isBattling()) {
            return false;
        }
        if (isInSafeRegion()) {
            return false;
        }
        int fightMakeId = MistBattleConfig.getFightMakeId(room.getLevel(), battleType);

        int fightCfgId = (int) magicGuard.getAttribute(MistUnitPropTypeEnum.MUPT_MonsterFightCfgId_VALUE);
        MistMonsterFightConfigObject config = MistMonsterFightConfig.getById(fightCfgId);
        if (config != null && config.getFightmakeid() != null && config.getFightmakeid().length > 0) {
            int index = RandomUtils.nextInt(config.getFightmakeid().length);
            fightMakeId = config.getFightmakeid()[index];
        }
        if (fightMakeId <= 0) {
            return false;
        }
        bufMachine.interruptBuffByType(MistBuffInterruptType.EnterPveBattle);

        long battlePos = MistConst.protoPosToLongPos(getPos().build());
        setAttribute(MistUnitPropTypeEnum.MUPT_BattlingPos_VALUE, battlePos);
        if (battleType == EnumMistPveBattleType.EMPBT_MonsterBattle_VALUE || battleType == EnumMistPveBattleType.EMPBT_EliteMonsterBattle_VALUE){
            setAttribute(MistUnitPropTypeEnum.MUPT_BattlingTargetId_VALUE, magicGuard.getId());
        } else {
            setAttribute(MistUnitPropTypeEnum.MUPT_BattlingTargetId_VALUE, -1);
        }
        setBattleType(battleType + 1);

        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_BattlingPos_VALUE, battlePos);
        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_BattlingTargetId_VALUE, magicGuard.getId()); // 须在battlingPos和BattlingSide之后通知

        setAttribute(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE, 1);
        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE, 1);

        if (config != null && config.getDirectsettledecreasehp() > 0) {
            long expireTime = GlobalTick.getInstance().getCurrentTime() + TimeUtil.MS_IN_A_S * 3;
            setAttribute(MistUnitPropTypeEnum.MUPT_FightExpireTimestamp_VALUE, expireTime);
            addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE, expireTime);
        } else {
            List<PetBuffData> extBuffList = getExtendBuffList(true);
            if (!CollectionUtils.isEmpty(extBuffIdList)) {
                for (int buffId : extBuffIdList) {
                    if (buffId <= 0) {
                        continue;
                    }
                    PetBuffData.Builder buffData = PetBuffData.newBuilder();
                    buffData.setBuffCfgId(buffId);
                    buffData.setBuffCount(1);
                    extBuffList.add(buffData.build());
                }
            }

            Event event = Event.valueOf(EventType.ET_EnterMistPveBattle, room, player);
            event.pushParam(battleType);
            event.pushParam(fightMakeId);
            event.pushParam(extBuffList);
            event.pushParam(fightCfgId);
            EventManager.getInstance().dispatchEvent(event);
        }
        return true;
    }

    public void onPveBattleSettle(boolean isWinner, int pveType, long damage, boolean directSettle) {
        if (getBattleType() <= 1) {
            LogUtil.error("settle pve battle failed,player not in pve battle,id=" + getId());
            return;
        }
        if (!isWinner) {
            setAttribute(MistUnitPropTypeEnum.MUPT_PlayerAttackMode_VALUE, MistAttackModeEnum.EAME_Peace_VALUE);
            addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_PlayerAttackMode_VALUE, MistAttackModeEnum.EAME_Peace_VALUE);
        }
        MistPlayer owner = getOwnerPlayerInSameRoom();
        if (pveType == EnumMistPveBattleType.EMPBT_BossBattle_VALUE) {
            int timing = isWinner ? MistSkillTiming.WinBossBattle : MistSkillTiming.LossBossBattle;
            skillMachine.triggerPassiveSkills(timing, this, null);

            if (isWinner) {
                MistBattleRewardObject rewardCfg = MistBattleReward.getByLevel(room.getLevel());
                if (rewardCfg != null) {
                    gainReward(rewardCfg.getBeatbossreward());

                    MistTeam team = room.getTeamById(getTeamId());
                    if (team != null) {
                        for (MistFighter member : team.getAllMembers().values()) {
                            if (member.getId() == getId()) {
                                continue;
                            }
                            member.gainReward(rewardCfg.getBeatbossteamreward());
                        }
                    }
                }
            }
        } else if (pveType == EnumMistPveBattleType.EMPBT_MonsterBattle_VALUE || pveType == EnumMistPveBattleType.EMPBT_EliteMonsterBattle_VALUE) {
            long monsterId = getAttribute(MistUnitPropTypeEnum.MUPT_BattlingTargetId_VALUE);
            MistObject monster = room.getObjManager().getMistObj(monsterId);
            int fightCfgId = 0;
            if (null != monster) {
                fightCfgId = (int) monster.getAttribute(MistUnitPropTypeEnum.MUPT_MonsterFightCfgId_VALUE);
            }
            int timing = isWinner ? MistSkillTiming.WinMonsterBattle : MistSkillTiming.LossMonsterBattle;
            HashMap<Integer, Long> params = new HashMap<>();
            params.put(MistTriggerParamType.SettleJewelryCount, MistConst.getMonsterBattleJewelryCount(fightCfgId));
            params.put(MistTriggerParamType.DirectSettleBattleFlag, directSettle ? 1l : 0l);
            if (getRoom().getScheduleManager() != null && getRoom().getScheduleManager().isScheduleTypeOpen(MistScheduleTypeEnum.MSTE_HotDispute_VALUE)) {
                params.put(MistTriggerParamType.LavaBadgeCount, MistConst.getFightLavaBadgeCount(fightCfgId));
            }
            skillMachine.triggerPassiveSkills(timing, this, params);

            if (isWinner && owner != null && fightCfgId > 0) {
                if (pveType == EnumMistPveBattleType.EMPBT_MonsterBattle_VALUE) {
                    Map<Integer, Integer> rewardMap = MistConst.buildMonsterBattleReward(fightCfgId, room.getMistRule(), owner.getLevel());
                    if (rewardMap != null && !rewardMap.isEmpty()) {
                        Event event = Event.valueOf(EventType.ET_MonsterBattleCarryReward, room, GameUtil.getDefaultEventSource());
                        event.pushParam(owner, rewardMap, fightCfgId, getPos());
                        EventManager.getInstance().dispatchEvent(event);
                    }
                }
            }
            if (monster instanceof MistMonster) {
                ((MistMonster)monster).settleBattle(this, !isWinner);
                if (isWinner) {
                    getNpcTask().doNpcTask(MistTaskTargetType.MTTT_KillMonster_VALUE, 1, 0);
                }
            } else if (monster instanceof MistEliteMonster) {
                ((MistEliteMonster)monster).settleBattle(this, !isWinner, damage);
                doMistTargetProg(TargetTypeEnum.TTE_Mist_JoinEliteMonsterFight, 0, 1);
                getNpcTask().doNpcTask(MistTaskTargetType.MTTT_JoinEliteMonsterFight_VALUE, 1, 0);
            } else if (monster instanceof MistMagicGuard) {
                ((MistMagicGuard)monster).settleBattle(this, damage);
            } else if (monster instanceof MistActivityBoss) {
                ((MistActivityBoss)monster).settleDamage(this, damage);
                doMistTargetProg(TargetTypeEnum.TTE_Mist_JoinActivityBossFight, 0, 1);
            } else if (monster instanceof MistSlimeMonster) {
                ((MistSlimeMonster)monster).settleDamage(damage);
            } else if (monster instanceof MistManEaterPhantom) {
                ((MistManEaterPhantom)monster).settleDamage(this, damage);
            } else if (monster instanceof MistManEaterMonster) {
                ((MistManEaterMonster)monster).settleDamage(this, damage);
            } else if (monster instanceof MistEliteDoorKeeper) {
                ((MistEliteDoorKeeper)monster).settleDamage(this, isWinner);
            }
        } else if (pveType == EnumMistPveBattleType.EMPBT_SummonEvilBattle_VALUE) {
            int cfgId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_HiddenEvilId_VALUE);
            int timing = isWinner ? MistSkillTiming.WinSummonEvilBattle : MistSkillTiming.LossSummonEvilBattle;
            skillMachine.triggerPassiveSkills(timing, this, null);
            if (isWinner) {
                Map<Integer, Integer> rewards = MistConst.buildJewelryReward(cfgId);
                if (rewards != null) {
                    Event event = Event.valueOf(EventType.ET_GainMistCarryReward, getRoom(), getOwnerPlayer());
                    event.pushParam(rewards, false);
                    EventManager.getInstance().dispatchEvent(event);
                }
            }
            getNpcTask().doNpcTask(MistTaskTargetType.MTTT_JoinHiddenEvilFight_VALUE, 1, 0);
        }
        setAttribute(MistUnitPropTypeEnum.MUPT_BattlingPos_VALUE, MistBattleSide.notBattle);
        setAttribute(MistUnitPropTypeEnum.MUPT_BattlingTargetId_VALUE, 0);
        setAttribute(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE, 0);

        setBattleType(0);

        if (getAttribute(MistUnitPropTypeEnum.MUPT_FightExpireTimestamp_VALUE) == 0 && owner != null && owner.isOnline()) {
            updateRevertRoomInfo();
        }
        setAttribute(MistUnitPropTypeEnum.MUPT_FightExpireTimestamp_VALUE, 0);

        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_BattlingTargetId_VALUE, 0);
        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_BattlingPos_VALUE, 0);
        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE, 0);
        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_FightExpireTimestamp_VALUE, 0);
    }

    public boolean isExploiting() {
        return getAttribute(MistUnitPropTypeEnum.MUPT_OwningKeyState_VALUE) > 0
                || getAttribute(MistUnitPropTypeEnum.MUPT_ExploitingResource_VALUE) > 0;
    }

    public boolean checkCanAttack(MistFighter targetFighter) {
        if (targetFighter == null) {
            return false;
        }
        if (isBattling()) {
            return false;
        }
        if (isInSafeRegion() || targetFighter.isInSafeRegion()) {
            return false;
        }
        long selfMode = getAttribute(MistUnitPropTypeEnum.MUPT_PlayerAttackMode_VALUE);
        long targetMode = targetFighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerAttackMode_VALUE);
        boolean selfIsExploiting = isExploiting();
        boolean targetIsExploiting = targetFighter.isExploiting();
        return selfMode == MistAttackModeEnum.EAME_Attack_VALUE
                || targetMode == MistAttackModeEnum.EAME_Attack_VALUE
                || (selfIsExploiting && targetMode == MistAttackModeEnum.EAME_Plunder_VALUE)
                || (selfMode == MistAttackModeEnum.EAME_Plunder_VALUE && targetIsExploiting);
    }

    public boolean enterPvpBattle(MistFighter target, ProtoVector posVec, int battlingSide) {
        if (target == null) {
            return false;
        }
        MistPlayer player = getOwnerPlayerInSameRoom();
        if (player == null) {
            return false;
        }
        if (isBattling()) {
            return false;
        }
        long curTime = GlobalTick.getInstance().getCurrentTime();
        long posLong = MistConst.protoPosToLongPos(posVec);
        setAttribute(MistUnitPropTypeEnum.MUPT_BattlingPos_VALUE, posLong);
        setAttribute(MistUnitPropTypeEnum.MUPT_BattlingTargetId_VALUE, target.getId());
        setAttribute(MistUnitPropTypeEnum.MUPT_BattlingSide_VALUE, battlingSide);

        setAttribute(MistUnitPropTypeEnum.MUPT_FightExpireTimestamp_VALUE, curTime + 3 * TimeUtil.MS_IN_A_S);

        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_BattlingPos_VALUE, posLong);
        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_BattlingTargetId_VALUE, target.getId()); // 须在battlingPos之后通知
        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_BattlingSide_VALUE, battlingSide);

        bufMachine.interruptBuffByType(MistBuffInterruptType.BeenPlayerTouched);

        bufMachine.pauseBuff(GlobalTick.getInstance().getCurrentTime());

        setBattleType(1);
        return true;
    }

    public void onPvpBattleSettle(boolean isWinner, long targetFighterId, boolean robBossKey, boolean terminate, boolean beatWantedPlayer, long jewelryCount, long lavaBadgeCount) {
        if (getBattleType() != 1) {
            LogUtil.error("settle pvp battle failed,player not in pvp battle,id=" + getId());
            return;
        }
        if (!isWinner) {
            setAttribute(MistUnitPropTypeEnum.MUPT_PlayerAttackMode_VALUE, MistAttackModeEnum.EAME_Peace_VALUE);
            addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_PlayerAttackMode_VALUE, MistAttackModeEnum.EAME_Peace_VALUE);
        }
        bufMachine.resumeBuffByTarget(GlobalTick.getInstance().getCurrentTime(), !isWinner); // pvp结算buff效果顺序在技能效果前
        MistFighter target = room.getObjManager().getMistObj(targetFighterId);
        int timing = isWinner ? MistSkillTiming.WinPvpBattle : MistSkillTiming.LossPvpBattle;
        HashMap<Integer, Long> params = new HashMap<>();
        if (beatWantedPlayer) {
            params.put(MistTriggerParamType.BeatWantedPlayerFlag, 1l);
        }
        params.put(MistTriggerParamType.SettleJewelryCount, jewelryCount);
        if (getRoom().getScheduleManager() != null && getRoom().getScheduleManager().isScheduleTypeOpen(MistScheduleTypeEnum.MSTE_HotDispute_VALUE)) {
            params.put(MistTriggerParamType.LavaBadgeCount, lavaBadgeCount);
        }
        skillMachine.triggerPassiveSkills(timing, target, params);

        if (isWinner) {
            if (target != null) {
                if (robBossKey) {
                    room.broadcastMsg(MsgIdEnum.SC_BattleCmd_VALUE,
                            room.buildMistTips(EnumMistTipsType.EMTT_KillPlayerWithKey_VALUE, this, target), true);
                }
                if (terminate) {
                    room.broadcastMsg(MsgIdEnum.SC_BattleCmd_VALUE,
                            room.buildMistTips(EnumMistTipsType.EMTT_TerminateKill_VALUE, this, target), true);
                }
            }
            ++continualKillCount;
            getNpcTask().doNpcTask(MistTaskTargetType.MTTT_KillPlayer_VALUE, 1, 0);
        } else {
            continualKillCount = 0;
        }

        if (continualKillCount > 0 && continualKillCount % MistConst.ContinualKillCount == 0) {
            room.broadcastMsg(MsgIdEnum.SC_BattleCmd_VALUE,
                    room.buildMistTips(EnumMistTipsType.EMTT_ContinualKill_VALUE, this, this), true);
        }

        setAttribute(MistUnitPropTypeEnum.MUPT_BattlingPos_VALUE, 0);
        setAttribute(MistUnitPropTypeEnum.MUPT_BattlingTargetId_VALUE, 0);
        setAttribute(MistUnitPropTypeEnum.MUPT_BattlingSide_VALUE, 0);

        setAttribute(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE, 0);

        setBattleType(0);

        MistPlayer owner = getOwnerPlayerInSameRoom();
        if (owner != null) {
            if (owner.isOnline()) {
                //            updateRevertRoomInfo();
                addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_BattlingPos_VALUE, 0);
                addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_BattlingTargetId_VALUE, 0);
                addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_BattlingSide_VALUE, 0);
                addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE, 0);
            }

            CS_GS_MistDirectSettleBattleData.Builder builder = CS_GS_MistDirectSettleBattleData.newBuilder();
            builder.setPlayerIdx(owner.getIdx());
            builder.setBattleType(BattleTypeEnum.BTE_PVP);
            builder.setIsWinner(isWinner);
            if (target != null) {
                MistPlayer targetPlayer = target.getOwnerPlayerInSameRoom();
                if (targetPlayer != null) {
                    builder.setTargetPlayerIdx(targetPlayer.getIdx());
                }
            }
            GlobalData.getInstance().sendMsgToServer(owner.getServerIndex(), MsgIdEnum.CS_GS_MistDirectSettleBattleData_VALUE, builder);
        }
    }

    public void autoSettleBattle() {
        if (!isBattling()) {
            return;
        }
//        Event dropEvent = Event.valueOf(EventType.ET_CalcPlayerDropItem, GameUtil.getDefaultEventSource(), GameUtil.getDefaultEventSource());
//        boolean isPkMode = getAttribute(MistUnitPropTypeEnum.MUPT_PlayerAttackMode_VALUE) == MistAttackModeEnum.EAME_Attack_VALUE;
//        dropEvent.pushParam(isPkMode, getOwnerPlayerInSameRoom(), null);
//        EventManager.getInstance().dispatchEvent(dropEvent);
        if (battleType == 1) {
            onPvpBattleSettle(false, 0,false, false, false, 0, 0);
        } else if (battleType == 2) {
            onPveBattleSettle(false, EnumMistPveBattleType.EMPBT_BossBattle_VALUE, 0, false);
        } else if (battleType == 3) {
            onPveBattleSettle(false, EnumMistPveBattleType.EMPBT_MonsterBattle_VALUE, 0, false);
        }
        Event event = Event.valueOf(EventType.ET_SetMistPlayerPetRemainHp, getRoom(), getOwnerPlayerInSameRoom());
        event.pushParam(1, Collections.emptyList());
        EventManager.getInstance().dispatchEvent(event);
    }

    public void directSettleSettle() {
        if (!isBattling()) {
            return;
        }
        if (battleType == 1) {
            directSettlePvpBattle();
        } else {
            directSettlePveBattle();
        }
    }

    protected void directSettlePveBattle() {
        MistPlayer player = getOwnerPlayerInSameRoom();
        if (player == null) {
            return;
        }
        long monsterId = getAttribute(MistUnitPropTypeEnum.MUPT_BattlingTargetId_VALUE);
        MistObject monster = room.getObjManager().getMistObj(monsterId);
        int fightCfgId = 0;
        if (null != monster) {
            fightCfgId = (int) monster.getAttribute(MistUnitPropTypeEnum.MUPT_MonsterFightCfgId_VALUE);
        }
        int decreaseHpRate = MistConst.directSettlePveBattle(this, fightCfgId);
        onPveBattleSettle(decreaseHpRate >= 0, EnumMistPveBattleType.EMPBT_MonsterBattle_VALUE, 0, true);

        Event event = Event.valueOf(EventType.ET_ChangePlayerHpRate, room, getOwnerPlayerInSameRoom());
        event.pushParam(decreaseHpRate >= 0 ? -decreaseHpRate : -1000);
        EventManager.getInstance().dispatchEvent(event);

        CS_GS_MistDirectSettleBattleData.Builder builder = CS_GS_MistDirectSettleBattleData.newBuilder();
        builder.setPlayerIdx(player.getIdx());
        builder.setBattleType(BattleTypeEnum.BTE_PVE);
        builder.setSubPveType(EnumMistPveBattleType.EMPBT_MonsterBattle);
        builder.setFightCfgId(fightCfgId);
        builder.setIsWinner(decreaseHpRate >= 0);
        GlobalData.getInstance().sendMsgToServer(player.getServerIndex(), MsgIdEnum.CS_GS_MistDirectSettleBattleData_VALUE, builder);
    }

    protected void directSettlePvpBattle() {
        MistPlayer owner = getOwnerPlayerInSameRoom();
        if (owner == null) {
            return;
        }
        long targetId = getAttribute(MistUnitPropTypeEnum.MUPT_BattlingTargetId_VALUE);
        MistFighter target = getRoom().getObjManager().getMistObj(targetId);
        int winnerCamp = MistConst.calcPvpPlayerFightPower(this, target);
        boolean robBossKey = false;
        boolean terminate = false;
        boolean beatWantedPlayer = false;
        long jewelryCount = 0;
        long lavaBadge = 0;
        if (winnerCamp == 1) {
            if (target != null) {
                robBossKey = target.getAttribute(MistUnitPropTypeEnum.MUPT_OwningKeyState_VALUE) > 0;
                terminate = target.getContinualKillCount() >= MistConst.ContinualKillCount;
                beatWantedPlayer = target.getAttribute(MistUnitPropTypeEnum.MUPT_IsWantedState_VALUE) > 0;
                jewelryCount = target.getAttribute(MistUnitPropTypeEnum.MUPT_JewelryCount_VALUE);
                lavaBadge = target.getAttribute(MistUnitPropTypeEnum.MUPT_LavaBadgeCount_VALUE);
            }
            // 先结算胜利方
            onPvpBattleSettle(true, targetId, robBossKey, terminate, beatWantedPlayer, jewelryCount, lavaBadge);
            if (target != null) {
                target.onPvpBattleSettle(false, getId(),false, false, false, 0, 0);
            }
        } else if (winnerCamp == 2) {
            robBossKey = getAttribute(MistUnitPropTypeEnum.MUPT_OwningKeyState_VALUE) > 0;
            terminate = getContinualKillCount() >= MistConst.ContinualKillCount;
            beatWantedPlayer = getAttribute(MistUnitPropTypeEnum.MUPT_IsWantedState_VALUE) > 0;
            jewelryCount = getAttribute(MistUnitPropTypeEnum.MUPT_JewelryCount_VALUE);
            lavaBadge = getAttribute(MistUnitPropTypeEnum.MUPT_LavaBadgeCount_VALUE);
            // 先结算胜利方
            if (target != null) {
                target.onPvpBattleSettle(true, getId(), robBossKey, terminate, beatWantedPlayer, jewelryCount, lavaBadge);
            }
            onPvpBattleSettle(false, targetId,false, false, false, 0, 0);
        } else { // 平局都算输
            onPvpBattleSettle(false, targetId,false, false, false, 0, 0);
            target.onPvpBattleSettle(false, getId(),false, false, false, 0, 0);
        }
    }

    public List<UnitMetadata> getVisibleMetaData() {
        MistObject object;
        List<UnitMetadata> metadataList = null;
        for (Long id : selfVisibleTargetList) {
            object = getRoom().getObjManager().getMistObj(id);
            if (object == null) {
                continue;
            }
            if (metadataList == null) {
                metadataList = new ArrayList<>();
            }
            metadataList.add(object.getMetaData(this));
        }
        return metadataList;
    }

    public void updateRevertRoomInfo() {
        MistPlayer owner = getOwnerPlayerInSameRoom();
        if (owner == null) {
            return;
        }
        CS_GS_MistRoomEnterInfo.Builder builder = CS_GS_MistRoomEnterInfo.newBuilder();
        builder.setPlayerIdx(owner.getIdx());
        MistForestRoomInfo.Builder initData = room.getRoomInitData(this, owner);
        List<UnitMetadata> selfObjList= getVisibleMetaData();
        if (selfObjList != null) {
            initData.addAllInitMetaData(selfObjList);
        }

        builder.setRoomInfo(initData);
        builder.setIsRevert(true);
        GlobalData.getInstance().sendMsgToServer(owner.getServerIndex(), MsgIdEnum.CS_GS_MistRoomEnterInfo_VALUE, builder);

        getRoom().sendBossActivityRankToPlayer(owner);
        LogUtil.info("Send RevertRoomInfo to player[" + owner.getIdx() + "]");

        AoiNode aoiNode = room.getWorldMap().getAoiNodeById(getAoiNodeKey());
        if (aoiNode != null) {
            aoiNode.onPlayerRevert(this);
        }

        if (getRoom().getMistRule() == EnumMistRuleKind.EMRK_GhostBuster_VALUE) {
            MistGhostBusterRoom ghostRoom = (MistGhostBusterRoom) getRoom();
            ghostRoom.updateGhostBustRoomState(owner);
        }
        if (getRoom().getScheduleManager() != null) {
            getRoom().getScheduleManager().updateAllScheduleData(owner);
        }
        getNpcTask().updateAllTask(owner);
    }

    public void dealAbsorbedBag() {
        if (absorbedBagIdList.isEmpty()) {
            return;
        }
        long curTime = GlobalTick.getInstance().getCurrentTime();
        if (curTime - lastDealBagTime < MistPlayerConstant.FighterDealBagInterval && absorbedBagIdList.size() < 50) { // 吸取超过50个 也处理一次
            return;
        }
        lastDealBagTime = curTime;
        MistPlayer owner = getOwnerPlayerInSameRoom();
        if (owner == null) {
            return;
        }
        PlayerLevelConfigObject plyLvCfg = PlayerLevelConfig.getByLevel(owner.getLevel());
        if (plyLvCfg == null) {
            return;
        }
        MistBagConfigObject bagCfg;
        int vipExtraRate = MistConst.getGainMistBagExtraRateByVipLv(owner.getVip());
        int multiRate = (int) getAttribute(MistUnitPropTypeEnum.MUPT_MultiRewardRate_VALUE);
        int dailyMaxCount = plyLvCfg.getLimitByRule(getRoom().getMistRule());
        int addCount = 0;
        int dailyCount = owner.getDailyBattleRewardCount();
        for (Entry<Long, Integer> entry : absorbedBagIdList.entrySet()) {
            bagCfg = MistBagConfig.getByBagid(entry.getValue());
            if (bagCfg == null || bagCfg.getRewardtype() != 3) {
                LogUtil.debug("bag cfg not found or reward is null");
                continue;
            }

//            int realExtraRate = bagCfg.getRewardtype() != 2 ? vipExtraRate + multiRate : vipExtraRate;
//            int addCount = bagCfg.getBagprogress() * (1000 + realExtraRate) / 1000;
            int realExtraRate = vipExtraRate + multiRate;
            int rewardCount = plyLvCfg.getMistrbagrewarcount() * (1000 + realExtraRate) / 1000;
            addCount += rewardCount;

            if (dailyCount + addCount >= dailyMaxCount) {
                addCount = dailyMaxCount - dailyCount;
                break;
            }
        }
        if (addCount > 0) {
            MistLootPackCarryConfigObject cfg = MistConst.getConfigIdByReward(RewardTypeEnum.RTE_Item_VALUE, plyLvCfg.getMistbattlerewarditemid());
            if (cfg != null) {
                Map<Integer, Integer> rewardMap = new HashMap<>();
                rewardMap.put(cfg.getId(), addCount);

                Event event = Event.valueOf(EventType.ET_GainMistCarryReward, room, owner);
                event.pushParam(rewardMap, false);
                EventManager.getInstance().dispatchEvent(event);
            }
        }
        absorbedBagIdList.clear();
    }

    public void updateSpeedUpState(long curTime) {
//        if (speedUpTime > curTime) {
//            return;
//        }
//        speedUpTime = 0;
    }

    public void sendClockCmd(long curTime) {
        if (sendClockTime > curTime) {
            return;
        }
//        if (speedUpTime > curTime) {
//            return;
//        }
        sendClockTime = curTime + MistConst.CheckClientClockTime;
        MistPlayer player = getOwnerPlayerInSameRoom();
        if (player == null) {
            return;
        }
        SC_BattleCmd.Builder builder = SC_BattleCmd.newBuilder();
        BattleCmdData.Builder cmdBuilder = BattleCmdData.newBuilder();
        cmdBuilder.setCMDType(MistBattleCmdEnum.MBC_Clock);
        builder.addCMDList(cmdBuilder);
        player.sendMsgToServer(MsgIdEnum.SC_BattleCmd_VALUE, builder);
    }

    protected void checkPvpMode() {
        if (room.isForcePvpState() && !isInSafeRegion() && MistAttackModeEnum.EAME_Attack_VALUE != getAttribute(MistUnitPropTypeEnum.MUPT_PlayerAttackMode_VALUE)) {
            setAttribute(MistUnitPropTypeEnum.MUPT_PlayerAttackMode_VALUE, MistAttackModeEnum.EAME_Attack_VALUE);
            addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_PlayerAttackMode_VALUE, MistAttackModeEnum.EAME_Attack_VALUE);
        }
    }

    public MistRetCode checkCanLeaveMistRoom() {
        if (isBattling() || getAttribute(MistUnitPropTypeEnum.MUPT_BeingTouchingState_VALUE) > 0) {
            return MistRetCode.MRC_Battling;
        }
//        if (MistAttackModeEnum.EAME_Attack_VALUE == getAttribute(MistUnitPropTypeEnum.MUPT_PlayerAttackMode_VALUE)) {
//            return MistRetCode.MRC_AttackModeCannotLeave;
//        }
        if (getAttribute(MistUnitPropTypeEnum.MUPT_OwningKeyState_VALUE) > 0) {
            return MistRetCode.MRC_OwningKeyCannotLeave;
        }
        return MistRetCode.MRC_Success;
    }

    public RobotController getRobController() {
        return robController;
    }

    public void changeJewelryCount(long newCount) {
        long oldCount = getAttribute(MistUnitPropTypeEnum.MUPT_JewelryCount_VALUE);
//        long maxCount = GameConfig.getById(GameConst.ConfigId).getMistsummonnum();
        newCount = Math.max(0, newCount);
        if (newCount == oldCount) {
            return;
        }
        setAttribute(MistUnitPropTypeEnum.MUPT_JewelryCount_VALUE, newCount);
        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_JewelryCount_VALUE, newCount);

        MistPlayer owner = getOwnerPlayerInSameRoom();
        if (null == owner) {
            return;
        }
        CS_GS_UpdateJewelryCountData.Builder builder = CS_GS_UpdateJewelryCountData.newBuilder();
        builder.setPlayerIdx(owner.getIdx());
        builder.setMistLevel(getRoom().getLevel());
        builder.setNewJewelryCount((int) newCount);
        GlobalData.getInstance().sendMsgToServer(owner.getServerIndex(), MsgIdEnum.CS_GS_UpdateJewelryCountData_VALUE, builder);
    }

    public void summonEvilMonster() {
        MistPlayer player = getOwnerPlayerInSameRoom();
        if (player == null) {
            return;
        }
        MistCommonConfigObject cfg = MistCommonConfig.getByMistlevel(room.getLevel());
        if (cfg == null) {
            return;
        }
        if (cfg.getHiddenevilgroup() == null || cfg.getHiddenevilgroup().length <= 0) {
            return;
        }
        int cfgId = cfg.getHiddenevilgroup()[RandomUtils.nextInt(cfg.getHiddenevilgroup().length)];
        int needSummonCount = GameConfig.getById(GameConst.ConfigId).getMistsummonnum();
        CrossArenaLvCfgObject crossVipLvCfg = CrossArenaLvCfg.getByLv(player.getCrossVipLv());
        if (crossVipLvCfg != null) {
            needSummonCount -= crossVipLvCfg.getMist_callbossreduction();
            needSummonCount = Math.max(0, needSummonCount);

            int [] sumBossData = crossVipLvCfg.getMistsummonbossextidlist();
            if (sumBossData != null && sumBossData.length > 1) { // 随机vip的召唤配置
                if (crossVipLvCfg.getMistsummonbossodds() > RandomUtils.nextInt(1000)) {
                    int index = RandomUtils.nextInt(sumBossData.length );
                    cfgId = sumBossData[index];
                }
            }
        }

        MistJewelryConfigObject jewelryCfg = MistJewelryConfig.getById(cfgId);
        if (jewelryCfg == null) {
            LogUtil.error("Hidden jewelryCfg not found, roomLevel={},cfgId={}", room.getLevel(), cfgId);
            return;
        }
        long summonCount = getAttribute(MistUnitPropTypeEnum.MUPT_JewelryCount_VALUE);
        if (getAttribute(MistUnitPropTypeEnum.MUPT_JewelryCount_VALUE) < needSummonCount) {
            player.sendRetCodeMsg(RetCodeEnum.RCE_MatieralNotEnough);
            return;
        }
        changeJewelryCount(summonCount - needSummonCount);
        changeHiddenEvilState(jewelryCfg);
    }

    public void changeHiddenEvilState(MistJewelryConfigObject cfg) {
        MistPlayer owner = getOwnerPlayerInSameRoom();
        if (owner == null) {
            return;
        }
        int cfgId = cfg != null ? cfg.getId() : 0;
        long expireTime = cfg != null ? GlobalTick.getInstance().getCurrentTime() + cfg.getDuration() * TimeUtil.MS_IN_A_S : 0;
        setAttribute(MistUnitPropTypeEnum.MUPT_HiddenEvilId_VALUE, cfgId);
        setAttribute(MistUnitPropTypeEnum.MUPT_HiddenEvilExpireTime_VALUE, expireTime);

        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_HiddenEvilId_VALUE, cfgId);
        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_HiddenEvilExpireTime_VALUE, expireTime);

        CS_GS_UpdateHiddenEvilData.Builder builder = CS_GS_UpdateHiddenEvilData.newBuilder();
        builder.setPlayerIdx(owner.getIdx());
        builder.setHiddenEvilId(cfgId);
        builder.setHiddenEvilExpireTime(expireTime);
        GlobalData.getInstance().sendMsgToServer(owner.getServerIndex(), MsgIdEnum.CS_GS_UpdateHiddenEvilData_VALUE, builder);
    }

    protected void checkHiddenEvilExpire(long curTime) {
        if (isBattling()) {
            return;
        }

        long expireTime = getAttribute(MistUnitPropTypeEnum.MUPT_HiddenEvilExpireTime_VALUE);
        if (expireTime == 0 || expireTime > curTime) {
            return;
        }
        changeHiddenEvilState(null);
    }

    public void doMistTargetProg(TargetTypeEnum targetType, int param, int addProg) {
        MistPlayer player = getOwnerPlayerInSameRoom();
        if (player == null) {
            return;
        }
        CS_GS_MistTargetMissionData.Builder builder = CS_GS_MistTargetMissionData.newBuilder();
        builder.setPlayerIdx(player.getIdx());
        builder.setTargetType(targetType);
        builder.setParam(param);
        builder.setAddProg(addProg);
        GlobalData.getInstance().sendMsgToServer(player.getServerIndex(), MsgIdEnum.CS_GS_MistTargetMissionData_VALUE, builder);
    }

    public void changeLavaBadge(long changeVal) {
        MistPlayer player = getOwnerPlayerInSameRoom();
        if (player == null) {
            return;
        }
        if (getRoom().getScheduleManager() == null) {
            return;
        }
        if (!getRoom().getScheduleManager().isScheduleTypeOpen(MistScheduleTypeEnum.MSTE_HotDispute_VALUE)) {
            return;
        }
        int combineCount = CrossConstConfig.getById(GameConst.ConfigId).getLavabadgecombinenum();
        if (combineCount > 0 && changeVal >= combineCount) {
            for (int i = 0; i < changeVal / combineCount; i++) {
                int rewardId = gainLavaBadge();
                SC_LavaBadgeCombine.Builder builder = SC_LavaBadgeCombine.newBuilder();
                builder.setRewardCfgId(rewardId);
                player.sendMsgToServer(MsgIdEnum.SC_LavaBadgeCombine_VALUE, builder);

                changeVal -= combineCount;
            }
        }
        setAttribute(MistUnitPropTypeEnum.MUPT_LavaBadgeCount_VALUE, changeVal);
        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_LavaBadgeCount_VALUE, changeVal);
    }

    public int gainLavaBadge() {
        int[][] rewardData = CrossConstConfig.getById(GameConst.ConfigId).getLavabadgerandreward();
        if (rewardData == null || rewardData.length <= 0) {
            return 0;
        }
        int sum = 0;
        int rand = RandomUtils.nextInt(1000);
        for (int i = 0; i < rewardData.length; i++) {
            if (rewardData[i] == null || rewardData[i].length < 2) {
                continue;
            }
            sum += rewardData[i][0];
            if (rand < sum) {
                gainReward(rewardData[i][1]);
                return rewardData[i][1];
            }
        }
        return 0;
    }

    public void changeToWaitingForRebornState() {
        MistPlayer owner = getOwnerPlayerInSameRoom();
        if (owner == null) {
            return;
        }
        if (skillMachine.getVipSkillByType(MistVipSkillType.RebornInSituPlace) != null) {
            getBufMachine().addBuff(MistConst.MistVipRebornBuffId, this, null);
        } else {
            rebornForDefeated(true, false);
        }
    }

    public void rebornForDefeated(boolean toSafeRegion, boolean maxHp) {
        MistPlayer owner = getOwnerPlayerInSameRoom();
        if (owner == null) {
            return;
        }
        if (toSafeRegion) {
            MistBornPosInfo posObj = room.getObjGenerator().getRandomBornPosObj(getType(), true, false);
            if (posObj != null) {
                int oldX = getPos().getX();
                int oldY = getPos().getY();
                setPos(posObj.getPos());
                room.getWorldMap().objMove(this, oldX, oldY);
                addChangePosInfoCmd(getPos().build(), getToward().build());
            }
        }
        int remainHp = maxHp ? GameConst.PetMaxHpRate : 0;
        Event event = Event.valueOf(EventType.ET_ChangePlayerHpRate, room, owner);
        event.pushParam(remainHp);
        EventManager.getInstance().dispatchEvent(event);

        getBufMachine().addBuff(MistConst.MistRebornProtectedBuffId, this, null);
    }

    public void changeSearchShowObjsState(boolean bOpen) {
        MistPlayer owner = getOwnerPlayerInSameRoom();
        if (bOpen) {
            setAttribute(MistUnitPropTypeEnum.MUPT_UsingShowObjState_VALUE, 1);
            getRoom().getObjManager().addShowTreasureFighter(getId());
            if (owner == null) {
                return;
            }
            List<MistShowData> objList = getRoom().getObjManager().getAllShowObjs();
            if (objList != null) {
                SC_UpdateMistShowData.Builder builder = SC_UpdateMistShowData.newBuilder();
                builder.addAllShowData(objList);
                owner.sendMsgToServer(MsgIdEnum.SC_UpdateMistShowData_VALUE, builder);
            }
        } else {
            setAttribute(MistUnitPropTypeEnum.MUPT_UsingShowObjState_VALUE, 0);
            getRoom().getObjManager().removeShowTreasureFighter(getId());
            if (owner == null) {
                return;
            }
            owner.sendMsgToServer(MsgIdEnum.SC_UpdateMistShowData_VALUE, SC_UpdateMistShowData.newBuilder());
        }
    }

    public void addMoveEffectBuff(int moveEffectId, boolean isInit) {
        MistMoveEffectConfigObject cfg = MistMoveEffectConfig.getById(moveEffectId);
        if (cfg == null) {
            return;
        }
        int curMoveEffectId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_MoveEffectId_VALUE);
        if (curMoveEffectId > 0) {
            getBufMachine().interruptBuffByType(MistBuffInterruptType.BackToSafeRegion);
        }
        if (cfg.getExtendbufflist() != null) {
            for (Integer buffId : cfg.getExtendbufflist()) {
                getBufMachine().addBuff(buffId, this, null);
            }
        }
        setAttribute(MistUnitPropTypeEnum.MUPT_MoveEffectId_VALUE, moveEffectId);
        if (!isInit) {
            addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_MoveEffectId_VALUE, moveEffectId);
        }
    }

    @Override
    public void onTick(long curTime) {
        if (battleType > 0 && isBattling()) {
            MistPlayer owner = getOwnerPlayerInSameRoom();
            if (owner == null || (!owner.isOnline() && owner.getOfflineTime() + room.getMaxBattleTime() < curTime)) {
                autoSettleBattle();
            } else {
                long directFightExpireTime = getAttribute(MistUnitPropTypeEnum.MUPT_FightExpireTimestamp_VALUE);
                if (directFightExpireTime > 0 && directFightExpireTime <= curTime) {
                    directSettleSettle();
                }
            }
        }
        checkHiddenEvilExpire(curTime);
        checkPvpMode();
        skillMachine.onTick(curTime);
        dealAbsorbedBag();
        super.onTick(curTime);
        updateSpeedUpState(curTime);
        sendClockCmd(curTime);
        npcTask.onTick(curTime);

        MistPlayer player = getOwnerPlayerInSameRoom();
        if (player == null || player.getFighterId() != getId()) {
            if (isAlive()) {
                dead();
            }
        } else if (!player.isOnline() && player.getOfflineTime() > 0 && curTime - player.getOfflineTime() > MistConst.MaxOfflineTime) {
            MistRetCode retCode = room.onPlayerExit(player, false);
            if (retCode == MistRetCode.MRC_Success) {
                Event event = Event.valueOf(EventType.ET_Logout, room, player);
                event.pushParam(true);
                EventManager.getInstance().dispatchEvent(event);
            }
        }
    }
}

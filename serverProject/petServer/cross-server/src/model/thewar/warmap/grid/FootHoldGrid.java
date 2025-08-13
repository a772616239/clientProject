package model.thewar.warmap.grid;

import cfg.TheWarConstConfig;
import cfg.TheWarGridStationConfig;
import cfg.TheWarGridStationConfigObject;
import cfg.TheWarJobTileConfig;
import cfg.TheWarJobTileConfigObject;
import common.GameConst;
import common.GameConst.EventType;
import common.GlobalTick;
import datatool.StringHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import model.thewar.WarConst;
import model.thewar.warmap.WarMapData;
import model.thewar.warmap.WarMapManager;
import model.thewar.warplayer.dbCache.WarPlayerCache;
import model.thewar.warplayer.entity.WarPlayer;
import model.thewar.warroom.dbCache.WarRoomCache;
import model.thewar.warroom.entity.WarRoom;
import org.apache.commons.collections4.CollectionUtils;
import protocol.Battle.BattleRemainPet;
import protocol.Battle.ExtendProperty;
import protocol.Battle.PetBuffData;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer.CS_GS_EnterTheWarBattle;
import protocol.ServerTransfer.CS_GS_TheWarGridBeenOccupiedLog;
import protocol.ServerTransfer.CS_GS_TheWarUpdateOwnedGridData;
import protocol.TheWar.EnumTheWarTips;
import protocol.TheWar.SC_QueryGridBattleData;
import protocol.TheWar.StationTroopsInfo;
import protocol.TheWar.WarReward;
import protocol.TheWarDB.GridCacheData;
import protocol.TheWarDefine.Position;
import protocol.TheWarDefine.TheWarCellPropertyEnum;
import protocol.TheWarDefine.TheWarRetCode;
import protocol.TheWarDefine.WarPetData;
import protocol.TheWarDefine.WarTeamType;
import server.event.Event;
import server.event.EventManager;
import util.GameUtil;
import util.TimeUtil;

public class FootHoldGrid extends WarMapGrid {
    protected Map<String, Integer> monsterPetHpMap = new HashMap<>(); // 野怪当前血量信息

    protected String troopsPetIdx = "";
    protected long troopsTime = 0;

//    protected Map<Integer, WarGridTroopsInfo> stationTroopsPetMap;

    public String getTroopsPetIdx() {
        return troopsPetIdx;
    }

    public void setTroopsPetIdx(String troopsPetIdx) {
        this.troopsPetIdx = troopsPetIdx;
    }

    public long getTroopsTime() {
        return troopsTime;
    }

    public void setTroopsTime(long troopsTime) {
        this.troopsTime = troopsTime;
    }

    public void refreshFightMakeId() {
        int fightMakeCfgId = (int) WarGridDefaultProp.getDefaultPropVal(getName(), TheWarCellPropertyEnum.TWCP_FightMakeCfgId_VALUE);
        int fightMakeId = WarConst.getFightMakeIdByCfgId(fightMakeCfgId);
        setPropValue(TheWarCellPropertyEnum.TWCP_FightMakeCfgId_VALUE, fightMakeId);
    }

    public RetCodeEnum playerAttackGrid(WarPlayer warPlayer, boolean skipBattle) {
        if (isBlock()) {
            return RetCodeEnum.RCE_TheWar_BlockGrid; // 阻挡格子
        }
        TheWarJobTileConfigObject cfg = TheWarJobTileConfig.getById(warPlayer.getJobTileLevel());
        if (cfg == null) {
            return RetCodeEnum.RCE_TheWar_JobTileCfgError; // 职位配置未找到，无法获取最大可占领格子数
        }
        if (warPlayer.getPlayerData().getOwnedGridPosCount() >= cfg.getMaxoccupygirdcount()) {
            return RetCodeEnum.RCE_TheWar_LimitOccupyGridNum; // 达到当前职位最大占领格子数
        }
        if (getPropValue(TheWarCellPropertyEnum.TWCP_PlayerSpawn_VALUE) > 0) {
            return RetCodeEnum.RCE_TheWar_TargetGridIsPlayerSpawn; // 玩家出生点不可占
        }
        if (getPropValue(TheWarCellPropertyEnum.TWCP_BattlingTarget_VALUE) > 0) {
            return RetCodeEnum.RCE_TheWar_OtherPlayerAttacking; // 其他玩家正在进攻中，无法占据
        }
        long protectTime = getPropValue(TheWarCellPropertyEnum.TWCP_CellProtectTime_VALUE);
        if (getPropValue(TheWarCellPropertyEnum.TWCP_OccupyTime_VALUE) + protectTime * TimeUtil.MS_IN_A_S > GlobalTick.getInstance().getCurrentTime()) {
            return RetCodeEnum.RCE_TheWar_OccupyProtecting; // 进攻冷却中
        }
        long ownerId = getPropValue(TheWarCellPropertyEnum.TWCP_OccupierPlayerId_VALUE);
        WarPlayer owner = WarPlayerCache.getInstance().queryObject(String.valueOf(ownerId));
        if (owner != null && owner.getCamp() == warPlayer.getCamp()) {
            return RetCodeEnum.RCE_TheWar_TeamGrid; // 队友已占据该据点，无法占据
        }
        int energy = owner != null ? (int) getPropValue(TheWarCellPropertyEnum.TWCP_OccupyPlayerNeedEnergy_VALUE)
                : (int) getPropValue(TheWarCellPropertyEnum.TWCP_OccupyMonsterNeedEnergy_VALUE);
        if (warPlayer.getPlayerData().getStamina() < energy) {
            return RetCodeEnum.RCE_TheWar_StaminaNotEnough; // 玩家体力不足，无法占领
        }
        List<WarPetData> petList = warPlayer.getTeamWarPets(WarTeamType.WTT_AttackTeam_VALUE);
        if (CollectionUtils.isEmpty(petList)) {
            return RetCodeEnum.RCE_TheWar_EmptyPetTeam; // 空队伍
        }
        WarRoom warRoom = WarRoomCache.getInstance().queryObject(warPlayer.getRoomIdx());
        if (warRoom != null) {
            if (owner != null) {
                warRoom.broadcastTips(EnumTheWarTips.EMTW_AttackPlayerGrid_VALUE, false, warPlayer.getIdx(), owner.getIdx(), getPos().getX(), getPos().getY());

                warRoom.broadcastMarquee(TheWarConstConfig.getById(GameConst.ConfigId).getAttackenemygridmarqueeid(), warPlayer.getCamp(),
                        warPlayer.getName(), owner.getCamp(), owner.getName(), getPos().getX(), getPos().getY());
            } else {
                warRoom.broadcastTips(EnumTheWarTips.EMTW_AttackMonsterGrid_VALUE, true, warPlayer.getIdx(), getPos().getX(), getPos().getY());
            }
        }

        Event event = Event.valueOf(EventType.ET_TheWar_ChangePlayerStamina, this, warPlayer);
        event.pushParam(false, energy);
        EventManager.getInstance().dispatchEvent(event);
        int fightMakeId = (int) getPropValue(TheWarCellPropertyEnum.TWCP_FightMakeCfgId_VALUE);
//        int fightMakeId = owner != null ? (int) getPropValue(TheWarCellPropertyEnum.TWCP_PvpFightMakeId_VALUE) : (int) getPropValue(TheWarCellPropertyEnum.TWCP_FightMakeCfgId_VALUE);
        if (fightMakeId > 0) {
            setPropValue(TheWarCellPropertyEnum.TWCP_BattlingTarget_VALUE, GameUtil.stringToLong(warPlayer.getIdx(), 0));

            CS_GS_EnterTheWarBattle.Builder builder = CS_GS_EnterTheWarBattle.newBuilder();
            builder.setPlayerIdx(warPlayer.getIdx());
            builder.setBattleGridPos(getPos());
            builder.setFightMakeId(fightMakeId);
            builder.setSkipBattle(skipBattle);
            if (owner != null) { // 攻击其他玩家镜像
                builder.setTargetPlayerInfo(owner.buildPlayerBaseInfo());

                WarPetData troopPet = owner.getWarPetData(getTroopsPetIdx());
                if (troopPet != null) {
//                    ExtendProperty.Builder extBuilder = owner.buildExtendBattleInfo(2);
                    ExtendProperty.Builder extBuilder = ExtendProperty.newBuilder();
                    extBuilder.setCamp(2);
                    long buffCfgId = getPropValue(TheWarCellPropertyEnum.TWCP_StationTroopBuffCfgIg_VALUE);
                    List<Integer> buffList = TheWarGridStationConfig.getInstance().getTroopsBuffListByPetInfo((int) buffCfgId, troopPet.getPetQuality(), troopPet.getPetCfgId());
                    if (buffList != null) {
                        for (Integer buffId : buffList) {
                            extBuilder.addBuffData(PetBuffData.newBuilder().setBuffCfgId(buffId).setBuffCount(1));
                        }
                    }
                    builder.addExtendProp(extBuilder); // 附加buff,敌方阵营为2
                }
            }
            builder.addAllRemainMonsters(buildMonsterPetData());

            builder.addAllSelfPetData(warPlayer.getTeamBattlePets(WarTeamType.WTT_AttackTeam_VALUE));
            builder.addAllSelfSkillData(warPlayer.getTeamSkillDict(WarTeamType.WTT_AttackTeam_VALUE));
            builder.addExtendProp(warPlayer.buildExtendBattleInfo(1)); // 附加buff,本方阵营为1

            warPlayer.sendMsgToServer(MsgIdEnum.CS_GS_EnterTheWarBattle_VALUE, builder);
            broadcastPropData();

            Event battleEvent = Event.valueOf(EventType.ET_TheWar_AddPlayerBattleState, this, warPlayer);
            battleEvent.pushParam(true);
            EventManager.getInstance().dispatchEvent(battleEvent);
        } else { // 配置为0视为占据成功
            settleBattle(warPlayer, true, null, 0);
        }

        return RetCodeEnum.RCE_Success;
    }

    public void settleBattle(WarPlayer warPlayer, boolean attackResult, List<BattleRemainPet> remainPets, int fightStar) {
        List<BattleRemainPet> attackerRemainPets = new ArrayList<>();
        List<BattleRemainPet> defenderRemainPets = new ArrayList<>();
        if (!CollectionUtils.isEmpty(remainPets)) {
            for (BattleRemainPet remainPet : remainPets) {
                if (remainPet.getCamp() == 1) {
                    attackerRemainPets.add(remainPet);
                } else if (remainPet.getCamp() == 2) {
                    defenderRemainPets.add(remainPet);
                }
            }
        }

        if (!attackResult) {
            for (BattleRemainPet remainPet : defenderRemainPets) {
                monsterPetHpMap.put(remainPet.getPetId(), remainPet.getRemainHpRate());
            }
        } else {
            occupySuccessSettle(warPlayer);
        }
        if (!CollectionUtils.isEmpty(attackerRemainPets)) {
            Event remainPetHpEvent = Event.valueOf(EventType.ET_TheWar_UpdateRemainPetHp, this, warPlayer); // 保留进攻玩家血量
            remainPetHpEvent.pushParam(attackerRemainPets);
            EventManager.getInstance().dispatchEvent(remainPetHpEvent);
        }

        setPropValue(TheWarCellPropertyEnum.TWCP_BattlingTarget_VALUE, 0);
        broadcastPropData();

        Event rewardEvent = Event.valueOf(EventType.ET_TheWar_SettleBattleReward, this, warPlayer);
        WarPlayer ownerPlayer = null;
        long ownerId = getPropValue(TheWarCellPropertyEnum.TWCP_OccupierPlayerId_VALUE);
        if (ownerId > 0) {
            ownerPlayer = WarPlayerCache.getInstance().queryObject(GameUtil.longToString(ownerId, ""));
        }
        String ownerName = ownerPlayer != null ? ownerPlayer.getName() : "";
        boolean hasTroops = ownerPlayer != null && ownerPlayer.getWarPetData(troopsPetIdx) != null;
//        boolean hasTroops = stationTroopsPetMap != null && !stationTroopsPetMap.isEmpty();
        rewardEvent.pushParam(attackResult, fightStar, ownerName, hasTroops);
        EventManager.getInstance().dispatchEvent(rewardEvent);
    }

    protected void occupySuccessSettle(WarPlayer warPlayer) {
        long ownerId = getPropValue(TheWarCellPropertyEnum.TWCP_OccupierPlayerId_VALUE);
        WarPlayer owner = WarPlayerCache.getInstance().queryObject(GameUtil.longToString(ownerId, ""));
        if (owner != null) {
            CS_GS_TheWarGridBeenOccupiedLog.Builder builder = CS_GS_TheWarGridBeenOccupiedLog.newBuilder();
            builder.setPlayerIdx(owner.getIdx());
            builder.setAttackerName(warPlayer.getName());
            builder.getGridDataBuilder().setPos(getPos());
            builder.getGridDataBuilder().setGridType((int) getPropValue(TheWarCellPropertyEnum.TWCP_CellTag_VALUE));
            builder.getGridDataBuilder().setGridLevel((int) getPropValue(TheWarCellPropertyEnum.TWCP_Level_VALUE));
            builder.getGridDataBuilder().setHasTrooped(!StringHelper.isNull(getTroopsPetIdx()));

            clearOwnedPosByEnemy(owner);
        }

        long curTime = GlobalTick.getInstance().getCurrentTime();
        long playerId = GameUtil.stringToLong(warPlayer.getIdx(), 0);
        setPropValue(TheWarCellPropertyEnum.TWCP_OccupyTime_VALUE, curTime);
        setPropValue(TheWarCellPropertyEnum.TWCP_OccupierPlayerId_VALUE, playerId);
        setPropValue(TheWarCellPropertyEnum.TWCP_LastSettleAfkTime_VALUE, curTime);

        setPropValue(TheWarCellPropertyEnum.TWCP_Camp_VALUE, warPlayer.getCamp());
        Event addGridEvent = Event.valueOf(EventType.ET_TheWar_AddFootHoldGrid, this, warPlayer);
        String ownerName = owner != null ? owner.getName() : "";
        boolean hasTroops = owner != null && owner.getWarPetData(troopsPetIdx) != null;
//        boolean hasTroops = stationTroopsPetMap != null && !stationTroopsPetMap.isEmpty();
        addGridEvent.pushParam(ownerName, hasTroops);
        EventManager.getInstance().dispatchEvent(addGridEvent);

        WarRoom warRoom = WarRoomCache.getInstance().queryObject(getRoomIdx());
        if (warRoom != null) {
            Event addPosGroupGridEvent = Event.valueOf(EventType.ET_TheWar_AddPosGroupGrid, warPlayer, warRoom);
            addPosGroupGridEvent.pushParam(this);
            EventManager.getInstance().dispatchEvent(addPosGroupGridEvent);

            if (owner != null) {
                warRoom.broadcastTips(EnumTheWarTips.EMTW_OccupyPlayerGrid_VALUE, false, warPlayer.getIdx(), owner.getIdx(), getPos().getX(), getPos().getY());
                warRoom.broadcastMarquee(TheWarConstConfig.getById(GameConst.ConfigId).getOccupyenemygridmarqueeid(), warPlayer.getCamp(),
                        warPlayer.getName(), owner.getCamp(), owner.getName(), getPos().getX(), getPos().getY());
            } else {
                warRoom.broadcastTips(EnumTheWarTips.EMTW_OccupyMonsterGrid_VALUE, true, warPlayer.getIdx(), getPos().getX(), getPos().getY());
            }

            Event recordEvent = Event.valueOf(EventType.ET_TheWar_AddWarGridRecord, this, GameUtil.getDefaultEventSource());
            recordEvent.pushParam(warPlayer, owner);
            EventManager.getInstance().dispatchEvent(recordEvent);
        }

        monsterPetHpMap.clear();
    }

    protected void bossUnlockTargetPos() {
        if (getPropValue(TheWarCellPropertyEnum.TWCP_BossUnlockTargetPosFlag_VALUE) <= 0) {
            return;
        }
        Position.Builder unlockPos = Position.newBuilder();
        long posLongVal = getPropValue(TheWarCellPropertyEnum.TWCP_BossUnlockTargetPos_VALUE);
        unlockPos.setX((int) (posLongVal >>> 32));  // 高32位x
        unlockPos.setY((int) posLongVal);           // 低32位y
        WarMapData mapData = WarMapManager.getInstance().getRoomMapData(getRoomIdx());
        if (mapData == null) {
            return;
        }
        WarMapGrid grid = mapData.getMapGridByPos(unlockPos.build());
        if (grid == null || !grid.isBlock()) {
            return;
        }
        // 解锁目标格子
        Event event = Event.valueOf(EventType.ET_TheWar_ChangeTargetGridProperty, this, grid);
        event.pushParam(TheWarCellPropertyEnum.TWCP_IsBlock_VALUE, 0l);
        EventManager.getInstance().dispatchEvent(event);
    }

    public TheWarRetCode preClearOwnedGrid(WarPlayer player) {
        if (isBlock()) {
            return TheWarRetCode.TWRC_GridIsBlock; // 阻挡格子
        }
        if (getPropValue(TheWarCellPropertyEnum.TWCP_BossMaxHp_VALUE) > 0) {
            return TheWarRetCode.TWRC_CannotClearBossGrid; // 不能清除boss点
        }
        if (getPropValue(TheWarCellPropertyEnum.TWCP_PlayerSpawn_VALUE) > 0) {
            return TheWarRetCode.TWRC_CannotClearBornGrid; // 不能清除出生点
        }
        long playerId = GameUtil.stringToLong(player.getIdx(), 0);
        if (getPropValue(TheWarCellPropertyEnum.TWCP_OccupierPlayerId_VALUE) != playerId) {
            return TheWarRetCode.TWRC_NotOccupiedGrid; // 未占领该格子
        }
        if (getPropValue(TheWarCellPropertyEnum.TWCP_BattlingTarget_VALUE) > 0) {
            return TheWarRetCode.TWRC_GridIsUnderAttack; // 格子被攻击中
        }
        if (getPropValue(TheWarCellPropertyEnum.TWCP_RealClearTimeStamp_VALUE) > 0) {
            return TheWarRetCode.TWRC_GridClearing; // 格子清除中
        }
        return TheWarRetCode.TWRC_Success;
    }

    public void clearOwnedPosBySelf(WarPlayer player) {
        long playerId = GameUtil.stringToLong(player.getIdx(), 0);
        if (getPropValue(TheWarCellPropertyEnum.TWCP_OccupierPlayerId_VALUE) != playerId) {
            return; // 未占领该格子
        }
        settleClearGird(player);
    }

    public void clearOwnedPosByEnemy(WarPlayer player) {
        long playerId = GameUtil.stringToLong(player.getIdx(), 0);
        if (getPropValue(TheWarCellPropertyEnum.TWCP_OccupierPlayerId_VALUE) != playerId) {
            return; // 未占领该格子
        }

        long curTime = GlobalTick.getInstance().getCurrentTime();
        Event event = Event.valueOf(EventType.ET_TheWar_AddUnsettledCurrency, this, player);
        event.pushParam(true, calcRewardGold(curTime), calcRewardDp(curTime), calcRewardHolyWater(curTime));
        event.pushParam((int) getPropValue(TheWarCellPropertyEnum.TWCP_DropItemCfgId_VALUE), getAfkBonusTime(curTime));
        EventManager.getInstance().dispatchEvent(event);

        settleClearGird(player);
    }

    protected void settleClearGird(WarPlayer player) {
        WarRoom warRoom = WarRoomCache.getInstance().queryObject(getRoomIdx());
        if (warRoom != null) {
            Event event1 = Event.valueOf(EventType.ET_TheWar_RemovePosGroupGrid, player, warRoom);
            event1.pushParam(this);
            EventManager.getInstance().dispatchEvent(event1);
        }

        CS_GS_TheWarUpdateOwnedGridData.Builder builder = CS_GS_TheWarUpdateOwnedGridData.newBuilder();
        builder.setBAdd(false);
        builder.setPlayerIdx(getIdx());
        builder.getGridDataBuilder().setPos(getPos());
        player.sendMsgToServer(MsgIdEnum.CS_GS_TheWarUpdateOwnedGridData_VALUE, builder);

        clearMonsterAfkReward();

        clearStationTroopsPet(player, false);

        setPropValue(TheWarCellPropertyEnum.TWCP_Camp_VALUE, 0);
        setPropValue(TheWarCellPropertyEnum.TWCP_OccupierPlayerId_VALUE, 0);
        setPropValue(TheWarCellPropertyEnum.TWCP_OccupyTime_VALUE, 0);
        setPropValue(TheWarCellPropertyEnum.TWCP_LastSettleAfkTime_VALUE, 0);
        setPropDefaultValue(TheWarCellPropertyEnum.TWCP_RealClearTimeStamp_VALUE);

        broadcastPropData();
    }


    public TheWarRetCode stationTroopsGrid(WarPlayer player, StationTroopsInfo troopsInfo) {
        if (!StringHelper.isNull(troopsPetIdx)) {
            if (troopsPetIdx.equals(troopsInfo.getPetIdx())) {
                return TheWarRetCode.TWRC_PetAlreadyStationTroops;
            } else {
                clearStationTroopsPet(player, true);
            }
        } else {
            CS_GS_TheWarUpdateOwnedGridData.Builder builder = CS_GS_TheWarUpdateOwnedGridData.newBuilder();
            builder.setBAdd(true);
            builder.setPlayerIdx(getIdx());
            builder.getGridDataBuilder().setPos(getPos());
            builder.getGridDataBuilder().setGridType((int) getPropValue(TheWarCellPropertyEnum.TWCP_CellTag_VALUE));
            builder.getGridDataBuilder().setGridLevel((int) getPropValue(TheWarCellPropertyEnum.TWCP_Level_VALUE));
            builder.getGridDataBuilder().setHasTrooped(true);
            player.sendMsgToServer(MsgIdEnum.CS_GS_TheWarUpdateOwnedGridData_VALUE, builder);
        }

        setTroopsPetIdx(troopsInfo.getPetIdx());
        setTroopsTime(GlobalTick.getInstance().getCurrentTime());

        Event event = Event.valueOf(EventType.ET_TheWar_ModifyPetTroopsData, this, player);
        event.pushParam(troopsInfo);
        EventManager.getInstance().dispatchEvent(event);


        return TheWarRetCode.TWRC_Success;
    }

    public void settlePetTroopsReward(WarPlayer player) {
        if (StringHelper.isNull(troopsPetIdx)) {
            return;
        }
        int unclaimedGold = 0;
        int unclaimedDP = 0;
        int unclaimedHolyWater = 0;
        long lastSettleAfkTime = getPropValue(TheWarCellPropertyEnum.TWCP_LastSettleAfkTime_VALUE);

        long goldEfficacy = getPropValue(TheWarCellPropertyEnum.TWCP_WarGoldEfficacy_VALUE);
        long dpEfficacy = getPropValue(TheWarCellPropertyEnum.TWCP_DPEfficacy_VALUE);
        long holyWaterEfficacy = getPropValue(TheWarCellPropertyEnum.TWCP_HolyWarterEfficacy_VALUE);
        long productTime = GlobalTick.getInstance().getCurrentTime() - Math.max(getTroopsTime(), lastSettleAfkTime);
        unclaimedGold += goldEfficacy * productTime * getPropValue(TheWarCellPropertyEnum.TWCP_StationTroopWGPlus_VALUE) / 1000 / TimeUtil.MS_IN_A_MIN;
        unclaimedDP += dpEfficacy * productTime * getPropValue(TheWarCellPropertyEnum.TWCP_StationTroopDPPlus_VALUE) / 1000 / TimeUtil.MS_IN_A_MIN;
        unclaimedHolyWater += holyWaterEfficacy * productTime * getPropValue(TheWarCellPropertyEnum.TWCP_StationTroopHolyWaterPlus_VALUE) / 1000 / TimeUtil.MS_IN_A_MIN;

        Event event = Event.valueOf(EventType.ET_TheWar_AddUnclaimedReward, this, player);
        event.pushParam(unclaimedGold, unclaimedDP, unclaimedHolyWater);
        EventManager.getInstance().dispatchEvent(event);
    }

    public TheWarRetCode clearStationTroopsPet(WarPlayer player, boolean needBroadcast) {
        if (isBlock()) {
            return TheWarRetCode.TWRC_GridIsBlock; // 阻挡格子
        }
        long ownerId = getPropValue(TheWarCellPropertyEnum.TWCP_OccupierPlayerId_VALUE);
        if (GameUtil.stringToLong(player.getIdx(), 0) != ownerId) {
            return TheWarRetCode.TWRC_NotOccupiedGrid; // 未占领该格子
        }
        if (StringHelper.isNull(troopsPetIdx)) {
            return TheWarRetCode.TWRC_NotFoundTroopsPet; // 未找到驻防宠物
        }
        Event event = Event.valueOf(EventType.ET_TheWar_RemovePetTroopsData, this, player);
        event.pushParam(Collections.singletonList(getTroopsPetIdx()));
        EventManager.getInstance().dispatchEvent(event);

        settlePetTroopsReward(player);

        setTroopsPetIdx("");
        setTroopsTime(0);

        if (needBroadcast) {
            broadcastPropData();
        }

        CS_GS_TheWarUpdateOwnedGridData.Builder builder = CS_GS_TheWarUpdateOwnedGridData.newBuilder();
        builder.setBAdd(true);
        builder.setPlayerIdx(getIdx());
        builder.getGridDataBuilder().setPos(getPos());
        builder.getGridDataBuilder().setGridType((int) getPropValue(TheWarCellPropertyEnum.TWCP_CellTag_VALUE));
        builder.getGridDataBuilder().setGridLevel((int) getPropValue(TheWarCellPropertyEnum.TWCP_Level_VALUE));
        builder.getGridDataBuilder().setHasTrooped(false);
        player.sendMsgToServer(MsgIdEnum.CS_GS_TheWarUpdateOwnedGridData_VALUE, builder);

        return TheWarRetCode.TWRC_Success;
    }

    public long getOccupyTime(long curTime) {
        long occupyTimeStamp = getPropValue(TheWarCellPropertyEnum.TWCP_OccupyTime_VALUE);
        return occupyTimeStamp > 0 && occupyTimeStamp < curTime ? curTime - occupyTimeStamp : 0;
    }

    public long getAfkBonusTime(long curTime) {
        long lastSettleTime = getPropValue(TheWarCellPropertyEnum.TWCP_LastSettleAfkTime_VALUE);
        return lastSettleTime > 0 && lastSettleTime < curTime ? curTime - lastSettleTime : 0;
    }

    public long getTroopsRewardTime(long curTime) {
        if (getPropValue(TheWarCellPropertyEnum.TWCP_CanStationTroop_VALUE) <= 0) {
            return 0;
        }
        long ownerId = getPropValue(TheWarCellPropertyEnum.TWCP_OccupierPlayerId_VALUE);
        if (ownerId <= 0) {
            return 0;
        }
        if (StringHelper.isNull(troopsPetIdx)) {
            return 0;
        }
        WarPlayer warPlayer = WarPlayerCache.getInstance().queryObject(GameUtil.longToString(ownerId, ""));
        if (warPlayer == null) {
            return 0;
        }
        long lastSettleTime = getPropValue(TheWarCellPropertyEnum.TWCP_LastSettleAfkTime_VALUE);

        return curTime - Math.max(lastSettleTime, troopsTime);
    }

    public TheWarGridStationConfigObject getBonusRewardPlusConfig() {
        long troopsCfgId = getPropValue(TheWarCellPropertyEnum.TWCP_StationTroopBuffCfgIg_VALUE);
        if (troopsCfgId <= 0) {
            return null;
        }
        long ownerId = getPropValue(TheWarCellPropertyEnum.TWCP_OccupierPlayerId_VALUE);
        WarPlayer warPlayer = WarPlayerCache.getInstance().queryObject(GameUtil.longToString(ownerId, ""));
        if (warPlayer == null) {
            return null;
        }
        WarPetData warPet = warPlayer.getWarPetData(troopsPetIdx);
        if (warPet == null) {
            return null;
        }
        return TheWarGridStationConfig.getInstance().getTroopsBuffConfigByGroupAndRace((int) troopsCfgId, warPet.getPetQuality());
    }

    public int calcRewardGold(long curTime) {
        long unclaimedGold = 0;
        // 每分钟产出金币效率 * 1000
        long goldEfficacy = getPropValue(TheWarCellPropertyEnum.TWCP_WarGoldEfficacy_VALUE);
        unclaimedGold += goldEfficacy * getAfkBonusTime(curTime) / TimeUtil.MS_IN_A_MIN;
        // 每分钟产出金币效率 加成千分比
        TheWarGridStationConfigObject cfg = getBonusRewardPlusConfig();
        if (cfg != null) {
            unclaimedGold += goldEfficacy * getTroopsRewardTime(curTime) * cfg.getWargoldplus() / 1000 / TimeUtil.MS_IN_A_MIN;
        }
        return (int) unclaimedGold;
    }

    public int calcRewardDp(long curTime) {
        long unclaimedDp = 0;
        // 每分钟产出开门资源点效率 * 1000
        long dpEfficacy = getPropValue(TheWarCellPropertyEnum.TWCP_DPEfficacy_VALUE);
        unclaimedDp += dpEfficacy * getAfkBonusTime(curTime) / TimeUtil.MS_IN_A_MIN;
        // 每分钟产出开门资源点效率 加成千分比
        TheWarGridStationConfigObject cfg = getBonusRewardPlusConfig();
        if (cfg != null) {
            unclaimedDp += dpEfficacy * getTroopsRewardTime(curTime) * cfg.getDpplus() / 1000 / TimeUtil.MS_IN_A_MIN;
        }
        return (int) unclaimedDp;
    }

    public int calcRewardHolyWater(long curTime) {
        long unclaimedHolyWater = 0;
        // 每分钟产出圣水效率 * 1000
        long holyWaterEfficacy = getPropValue(TheWarCellPropertyEnum.TWCP_HolyWarterEfficacy_VALUE);
        unclaimedHolyWater += holyWaterEfficacy * getAfkBonusTime(curTime) / TimeUtil.MS_IN_A_MIN;
        // 每分钟产出圣水效率 加成千分比
        TheWarGridStationConfigObject cfg = getBonusRewardPlusConfig();
        if (cfg != null) {
            unclaimedHolyWater += holyWaterEfficacy * getTroopsRewardTime(curTime) * cfg.getHolywaterplus() / 1000 / TimeUtil.MS_IN_A_MIN;
        }
        return (int) unclaimedHolyWater;
    }

    public void resetAfkBonusTimeInfo(long curTime) {
        setPropValue(TheWarCellPropertyEnum.TWCP_LastSettleAfkTime_VALUE, curTime);
        broadcastPropData();
    }

    public long playerClaimAfkReward(WarReward.Builder goldReward, WarReward.Builder dpReward, WarReward.Builder holyWaterReward, List<WarReward> rewardList, long curTime) {
        if (rewardList == null || getPropValue(TheWarCellPropertyEnum.TWCP_BossMaxHp_VALUE) > 0) {
            resetAfkBonusTimeInfo(curTime);
            return 0;
        }

        goldReward.setRewardCount(goldReward.getRewardCount() + calcRewardGold(curTime));
        dpReward.setRewardCount(dpReward.getRewardCount() + calcRewardDp(curTime));
        holyWaterReward.setRewardCount(holyWaterReward.getRewardCount() + calcRewardHolyWater(curTime));

        long occupyTime = getAfkBonusTime(curTime);
        resetAfkBonusTimeInfo(curTime);
        return occupyTime;
    }

    public void clearMonsterAfkReward() {
        long isRefreshed = getPropValue(TheWarCellPropertyEnum.TWCP_IsRefreshed_VALUE);
        if (isRefreshed <= 0) {
            return;
        }

        int cfgId = (int) getPropValue(TheWarCellPropertyEnum.TWCP_MonsterRefreshCfgId_VALUE);
        if (cfgId > 0) {
            WarRoom warRoom = WarRoomCache.getInstance().queryObject(getRoomIdx());
            if (warRoom != null) {
                Event event = Event.valueOf(EventType.ET_TheWar_DecRefreshMonsterCount, this, warRoom);
                event.pushParam(cfgId);
                EventManager.getInstance().dispatchEvent(event);
            }
        }
        setPropValue(TheWarCellPropertyEnum.TWCP_IsRefreshed_VALUE, 0);
        setPropDefaultValue(TheWarCellPropertyEnum.TWCP_WarGoldEfficacy_VALUE);
        setPropDefaultValue(TheWarCellPropertyEnum.TWCP_DPEfficacy_VALUE);
        setPropValue(TheWarCellPropertyEnum.TWCP_IsDoorPointMode_VALUE, 0);
        setPropValue(TheWarCellPropertyEnum.TWCP_CurDpAfkTime_VALUE, 0);
        setPropDefaultValue(TheWarCellPropertyEnum.TWCP_MaxDpAfkTime_VALUE);
        refreshFightMakeId();
    }

    public void settleMonsterAfkReward() {
        long isRefreshed = getPropValue(TheWarCellPropertyEnum.TWCP_IsRefreshed_VALUE);
        if (isRefreshed <= 0) {
            return;
        }
        String playerIdx = GameUtil.longToString(getPropValue(TheWarCellPropertyEnum.TWCP_OccupierPlayerId_VALUE), "");
        WarPlayer warPlayer = WarPlayerCache.getInstance().queryObject(playerIdx);
        long curTime = GlobalTick.getInstance().getCurrentTime();
        if (warPlayer != null) {
            Event event = Event.valueOf(EventType.ET_TheWar_AddUnsettledCurrency, this, warPlayer);
            event.pushParam(false, calcRewardGold(curTime), calcRewardDp(curTime), calcRewardHolyWater(curTime));
            event.pushParam((int) getPropValue(TheWarCellPropertyEnum.TWCP_DropItemCfgId_VALUE), getAfkBonusTime(curTime));
            EventManager.getInstance().dispatchEvent(event);
        }
        resetAfkBonusTimeInfo(curTime);

        setPropValue(TheWarCellPropertyEnum.TWCP_IsRefreshed_VALUE, 0);
        setPropDefaultValue(TheWarCellPropertyEnum.TWCP_WarGoldEfficacy_VALUE);
        setPropDefaultValue(TheWarCellPropertyEnum.TWCP_DPEfficacy_VALUE);
        setPropValue(TheWarCellPropertyEnum.TWCP_IsDoorPointMode_VALUE, 0);
        setPropValue(TheWarCellPropertyEnum.TWCP_CurDpAfkTime_VALUE, 0);
        setPropDefaultValue(TheWarCellPropertyEnum.TWCP_MaxDpAfkTime_VALUE);
        refreshFightMakeId();
        broadcastPropData();
    }

    public SC_QueryGridBattleData.Builder getGridBattlePetData() {
        SC_QueryGridBattleData.Builder builder = SC_QueryGridBattleData.newBuilder();
        builder.setGridPos(getPos());
        builder.setRetCode(TheWarRetCode.TWRC_Success);
        if (!monsterPetHpMap.isEmpty()) {
            for (Entry<String, Integer> entry : monsterPetHpMap.entrySet()) {
                builder.getPetHpDataBuilder().addPetIdx(entry.getKey()).addHpRate(entry.getValue());
            }
        }
        long ownerId = getPropValue(TheWarCellPropertyEnum.TWCP_OccupierPlayerId_VALUE);
        if (ownerId <= 0) {
            return builder;
        }
        String playerIdx = GameUtil.longToString(ownerId, "");
        WarPlayer owner = WarPlayerCache.getInstance().queryObject(playerIdx);
        if (owner == null || StringHelper.isNull(troopsPetIdx)) {
            return builder;
        }
        WarPetData petData = owner.getWarPetData(troopsPetIdx);
        List<Integer> extBuffList = new ArrayList<>();
//        for (WarProfessionData profData : owner.getPlayerData().getTechDbData().getProfessionDataMap().values()) {
//            extBuffList.addAll(profData.getTechBuffList());
//        }
        if (petData != null) {
            long buffCfgId = getPropValue(TheWarCellPropertyEnum.TWCP_StationTroopBuffCfgIg_VALUE);
            List<Integer> buffList = TheWarGridStationConfig.getInstance().getTroopsBuffListByPetInfo((int) buffCfgId, petData.getPetQuality(), petData.getPetCfgId());
            if (buffList != null) {
                extBuffList.addAll(buffList);
            }
        }

        builder.addAllExtBuffList(extBuffList);
        builder.setWarPetData(petData);
        return builder;
    }

    public List<BattleRemainPet> buildMonsterPetData() {
        if (monsterPetHpMap == null || monsterPetHpMap.isEmpty()) {
            return Collections.emptyList();
        }

        List<BattleRemainPet> battleRemainPets = new ArrayList<>();
        BattleRemainPet.Builder builder = BattleRemainPet.newBuilder();
        builder.setCamp(2);
        for (Entry<String, Integer> entry : monsterPetHpMap.entrySet()) {
            builder.setPetId(entry.getKey());
            builder.setRemainHpRate(entry.getValue());
            battleRemainPets.add(builder.build());
        }
        return battleRemainPets;
    }

    @Override
    public GridCacheData.Builder buildGridCacheBuilder() {
        gridCacheData = super.buildGridCacheBuilder();
        gridCacheData.putAllMonsterRemainHpInfo(monsterPetHpMap);
        gridCacheData.getGridTroopsInfoBuilder().setPetIdx(getTroopsPetIdx());
        gridCacheData.getGridTroopsInfoBuilder().setTroopsTime(getTroopsTime());
        return gridCacheData;
    }

    @Override
    public void parseFromCacheData(GridCacheData cacheData) {
        super.parseFromCacheData(cacheData);
        monsterPetHpMap.putAll(cacheData.getMonsterRemainHpInfoMap());
        setTroopsPetIdx(cacheData.getGridTroopsInfo().getPetIdx());
        setTroopsTime(cacheData.getGridTroopsInfo().getTroopsTime());
    }
}

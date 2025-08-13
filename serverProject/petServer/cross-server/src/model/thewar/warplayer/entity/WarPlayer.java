package model.thewar.warplayer.entity;

import cfg.TheWarBattleReward;
import cfg.TheWarBattleRewardObject;
import cfg.TheWarConstConfig;
import cfg.TheWarConstConfigObject;
import cfg.TheWarDropItemConfig;
import cfg.TheWarDropItemConfigObject;
import cfg.TheWarItemConfig;
import cfg.TheWarItemConfigObject;
import cfg.TheWarJobTileConfig;
import cfg.TheWarJobTileConfigObject;
import cfg.TheWarMapConfig;
import cfg.TheWarMapConfigObject;
import cfg.TheWarPersueConfig;
import cfg.TheWarPersueConfigObject;
import cfg.TheWarSeasonConfigObject;
import cfg.TheWarSkillConfig;
import cfg.TheWarTargetConfig;
import cfg.TheWarTargetConfigObject;
import cfg.TheWarTechConfig;
import cfg.TheWarTechConfigObject;
import com.google.protobuf.GeneratedMessageV3.Builder;
import common.GameConst;
import common.GameConst.EventType;
import common.GlobalData;
import common.GlobalTick;
import common.IdGenerator;
import datatool.StringHelper;
import lombok.Getter;
import lombok.Setter;
import model.obj.BaseObj;
import model.thewar.TheWarManager;
import model.thewar.WarConst;
import model.thewar.warmap.WarMapData;
import model.thewar.warmap.WarMapManager;
import model.thewar.warmap.grid.FootHoldGrid;
import model.thewar.warmap.grid.WarMapGrid;
import model.thewar.warroom.dbCache.WarRoomCache;
import model.thewar.warroom.entity.WarRoom;
import org.springframework.util.CollectionUtils;
import protocol.Battle.BattlePetData;
import protocol.Battle.ExtendProperty;
import protocol.Battle.PetBuffData;
import protocol.Battle.PetPropertyDict;
import protocol.Battle.PlayerBaseInfo;
import protocol.Battle.SkillBattleDict;
import protocol.Common.MissionStatusEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.PetProperty;
import protocol.PrepareWar.TeamSkillPositionEnum;
import protocol.ServerTransfer.CS_GS_GainHolyWater;
import protocol.ServerTransfer.CS_GS_GetJobTileReward;
import protocol.ServerTransfer.CS_GS_RemoveWarPetData;
import protocol.ServerTransfer.CS_GS_TheWarCurrencyLog;
import protocol.ServerTransfer.CS_GS_TheWarCurrencyLog.CurrencyLogData;
import protocol.ServerTransfer.CS_GS_TheWarTransInfo;
import protocol.ServerTransfer.CS_GS_TheWarUpdateOwnedGridData;
import protocol.ServerTransfer.CS_GS_UpdateLastSettleTime;
import protocol.ServerTransfer.CS_GS_UpdateTheWarTargetPro;
import protocol.ServerTransfer.CS_GS_UpdateWarReward;
import protocol.ServerTransfer.GS_CS_PlayerEnterTheWar;
import protocol.TargetSystem.TargetTypeEnum;
import protocol.TheWar.CS_ComposeNewItem;
import protocol.TheWar.EnumTheWarTips;
import protocol.TheWar.SC_QueryWarTeam;
import protocol.TheWar.SC_TheWarBattleReward;
import protocol.TheWar.SC_UpdateAfkRewardData;
import protocol.TheWar.SC_UpdateTheWarMission;
import protocol.TheWar.SC_UpdateWarFightTeam;
import protocol.TheWar.SC_UpdateWarGridRecord;
import protocol.TheWar.WarGridRecordData;
import protocol.TheWar.WarReward;
import protocol.TheWar.WarSeasonMission;
import protocol.TheWar.WarTeamInfo;
import protocol.TheWar.WarTeamPetDict;
import protocol.TheWar.WarTeamSkillDict;
import protocol.TheWarDB.PlayerCacheData;
import protocol.TheWarDB.WarCampAkfInfo;
import protocol.TheWarDB.WarPlayerDB;
import protocol.TheWarDB.WarProfessionData;
import protocol.TheWarDB.WarTeamData;
import protocol.TheWarDefine.CS_EquipOffItem;
import protocol.TheWarDefine.CS_EquipOnItem;
import protocol.TheWarDefine.Position;
import protocol.TheWarDefine.SC_JobTileTaskData;
import protocol.TheWarDefine.SC_UpdateNewItem;
import protocol.TheWarDefine.SC_UpdatePetProp;
import protocol.TheWarDefine.SC_UpdatePlayerStamia;
import protocol.TheWarDefine.SC_UpdateWarCurrency;
import protocol.TheWarDefine.TheWarCampInfo;
import protocol.TheWarDefine.TheWarCellPropertyEnum;
import protocol.TheWarDefine.TheWarGridData;
import protocol.TheWarDefine.TheWarItemData;
import protocol.TheWarDefine.TheWarItemPos;
import protocol.TheWarDefine.TheWarPlayerDetailInfo;
import protocol.TheWarDefine.TheWarPropertyMap;
import protocol.TheWarDefine.TheWarResourceType;
import protocol.TheWarDefine.TheWarRetCode;
import protocol.TheWarDefine.TheWarSkillData;
import protocol.TheWarDefine.TheWarTechData;
import protocol.TheWarDefine.TheWarTechnicalData;
import protocol.TheWarDefine.WarCellTagFlag;
import protocol.TheWarDefine.WarJobTileTask;
import protocol.TheWarDefine.WarPetData;
import protocol.TheWarDefine.WarPetPropInfo;
import protocol.TheWarDefine.WarPetPropertyDict;
import protocol.TheWarDefine.WarTarget;
import protocol.TheWarDefine.WarTaskStateEnum;
import server.event.Event;
import server.event.EventManager;
import util.GameUtil;
import util.LogUtil;
import util.TimeUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
public class WarPlayer extends BaseObj {
    private String idx;
    private String name;
    private int level;
    private int vip;
    private int avatar;
    private int avatarBorder;
    private int avatarBorderRank;

    private int serverIndex;

    private int camp;
    private String roomIdx;

    private int jobTileLevel; // 职位等级

//    private byte[] dbData;

    private WarPlayerDB.Builder playerData;

    private PlayerCacheData.Builder playerCacheData = PlayerCacheData.newBuilder();

    private int titleId;

    private int playerNameId;

    private int newTitleId;

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
        return this.getClass().getSimpleName();
    }

    @Override
    public void putToCache() {
        WarRoom warRoom = WarRoomCache.getInstance().queryObject(getRoomIdx());
        if (warRoom != null) {
            Event event = Event.valueOf(EventType.ET_TheWar_AddPlayerCache, this, warRoom);
            event.pushParam(buildPlayerCache());
            EventManager.getInstance().dispatchEvent(event);
        }
    }

    @Override
    public void transformDBData() {
    }

    @Override
    public String getBaseIdx() {
        return idx;
    }

//    public byte[] getDbData() {
//        return dbData;
//    }
//
//    public void setDbData(byte[] dbData) {
//        this.dbData = dbData;
//    }

    public WarPlayerDB.Builder getPlayerData() {
        if (playerData == null) {
            this.playerData = WarPlayerDB.newBuilder();
//            try {
//                if (dbData != null) {
//                    this.playerData = WarPlayerDB.parseFrom(this.dbData).toBuilder();
//                } else {
//                    this.playerData = WarPlayerDB.newBuilder();
//                }
//            } catch (Exception e) {
//                LogUtil.printStackTrace(e);
//            }
        }
        return playerData;
    }

    public PlayerCacheData buildPlayerCache() {
        playerCacheData.clear();
        playerCacheData.setPlayerInfo(buildPlayerBaseInfo());
        playerCacheData.setRoomIdx(getRoomIdx());
        playerCacheData.setCamp(getCamp());
        playerCacheData.setServerIndex(getServerIndex());
        playerCacheData.setJobTileLevel(getJobTileLevel());
        playerCacheData.setPlayerDbData(getPlayerData());
        return playerCacheData.build();
    }

    // ====================================================================== //
    private boolean online;

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public void addOwnedGridPos(WarMapGrid grid) {
        if (grid != null) {
            if (getPlayerData().getOwnedGridPosList().contains(grid.getPos())) {
                return;
            }
            getPlayerData().addOwnedGridPos(grid.getPos());
//            getPlayerData().setLatestPos(grid.getPos());
            if (grid.getPropValue(TheWarCellPropertyEnum.TWCP_PlayerSpawn_VALUE) > 0) {
                getPlayerData().setBornPos(grid.getPos());
            }

            CS_GS_TheWarUpdateOwnedGridData.Builder builder = CS_GS_TheWarUpdateOwnedGridData.newBuilder();
            builder.setBAdd(true);
            builder.setPlayerIdx(getIdx());
            builder.getGridDataBuilder().setPos(grid.getPos());
            builder.getGridDataBuilder().setGridType((int) grid.getPropValue(TheWarCellPropertyEnum.TWCP_CellTag_VALUE));
            builder.getGridDataBuilder().setGridLevel((int) grid.getPropValue(TheWarCellPropertyEnum.TWCP_Level_VALUE));
            builder.getGridDataBuilder().setHasTrooped(false);
            sendMsgToServer(MsgIdEnum.CS_GS_TheWarUpdateOwnedGridData_VALUE, builder);
        }
    }

    public void removeOwnedGridPos(Position gridPos) {
        if (gridPos == null) {
            return;
        }
        int index = getPlayerData().getOwnedGridPosList().indexOf(gridPos);
        if (index < 0) {
            return;
        }
        getPlayerData().removeOwnedGridPos(index);

        index = getPlayerData().getCurTroopsGridList().indexOf(gridPos);
        if (index >= 0) {
            getPlayerData().removeCurTroopsGrid(index);
        }
    }

    public void addUnclaimedGold(int unclaimedGold) {
        unclaimedGold += getPlayerData().getUnclaimedWarGold();
        getPlayerData().setUnclaimedWarGold(unclaimedGold);
    }

    public void addUnclaimedDP(int unclaimedDP) {
        unclaimedDP += getPlayerData().getUnclaimedWarDp();
        getPlayerData().setUnclaimedWarDp(unclaimedDP);
    }

    public void addUnclaimedHolyWater(int unclaimedHolyWater) {
        unclaimedHolyWater += getPlayerData().getUnclaimedHolyWater();
        getPlayerData().setUnclaimedHolyWater(unclaimedHolyWater);
    }

    public void addUnsettledItem(int itemCfgId, long unsettledTime) {
        if (itemCfgId <= 0 || unsettledTime <= 0) {
            return;
        }
        Long oldTime = getPlayerData().getUnsettledItemTimeMap().get(itemCfgId);
        if (oldTime != null) {
            getPlayerData().putUnsettledItemTime(itemCfgId, unsettledTime + oldTime);
        } else {
            getPlayerData().putUnsettledItemTime(itemCfgId, unsettledTime);
        }
    }

    public List<WarReward> settleAfkItem() {
        List<WarReward> rewardList = new ArrayList<>();
        // 产出道具
        TheWarDropItemConfigObject cfg;
        Map<Integer, Long> remainItemTime = new HashMap<>();
        Map<Integer, Integer> rewardData = new HashMap<>();
        for (Entry<Integer, Long> entry : getPlayerData().getUnsettledItemTimeMap().entrySet()) {
            cfg = TheWarDropItemConfig.getById(entry.getKey());
            if (cfg == null) {
                continue;
            }
            cfg.calcDropItems(entry.getValue(), rewardData);
            remainItemTime.put(entry.getKey(), entry.getValue() % (TimeUtil.MS_IN_A_MIN * cfg.getBaseafktime()));
        }
        rewardData.forEach((itemId, itemCount) -> {
            WarReward.Builder rewardBuilder = WarReward.newBuilder().setRewardType(TheWarResourceType.TWRT_WarItem).setRewardId(itemId).setRewardCount(itemCount);
            rewardList.add(rewardBuilder.build());
        });
        getPlayerData().putAllUnsettledItemTime(remainItemTime);
        return rewardList;
    }

    public void init(String roomIdx, GS_CS_PlayerEnterTheWar playerEnterInfo) {
        setOnline(true);
        setIdx(playerEnterInfo.getPlayerInfo().getPlayerId());
        setRoomIdx(roomIdx);
        setServerIndex(playerEnterInfo.getFromSvrIndex());
        setName(playerEnterInfo.getPlayerInfo().getPlayerName());
        setLevel(playerEnterInfo.getPlayerInfo().getLevel());
        setVip(playerEnterInfo.getPlayerInfo().getVipLevel());
        setAvatar(playerEnterInfo.getPlayerInfo().getAvatar());
        setAvatarBorder(playerEnterInfo.getPlayerInfo().getAvatarBorder());
        setAvatarBorderRank(playerEnterInfo.getPlayerInfo().getAvatarBorderRank());
        setTitleId(playerEnterInfo.getPlayerInfo().getTitleId());
        setPlayerNameId(playerEnterInfo.getPlayerInfo().getPlayerNameId());
        setNewTitleId(playerEnterInfo.getPlayerInfo().getNewTitleId());
        // 初始化职位信息
        setJobTileLevel(TheWarJobTileConfig.getInstance().getInitJobTileLevel()); // 职位默认为1级
        TheWarJobTileConfigObject newCfg = TheWarJobTileConfig.getById(getJobTileLevel());
        if (newCfg != null) {
            getPlayerData().getTeamDbDataBuilder().setUnlockPetNum(newCfg.getTeammaxpetcount());
            getPlayerData().getJobTileTaskDataBuilder().setTaskState(WarTaskStateEnum.WTSE_NotFinish);
        }
        TheWarJobTileConfigObject nextCfg = TheWarJobTileConfig.getById(getJobTileLevel() + 1);
        if (newCfg != null) {
            addNewJobTileTask(nextCfg.getAchievecondition());
        }
        // 初始化科技信息,在职位信息后
        for (int race : TheWarTechConfig.getInstance().getRaceConfigMap().keySet()) {
            levelUpTechnology(race);
        }

        getPlayerData().clearPlayerBaseAdditions();
        getPlayerData().putAllPlayerBaseAdditions(playerEnterInfo.getPlayerBaseAdditionsMap());

        getPlayerData().setWarHolyWater(playerEnterInfo.getHolyWater());

        addTargetProgress(TargetTypeEnum.TTE_TheWar_JobTileLvReach, 0, getJobTileLevel());

        initWarSeasonMission();
    }

    public void initByCache(PlayerCacheData cacheData, WarMapData mapData) {
        setIdx(cacheData.getPlayerInfo().getPlayerId());
        setRoomIdx(cacheData.getRoomIdx());
        setCamp(cacheData.getCamp());
        setJobTileLevel(cacheData.getJobTileLevel());
        // 兼容代码
        int svrIndex = cacheData.getServerIndex();
        if (svrIndex <= 0) {
            String addr = cacheData.getFromSvrAddr();
            GlobalData.getInstance().getServerIndexByIp(addr);
        }
        setServerIndex(svrIndex);
        setName(cacheData.getPlayerInfo().getPlayerName());
        setLevel(cacheData.getPlayerInfo().getLevel());
        setVip(cacheData.getPlayerInfo().getVipLevel());
        setAvatar(cacheData.getPlayerInfo().getAvatar());
        setAvatarBorder(cacheData.getPlayerInfo().getAvatarBorder());
        setAvatarBorderRank(cacheData.getPlayerInfo().getAvatarBorderRank());
        setTitleId(cacheData.getPlayerInfo().getTitleId());
        setPlayerNameId(cacheData.getPlayerInfo().getPlayerNameId());
        setNewTitleId(cacheData.getPlayerInfo().getNewTitleId());
        getPlayerData().mergeFrom(cacheData.getPlayerDbData());

        List<Position> list = getPlayerData().getOwnedGridPosList().stream().filter(pos -> {
            WarMapGrid grid = mapData.getMapGridByPos(pos);
            return grid != null && !getPlayerData().getOwnedGridPosList().contains(pos) && grid.getPropValue(TheWarCellPropertyEnum.TWCP_OccupierPlayerId_VALUE) == GameUtil.stringToLong(getIdx(), 0);
        }).collect(Collectors.toList());
        getPlayerData().addAllOwnedGridPos(list);
    }

    public void clear() {
        setName(null);
        setServerIndex(0);
        setLevel(0);
        setVip(0);
        setAvatar(0);
        setAvatarBorder(0);
        setAvatarBorderRank(0);
        setTitleId(0);
        setPlayerNameId(0);
        setNewTitleId(0);

        setRoomIdx(null);
        setJobTileLevel(0);
        setCamp(0);

        setOnline(false);
        getPlayerData().clear();
        playerCacheData.clear();
    }

    public int firstEnterReward(int roomLevel, List<WarReward> rewards) {
        TheWarPersueConfigObject cfg = TheWarPersueConfig.getByRoomlevel(roomLevel);
        if (cfg == null) {
            return 0;
        }
        int pursuitStamina = cfg.getRewardstamina();
        getPlayerData().setStamina(getPlayerData().getStamina() + pursuitStamina);
        sendPlayerStamina();
        if (rewards != null) {
            rewards.addAll(cfg.getWarrewrad());
            gainWarReward(rewards, false, "追赶奖励");
        }
        return pursuitStamina;
    }

    public void onPlayerLogin(GS_CS_PlayerEnterTheWar req) {
        setOnline(true);
        setIdx(req.getPlayerInfo().getPlayerId());
        setName(req.getPlayerInfo().getPlayerName());
        setLevel(req.getPlayerInfo().getLevel());
        setVip(req.getPlayerInfo().getVipLevel());
        setAvatar(req.getPlayerInfo().getAvatar());
        setAvatarBorder(req.getPlayerInfo().getAvatarBorder());
        setAvatarBorderRank(req.getPlayerInfo().getAvatarBorderRank());
        setTitleId(req.getPlayerInfo().getTitleId());
        setPlayerNameId(req.getPlayerInfo().getPlayerNameId());
        setNewTitleId(req.getPlayerInfo().getNewTitleId());
        getPlayerData().clearPlayerBaseAdditions();
        getPlayerData().putAllPlayerBaseAdditions(req.getPlayerBaseAdditionsMap());
        getPlayerData().setWarHolyWater(req.getHolyWater());
    }

    public void onPlayerLogout() {
        setOnline(false);
    }

    public PlayerBaseInfo buildPlayerBaseInfo() {
        PlayerBaseInfo.Builder builder = PlayerBaseInfo.newBuilder();
        builder.setPlayerId(getIdx());
        builder.setPlayerName(getName());
        builder.setLevel(getLevel());
        builder.setVipLevel(getVip());
        builder.setAvatar(getAvatar());
        builder.setAvatarBorder(getAvatarBorder());
        builder.setAvatarBorderRank(getAvatarBorderRank());
        builder.setTitleId(getTitleId());
        builder.setPlayerNameId(getPlayerNameId());
        builder.setNewTitleId(getNewTitleId());
        return builder.build();
    }

    public TheWarPlayerDetailInfo.Builder buildPlayerDetailInfo() {
        TheWarPlayerDetailInfo.Builder builder = TheWarPlayerDetailInfo.newBuilder();
        builder.setPlayerIdx(getIdx());
        builder.setCamp(getCamp());
        builder.setJobTileLevel(getJobTileLevel());
        builder.setWarGold(getPlayerData().getWarGold());
        builder.setWarDp(getPlayerData().getWarDP());
        builder.setTechData(buildTechnicalData());
        builder.addAllItemData(getPlayerData().getTechDbData().getOwedItemsMap().values());
        builder.setJobTileTask(builderJobTileTaskData());
        builder.setLastSettleTime(getPlayerData().getLastSettleAfkTime());
        builder.setStamia(getPlayerData().getStamina());
        builder.setLatestPos(getPlayerData().getLatestPos());
        WarRoom warRoom = WarRoomCache.getInstance().queryObject(getRoomIdx());
        if (warRoom != null) {
            TheWarCampInfo.Builder campInfo = warRoom.getCampInfo(getCamp());
            if (campInfo != null) {
                builder.setCampInfo(campInfo);
            }
        }
        return builder;
    }

    public ExtendProperty.Builder buildExtendBattleInfo(int camp) {
        ExtendProperty.Builder extendInfo = ExtendProperty.newBuilder();
        extendInfo.setCamp(camp);

        Set<Integer> buffList = new HashSet<>();
        for (WarProfessionData profData : getPlayerData().getTechDbData().getProfessionDataMap().values()) {
            for (TheWarSkillData skillData : profData.getSkillDataList()) {
                buffList.addAll(skillData.getSkillBuffList());
            }
            buffList.addAll(profData.getTechBuffList());
        }
        PetBuffData.Builder petBuff = PetBuffData.newBuilder();
        petBuff.setBuffCount(1);
        for (Integer buffId : buffList) {
            petBuff.setBuffCfgId(buffId);
            extendInfo.addBuffData(petBuff.build());
        }
        return extendInfo;
    }

    public List<TheWarGridData> buildPlayerOwnedGrids() {
        List<TheWarGridData> gridDataList = new ArrayList<>();
        WarMapData roomMap = WarMapManager.getInstance().getRoomMapData(getRoomIdx());
        if (roomMap == null) {
            return gridDataList;
        }
        WarMapGrid warGrid;
        for (Position gridPos : getPlayerData().getOwnedGridPosList()) {
            warGrid = roomMap.getMapGridByPos(gridPos);
            if (warGrid == null) {
                continue;
            }
            TheWarGridData.Builder builder = TheWarGridData.newBuilder();
            builder.setPos(gridPos);
            builder.setName(warGrid.getName());
            TheWarPropertyMap.Builder propBuilder = TheWarPropertyMap.newBuilder();
            warGrid.getTotalProp().forEach((key, val) -> {
                if (WarConst.isServerOnlyProp(key)) {
                    return;
                }
                propBuilder.addKeysValue(key);
                propBuilder.addValues(val);
            });
            builder.setProps(propBuilder);
            gridDataList.add(builder.build());
        }
        return gridDataList;
    }

    protected void updateClearingGrids(long curTime) {
        if (getPlayerData().getClearingGridPosCount() <= 0) {
            return;
        }

        WarMapData mapData = WarMapManager.getInstance().getRoomMapData(getRoomIdx());
        if (mapData == null) {
            return;
        }

        Position pos;
        WarMapGrid grid;
        List<Long> removePosList = null;
        Position.Builder posBuilder = Position.newBuilder();
        for (Entry<Long, Long> entry : getPlayerData().getClearingGridPosMap().entrySet()) {
            long longPos = entry.getKey();
            long expireTime = entry.getValue();
            posBuilder.setX((int) (longPos >>> 32)).setY((int) longPos);
            pos = posBuilder.build();
            if (expireTime > curTime && getPlayerData().getOwnedGridPosList().contains(pos)) {
                continue;
            }
            if (removePosList == null) {
                removePosList = new ArrayList<>();
            }
            removePosList.add(longPos);

            grid = mapData.getMapGridByPos(pos);
            if (grid instanceof FootHoldGrid) {
                FootHoldGrid ftGrid = (FootHoldGrid) grid;
                if (ftGrid.getPropValue(TheWarCellPropertyEnum.TWCP_OccupierPlayerId_VALUE) != GameUtil.stringToLong(getIdx(), 0)) {
                    continue;
                }

                int unclaimedGold = ftGrid.calcRewardGold(curTime);
                int unclaimedDp = ftGrid.calcRewardDp(curTime);
                int unclaimedHolyWater = ftGrid.calcRewardHolyWater(curTime);
                int dropItemCfgId = (int) ftGrid.getPropValue(TheWarCellPropertyEnum.TWCP_DropItemCfgId_VALUE);
                long unsettledItemTime = ftGrid.getAfkBonusTime(curTime);
                addUnclaimedGold(unclaimedGold);
                addUnclaimedDP(unclaimedDp);
                addUnclaimedHolyWater(unclaimedHolyWater);
                addUnsettledItem(dropItemCfgId, unsettledItemTime);
                if (getPlayerData().getOwnedGridPosList().contains(ftGrid.getPos())) {
                    removeOwnedGridPos(ftGrid.getPos());
                }

                Event event = Event.valueOf(EventType.ET_TheWar_ClearFootHoldGrid, GameUtil.getDefaultEventSource(), GameUtil.getDefaultEventSource());
                event.pushParam(this, ftGrid);
                EventManager.getInstance().dispatchEvent(event);

                CS_GS_TheWarUpdateOwnedGridData.Builder builder = CS_GS_TheWarUpdateOwnedGridData.newBuilder();
                builder.setBAdd(false);
                builder.setPlayerIdx(getIdx());
                builder.getGridDataBuilder().setPos(ftGrid.getPos());
                sendMsgToServer(MsgIdEnum.CS_GS_TheWarUpdateOwnedGridData_VALUE, builder);
            }
        }
        if (removePosList != null) {
            for (Long removePos : removePosList) {
                getPlayerData().removeClearingGridPos(removePos);
            }
        }

    }

    public void settleAfkReward(List<WarReward> rewardList, Map<Integer, Long> itemRewardTimeMap) {
        // 累加上次结算道具剩余时间
        itemRewardTimeMap.forEach((cfgId, time) -> addUnsettledItem(cfgId, time));
        // 根据时间获得随机道具奖励
        List<WarReward> itemRewards = settleAfkItem();

        // 合并随机出的道具奖励
        if (!CollectionUtils.isEmpty(itemRewards)) {
            rewardList.addAll(itemRewards);
        }

        // 获得所有奖励
        List<WarReward> realRewards = gainAfkRewardWard(rewardList);
        if (!CollectionUtils.isEmpty(realRewards)) {
            sendTransMsgToServer(MsgIdEnum.SC_UpdateAfkRewardData_VALUE, SC_UpdateAfkRewardData.newBuilder().addAllWarRewards(realRewards));
        }

        getPlayerData().setLastSettleAfkTime(GlobalTick.getInstance().getCurrentTime());
        updateLastSettleTime();
    }

    public void updateLastSettleTime() {
        CS_GS_UpdateLastSettleTime.Builder builder = CS_GS_UpdateLastSettleTime.newBuilder();
        builder.setPlayerIdx(getIdx());
        builder.setLastSettleTime(getPlayerData().getLastSettleAfkTime());
        sendMsgToServer(MsgIdEnum.CS_GS_UpdateLastSettleTime_VALUE, builder);
    }

    public List<WarReward> gainAfkRewardWard(List<WarReward> rewardList) {
        if (CollectionUtils.isEmpty(rewardList)) {
            return null;
        }

        TheWarItemConfigObject itemCfg;
        List<WarReward> realRewards = new ArrayList<>();
        int sumWarGold = getPlayerData().getUnclaimedWarGold();
        int sumWarDp = getPlayerData().getUnclaimedWarDp();
        int sumWarHolyWater = getPlayerData().getUnclaimedHolyWater();
        SC_UpdateNewItem.Builder itemBuilder = SC_UpdateNewItem.newBuilder();
        for (WarReward warReward : rewardList) {
            if (warReward.getRewardType() == TheWarResourceType.TWRT_WarGold) {
                sumWarGold += warReward.getRewardCount();
            } else if (warReward.getRewardType() == TheWarResourceType.TWRT_WarDoorPoint) {
                sumWarDp += warReward.getRewardCount();
            } else if (warReward.getRewardType() == TheWarResourceType.TWRT_WarHolyWater) {
                sumWarHolyWater += warReward.getRewardCount();
            } else if (warReward.getRewardType() == TheWarResourceType.TWRT_WarItem) {
                itemCfg = TheWarItemConfig.getByItemid(warReward.getRewardId());
                if (itemCfg != null) {
                    for (int i = 0; i < warReward.getRewardCount(); i++) {
                        TheWarItemData.Builder builder = TheWarItemData.newBuilder();
                        builder.setIdx(IdGenerator.getInstance().generateId());
                        builder.setItemCfgId(warReward.getRewardId());
                        getPlayerData().getTechDbDataBuilder().putOwedItems(builder.getIdx(), builder.build());
                        itemBuilder.addItemData(builder);
                    }
                    addTargetProgress(TargetTypeEnum.TTE_TheWar_CumuCollectTech, itemCfg.getQuality(), warReward.getRewardCount());
                    addTargetProgress(TargetTypeEnum.TTE_TheWar_CumuGainTech, itemCfg.getQuality(), warReward.getRewardCount());
                    realRewards.add(warReward);
                }
            }
        }

        WarRoom warRoom = WarRoomCache.getInstance().queryObject(getRoomIdx());
        if (warRoom != null) {
            WarCampAkfInfo bossEfficacyPlus = warRoom.getBossEfficacyPlus(getCamp());
            if (bossEfficacyPlus != null) {
                sumWarGold += sumWarGold * bossEfficacyPlus.getCampBossGoldEfficacy() / 1000;
                sumWarDp += sumWarDp * bossEfficacyPlus.getCampBossDpEfficacy() / 1000;
                sumWarHolyWater += sumWarHolyWater * bossEfficacyPlus.getCampBossHolyWaterEfficacy() / 1000;
            }
        }
        if (itemBuilder.getItemDataCount() > 0) {
            sendTransMsgToServer(MsgIdEnum.SC_UpdateNewItem_VALUE, itemBuilder);
        }

        CS_GS_TheWarCurrencyLog.Builder logBuilder = CS_GS_TheWarCurrencyLog.newBuilder();
        // 金币和开门点奖励数量扩大了1000倍,结算时除以1000为真实获得值，余数为保留值，用于下次结算，避免玩家奖励丢失
        int realGold = sumWarGold / 1000;
        if (realGold > 0) {
            int oldCount = getPlayerData().getWarGold();
            int newCount = oldCount + realGold;
            CurrencyLogData.Builder goldLogBuilder = CurrencyLogData.newBuilder();
            goldLogBuilder.setCurrencyType(TheWarResourceType.TWRT_WarGold).setBeforeAmount(oldCount).setAmount(newCount).setReason("挂机奖励");
            logBuilder.addLogData(goldLogBuilder);

            WarReward.Builder goldBuilder = WarReward.newBuilder().setRewardType(TheWarResourceType.TWRT_WarGold).setRewardCount(realGold);
            realRewards.add(goldBuilder.build());
            getPlayerData().setWarGold(newCount);

            long accGold = getPlayerData().getAccumulativeWarGold() + realGold;
            getPlayerData().setAccumulativeWarGold(accGold);
            addTargetProgress(TargetTypeEnum.TTE_TheWar_CumuCollectTheWarGold, 0, realGold);
        }
        int realDp = sumWarDp / 1000;
        if (realDp > 0) {
            int oldCount = getPlayerData().getWarDP();
            int newCount = oldCount + realDp;
            CurrencyLogData.Builder dpLogBuilder = CurrencyLogData.newBuilder();
            dpLogBuilder.setCurrencyType(TheWarResourceType.TWRT_WarDoorPoint).setBeforeAmount(oldCount).setAmount(newCount).setReason("挂机奖励");
            logBuilder.addLogData(dpLogBuilder);

            WarReward.Builder dpBuilder = WarReward.newBuilder().setRewardType(TheWarResourceType.TWRT_WarDoorPoint).setRewardCount(realDp);
            realRewards.add(dpBuilder.build());
            getPlayerData().setWarDP(newCount);

            long accDp = getPlayerData().getAccumulativeWarDp() + realDp;
            getPlayerData().setAccumulativeWarDp(accDp);
            addTargetProgress(TargetTypeEnum.TTE_TheWar_CumuCollectDP, 0, realDp);
        }

        int realHolyWater = sumWarHolyWater / 1000;
        if (realHolyWater > 0) {
            WarReward.Builder holyWaterBuilder = WarReward.newBuilder().setRewardType(TheWarResourceType.TWRT_WarHolyWater).setRewardCount(realHolyWater);
            realRewards.add(holyWaterBuilder.build());
            gainHolyWater(realHolyWater);
        }

        getPlayerData().setUnclaimedWarGold(sumWarGold % 1000);
        getPlayerData().setUnclaimedWarDp(sumWarDp % 1000);
        getPlayerData().setUnclaimedHolyWater(sumWarHolyWater % 1000);

        updatePlayerWarCurrency();

        if (logBuilder.getLogDataCount() > 0) {
            logBuilder.setPlayerIdx(getIdx());
            GlobalData.getInstance().sendMsgToServer(getServerIndex(), MsgIdEnum.CS_GS_TheWarCurrencyLog_VALUE, logBuilder);
        }
        return realRewards;
    }

    public void gainHolyWater(int holyWater) {
        CS_GS_GainHolyWater.Builder builder = CS_GS_GainHolyWater.newBuilder();
        builder.setPlayerIdx(getIdx());
        builder.setAddHolyWater(holyWater);
        sendMsgToServer(MsgIdEnum.CS_GS_GainHolyWater_VALUE, builder);
    }

    public void gainBattleReward(int fightStar) {
        TheWarBattleRewardObject config = TheWarBattleReward.getById(getJobTileLevel());
        if (config == null) {
            return;
        }
        List<WarReward> rewardList = null;
        switch (fightStar) {
            case -2:
                rewardList = config.getSurrenderrewrad();
                break;
            case -1:
                rewardList = config.getFailedrewrad();
                break;
            case 0:
                rewardList = config.getZerowinrewrad();
                break;
            case 1:
                rewardList = config.getOnestarwinrewrad();
                break;
            case 2:
                rewardList = config.getTwostarwinrewrad();
                break;
            case 3:
                rewardList = config.getThreestarwinrewradwarrewrad();
                break;
            default:
                break;
        }
        gainWarReward(rewardList, true, "战斗奖励");
        if (!CollectionUtils.isEmpty(rewardList)) {
            SC_TheWarBattleReward.Builder builder = SC_TheWarBattleReward.newBuilder();
            builder.addAllWarRewards(rewardList);
            sendTransMsgToServer(MsgIdEnum.SC_TheWarBattleReward_VALUE, builder);
        }
    }

    public void gainWarReward(List<WarReward> rewards, boolean bShow, String reason) {
        if (CollectionUtils.isEmpty(rewards)) {
            return;
        }

        int sumWarGold = 0;
        int sumWarDp = 0;
        int sumWarHolyWater = 0;
        TheWarItemConfigObject itemCfg;
        SC_UpdateNewItem.Builder itemBuilder = null;
        for (WarReward warReward : rewards) {
            if (warReward.getRewardType() == TheWarResourceType.TWRT_WarGold) {
                sumWarGold += warReward.getRewardCount();
            } else if (warReward.getRewardType() == TheWarResourceType.TWRT_WarDoorPoint) {
                sumWarDp += warReward.getRewardCount();
            } else if (warReward.getRewardType() == TheWarResourceType.TWRT_WarHolyWater) {
                sumWarHolyWater += warReward.getRewardCount();
            } else if (warReward.getRewardType() == TheWarResourceType.TWRT_WarItem) {
                itemCfg = TheWarItemConfig.getByItemid(warReward.getRewardId());
                if (itemCfg == null) {
                    continue;
                }
                if (bShow && itemBuilder == null) {
                    itemBuilder = SC_UpdateNewItem.newBuilder();
                }
                for (int i = 0; i < warReward.getRewardCount(); i++) {
                    TheWarItemData.Builder builder = TheWarItemData.newBuilder();
                    builder.setIdx(IdGenerator.getInstance().generateId());
                    builder.setItemCfgId(warReward.getRewardId());
                    getPlayerData().getTechDbDataBuilder().putOwedItems(builder.getIdx(), builder.build());
                    itemBuilder.addItemData(builder);
                }
                addTargetProgress(TargetTypeEnum.TTE_TheWar_CumuGainTech, itemCfg.getQuality(), warReward.getRewardCount());
            }
        }
        if (bShow && itemBuilder != null && itemBuilder.getItemDataCount() > 0) {
            sendTransMsgToServer(MsgIdEnum.SC_UpdateNewItem_VALUE, itemBuilder);
        }

        CS_GS_TheWarCurrencyLog.Builder logBuilder = CS_GS_TheWarCurrencyLog.newBuilder();
        if (sumWarGold > 0) {
            int oldCount = getPlayerData().getWarGold();
            int newCount = oldCount + sumWarGold;
            CurrencyLogData.Builder goldLogBuilder = CurrencyLogData.newBuilder();
            goldLogBuilder.setCurrencyType(TheWarResourceType.TWRT_WarGold).setBeforeAmount(oldCount).setAmount(newCount).setReason(reason);
            logBuilder.addLogData(goldLogBuilder);

            getPlayerData().setWarGold(newCount);
            getPlayerData().setAccumulativeWarGold(getPlayerData().getAccumulativeWarGold() + sumWarGold);
            addTargetProgress(TargetTypeEnum.TTE_TheWar_CumuCollectTheWarGold, 0, sumWarGold);
        }

        if (sumWarDp > 0) {
            int oldCount = getPlayerData().getWarDP();
            int newCount = oldCount + sumWarDp;
            CurrencyLogData.Builder dpLogBuilder = CurrencyLogData.newBuilder();
            dpLogBuilder.setCurrencyType(TheWarResourceType.TWRT_WarDoorPoint).setBeforeAmount(oldCount).setAmount(newCount).setReason(reason);
            logBuilder.addLogData(dpLogBuilder);

            getPlayerData().setWarDP(newCount);
            getPlayerData().setAccumulativeWarDp(getPlayerData().getAccumulativeWarDp() + sumWarDp);
            addTargetProgress(TargetTypeEnum.TTE_TheWar_CumuCollectDP, 0, sumWarDp);
        }

        gainHolyWater(sumWarHolyWater);

        if (bShow) {
            updatePlayerWarCurrency();
        }

        if (logBuilder.getLogDataCount() > 0) {
            logBuilder.setPlayerIdx(getIdx());
            GlobalData.getInstance().sendMsgToServer(getServerIndex(), MsgIdEnum.CS_GS_TheWarCurrencyLog_VALUE, logBuilder);
        }
    }

    // ---------------------------------PET------------------------------
    public List<BattlePetData> buildBattlePets(List<String> petIdxList) {
        if (CollectionUtils.isEmpty(petIdxList)) {
            return Collections.EMPTY_LIST;
        }
        List<BattlePetData> petList = new ArrayList<>();
        HashSet<Integer> calcedPropSet = new HashSet<>();
        petIdxList.forEach(petIdx -> {
            BattlePetData petData = builderBattlePetData(petIdx, calcedPropSet);
            if (petData != null) {
                petList.add(petData);
            }
        });
        return petList;
    }

    public List<WarPetData> buildWarPets(List<String> petIdxList) {
        if (petIdxList == null || petIdxList.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        List<WarPetData> petList = new ArrayList<>();
        petIdxList.forEach(petIdx -> {
            WarPetData petData = getPlayerData().getPlayerPetsMap().get(petIdx);
            if (petData != null) {
                petList.add(petData);
            }
        });
        return petList;
    }

    public WarPetData.Builder addNewPetData(int pos, BattlePetData petData) {
        TheWarJobTileConfigObject cfg = TheWarJobTileConfig.getById(getJobTileLevel());
        if (cfg == null) {
            return null; // 职位配置错误
        }
//        if (getPlayerData().getPlayerPetsMap().size() >= cfg.getMaxpetcount()) {
//            return null; // 达到当前职位最大可携带宠物数
//        }
        if (pos < 0 || pos >= cfg.getMaxpetcount()) {
            return null; // 宠物背包中位置错误，大于等于最大可携带数或小于0
        }
        if (getPlayerData().getPlayerPetsMap().containsKey(petData.getPetId())) {
            return null; // 重复携带宠物
        }
        Long banExpireTime = getPlayerData().getBanedPetsMap().get(petData.getPetId());
        if (banExpireTime != null && banExpireTime > GlobalTick.getInstance().getCurrentTime()) {
            return null; // 宠物重新上阵冷却中
        }
        Optional<Entry<String, WarPetData>> petDataEntry = getPlayerData().getPlayerPetsMap().entrySet().stream()
                .filter(entry -> entry.getValue().getIndexOfList() == pos).findAny();
        if (petDataEntry.isPresent()) {
            String petIdx = petDataEntry.get().getValue().getPetId();
            if (!petIdx.equals(petData.getPetId())) {
                removePetByPos(pos);
            }
        }
        WarPetData.Builder warPet = updateNewPetData(pos, petData);
        getPlayerData().putPlayerPets(petData.getPetId(), warPet.build());

        WarPetData.Builder builder = WarPetData.newBuilder();
        builder.setPetId(warPet.getPetId());
        builder.setPetCfgId(warPet.getPetCfgId());
        builder.setPetLevel(warPet.getPetLevel());
        builder.setEvolveLv(warPet.getEvolveLv());
        builder.setPetQuality(warPet.getPetQuality());
        builder.setAwake(warPet.getAwake());
        builder.setVoidStoneId(warPet.getVoidStoneId());

        builder.setAbility(warPet.getAbility());
        builder.setPropDict(warPet.getPropDict());

        builder.setStationIndex(warPet.getStationIndex());
        builder.setStationTroopsPos(warPet.getStationTroopsPos());
        builder.setIndexOfList(warPet.getIndexOfList());
        return builder;
    }

    public WarPetData.Builder updateNewPetData(int pos, BattlePetData petData) {
        WarPetData warPet = getPlayerData().getPlayerPetsMap().get(petData.getPetId());
        WarPetData.Builder petBuilder;
        if (warPet == null) {
            petBuilder = WarPetData.newBuilder();
        } else {
            petBuilder = warPet.toBuilder();
        }
        petBuilder.setPetId(petData.getPetId());
        petBuilder.setPetCfgId(petData.getPetCfgId());
        petBuilder.setPetLevel(petData.getPetLevel());
        petBuilder.setPetQuality(petData.getPetRarity());
        petBuilder.setAbility(petData.getAbility());
        petBuilder.setAwake(petData.getAwake());
        petBuilder.setEvolveLv(petData.getEvolveLv());
        petBuilder.setStationIndex(-1);
        WarPetPropertyDict.Builder petPropDict = WarPetPropertyDict.newBuilder();
        for (int i = 0; i < petData.getPropDict().getKeysCount(); i++) {
            petPropDict.addKeys(petData.getPropDict().getKeysValue(i));
            petPropDict.addValues(petData.getPropDict().getValues(i));
        }
        petBuilder.setPropDict(petPropDict);
        if (pos >= 0) {
            petBuilder.setIndexOfList(pos);
        }
        getPlayerData().putPlayerPets(petData.getPetId(), petBuilder.build());
        return petBuilder;
    }

    public WarPetData.Builder updatePetData(BattlePetData petData) {
        WarPetData warPet = getPlayerData().getPlayerPetsMap().get(petData.getPetId());
        WarPetData.Builder petBuilder;
        if (warPet == null) {
            return null;
        } else {
            petBuilder = warPet.toBuilder();
        }
        petBuilder.setPetId(petData.getPetId());
        petBuilder.setPetCfgId(petData.getPetCfgId());
        petBuilder.setPetLevel(petData.getPetLevel());
        petBuilder.setPetQuality(petData.getPetRarity());
        petBuilder.setAbility(petData.getAbility());
        petBuilder.setAwake(petData.getAwake());
        petBuilder.setEvolveLv(petData.getEvolveLv());
        WarPetPropertyDict.Builder petPropDict = WarPetPropertyDict.newBuilder();
        for (int i = 0; i < petData.getPropDict().getKeysCount(); i++) {
            petPropDict.addKeys(petData.getPropDict().getKeysValue(i));
            petPropDict.addValues(petData.getPropDict().getValues(i));
        }
        petBuilder.setPropDict(petPropDict);
        getPlayerData().putPlayerPets(petData.getPetId(), petBuilder.build());
        return petBuilder;
    }

    public void updatePlayerBaseAdditions(Map<Integer, Integer> newAdditions) {
        if (newAdditions == null) {
            return;
        }
        getPlayerData().clearPlayerBaseAdditions();
        getPlayerData().putAllPlayerBaseAdditions(newAdditions);
    }

    public void removePetByPos(int pos) {
        for (Entry<String, WarPetData> entry : getPlayerData().getPlayerPetsMap().entrySet()) {
            WarPetData warPetDB = entry.getValue();
            if (warPetDB.getIndexOfList() != pos) {
                continue;
            }

            // 清除队伍中宠物
            for (Entry<Integer, WarTeamData> teamEntry : getPlayerData().getTeamDbDataBuilder().getTeamDataMap().entrySet()) {
                Entry<Integer, String> teamPet = teamEntry.getValue().getPetDataMap().entrySet().stream()
                        .filter(teamPetEntry -> teamPetEntry.getValue().equals(warPetDB.getPetId())).findFirst().orElse(null);
                if (teamPet != null) {
                    WarTeamData.Builder builder = teamEntry.getValue().toBuilder();
                    builder.removePetData(teamPet.getKey());
                    getPlayerData().getTeamDbDataBuilder().putTeamData(teamEntry.getKey(), builder.build());
                    break;
                }
            }
            // 增加冷却标记
            int rechargePetTime = TheWarConstConfig.getById(GameConst.ConfigId).getRechargepettime();
            getPlayerData().putBanedPets(entry.getKey(), GlobalTick.getInstance().getCurrentTime() + rechargePetTime * TimeUtil.MS_IN_A_MIN);

            // 清除驻扎格子宠物
            if (warPetDB.getStationIndex() >= 0) {
                WarMapData mapData = WarMapManager.getInstance().getRoomMapData(getRoomIdx());
                if (mapData != null) {
                    WarMapGrid stationGrid = mapData.getMapGridByPos(warPetDB.getStationTroopsPos());
                    if (stationGrid != null) {
                        Event event = Event.valueOf(EventType.ET_TheWar_ClearStationPetGrid, this, stationGrid);
                        EventManager.getInstance().dispatchEvent(event);
                    }
                }
            }

            // 移除宠物
            getPlayerData().removePlayerPets(entry.getKey());

            CS_GS_RemoveWarPetData.Builder builder = CS_GS_RemoveWarPetData.newBuilder();
            builder.setPlayerIdx(getIdx());
            builder.setRemovePetIdx(entry.getKey());
            GlobalData.getInstance().sendMsgToServer(getServerIndex(), MsgIdEnum.CS_GS_RemoveWarPetData_VALUE, builder);
            return;
        }
    }

    public BattlePetData builderBattlePetData(String petIdx, HashSet<Integer> calcedPropSet) {
        WarPetData warPet = getPlayerData().getPlayerPetsMap().get(petIdx);
        BattlePetData.Builder builder = BattlePetData.newBuilder();
        if (warPet == null || calcedPropSet == null) {
            return null;
        }
        calcedPropSet.clear();

        builder.setPetId(warPet.getPetId());
        builder.setPetCfgId(warPet.getPetCfgId());
        builder.setPetLevel(warPet.getPetLevel());
        builder.setPetRarity(warPet.getPetQuality());
        builder.setAwake(warPet.getAwake());
        builder.setEvolveLv(warPet.getEvolveLv());
        builder.setAbility(warPet.getAbility());
        PetPropertyDict.Builder petPropDict = PetPropertyDict.newBuilder();
        int propType;
        Integer addition;
        for (int i = 0; i < warPet.getPropDict().getKeysCount(); i++) {
            propType = warPet.getPropDict().getKeys(i);
            petPropDict.addKeysValue(propType);

            addition = getPlayerData().getPlayerBaseAdditionsMap().get(propType);
            if (addition != null) {
                petPropDict.addValues(warPet.getPropDict().getValues(i) + addition);
                calcedPropSet.add(propType);
            } else {
                petPropDict.addValues(warPet.getPropDict().getValues(i));
            }
        }

        // 附加属性
        for (Entry<Integer, Integer> entry : getPlayerData().getPlayerBaseAdditionsMap().entrySet()) {
            if (!calcedPropSet.contains(entry.getKey())) {
                petPropDict.addKeysValue(entry.getKey());
                petPropDict.addValues(entry.getValue());
            }
        }
        builder.setPropDict(petPropDict);
        return builder.build();
    }

    public WarPetData getWarPetData(String petIdx) {
        return getPlayerData().getPlayerPetsMap().get(petIdx);
    }

    public List<WarPetData> getWarAllPetData(List<String> petIdxList) {
        List<WarPetData> petList = new ArrayList<>();
        WarPetData petData;
        for (String petIdx : petIdxList) {
            petData = getWarPetData(petIdx);
            if (petData != null) {
                petList.add(petData);
            }
        }
        return petList;
    }

    public Map<String, WarPetData> getWarAllPetData() {
        return getPlayerData().getPlayerPetsMap();
    }

    public boolean isAllPetsFullHp() {
        for (WarPetData petData : getWarAllPetData().values()) {
            for (int i = 0; i < petData.getPropDict().getKeysCount(); i++) {
                if (petData.getPropDict().getKeys(i) == PetProperty.Current_Health_VALUE && petData.getPropDict().getValues(i) < 1000) {
                    return false;
                }
            }
        }
        return true;
    }

    public void reviveAllPets() {
        SC_UpdatePetProp.Builder builder = SC_UpdatePetProp.newBuilder();
        WarPetData.Builder petBuilder;
        WarPetPropInfo.Builder propBuilder = WarPetPropInfo.newBuilder();
        for (WarPetData petData : getWarAllPetData().values()) {
            for (int i = 0; i < petData.getPropDict().getKeysCount(); i++) {
                if (petData.getPropDict().getKeys(i) == PetProperty.Current_Health_VALUE && petData.getPropDict().getValues(i) < 1000) {
                    petBuilder = petData.toBuilder();

                    petBuilder.getPropDictBuilder().setValues(i, 1000); // 回血
                    getPlayerData().putPlayerPets(petBuilder.getPetId(), petBuilder.build());

                    propBuilder.clear();
                    propBuilder.setPetIdx(petBuilder.getPetId());
                    propBuilder.setNewPropDict(petBuilder.getPropDict());
                    builder.addPetProp(propBuilder.build());
                }
            }
        }
        sendTransMsgToServer(MsgIdEnum.SC_UpdatePetProp_VALUE, builder);
    }
    // ---------------------------------PET end------------------------------

    // ---------------------------------Team------------------------------
    public SC_QueryWarTeam.Builder buildTotalTeamInfo() {
        SC_QueryWarTeam.Builder builder = SC_QueryWarTeam.newBuilder();
        builder.setRetCode(TheWarRetCode.TWRC_Success);
        builder.setUnlockPetNum(getPlayerData().getTeamDbData().getUnlockPetNum());

        for (Entry<Integer, WarTeamData> entry : getPlayerData().getTeamDbData().getTeamDataMap().entrySet()) {
            int teamType = entry.getKey();
            WarTeamData teamData = entry.getValue();

            WarTeamInfo.Builder teamBuilder = WarTeamInfo.newBuilder();
            teamBuilder.setTeamTypeValue(teamType);

            WarTeamPetDict.Builder teamPet = WarTeamPetDict.newBuilder();
            teamPet.addAllPos(teamData.getPetDataMap().keySet());
            teamPet.addAllPetIdx(teamData.getPetDataMap().values());
            teamBuilder.setPetInfo(teamPet);

            WarTeamSkillDict.Builder teamSkill = WarTeamSkillDict.newBuilder();
            teamSkill.addAllPos(teamData.getSkillDataMap().keySet());
            teamSkill.addAllSkill(teamData.getSkillDataMap().values());
            teamBuilder.setSkillInfo(teamSkill);

            builder.addTotalTeamInfo(teamBuilder);
        }
        return builder;
    }

    public TheWarRetCode updateTeamPet(int teamType, WarTeamPetDict petDict) {
        int unlockPetCount = getPlayerData().getTeamDbData().getUnlockPetNum();
        if (petDict.getPosCount() > unlockPetCount) {
            return TheWarRetCode.TWRC_LimitJobTileTeamPetNum; // 队伍数量达到当前职位最大上阵宠物数
        }
        WarRoom warRoom = WarRoomCache.getInstance().queryObject(getRoomIdx());
        if (warRoom == null) {
            return TheWarRetCode.TWRC_RoomNotFound; // 未找到战戈房间信息
        }
        WarMapData mapData = WarMapManager.getInstance().getRoomMapData(warRoom.getIdx());
        if (mapData == null) {
            return TheWarRetCode.TWRC_NotFoundWarMap; // 未找到战戈地图
        }

        WarTeamData.Builder teamBuilder;
        WarTeamData teamData = getPlayerData().getTeamDbDataBuilder().getTeamDataMap().get(teamType);
        if (teamData != null) {
            teamBuilder = teamData.toBuilder();
            teamBuilder.clearPetData();
        } else {
            teamBuilder = WarTeamData.newBuilder();
        }
        WarMapGrid troopsGrid;
        WarPetData warPetData;
        Map<Position, Set<WarPetData>> removedTroopsPetGridMap = null;
        for (int i = 0; i < petDict.getPosCount(); i++) {
            int pos = petDict.getPos(i);
            if (pos >= unlockPetCount) {
                return TheWarRetCode.TWRC_TeamPetPosError; // 编辑孔位达到当前职位最大上阵宠物数
            }
            String petIdx = petDict.getPetIdx(i);
            warPetData = getWarPetData(petIdx);
            if (warPetData == null) {
                return TheWarRetCode.TWRC_ExistInvalidPet; // 包含未携带或未拥有宠物
            }
            if (warPetData.getStationIndex() >= 0) {
                troopsGrid = mapData.getMapGridByPos(warPetData.getStationTroopsPos());
                if (troopsGrid.getPropValue(TheWarCellPropertyEnum.TWCP_BattlingTarget_VALUE) > 0) {
                    return TheWarRetCode.TWRC_PetTroopsInBattlingGird; // 存在宠物驻防在正在战斗的格子中
                }
                if (removedTroopsPetGridMap == null) {
                    removedTroopsPetGridMap = new HashMap<>();
                }
                Set<WarPetData> petSet = removedTroopsPetGridMap.get(warPetData.getStationTroopsPos());
                if (petSet == null) {
                    petSet = new HashSet<>();
                    removedTroopsPetGridMap.put(warPetData.getStationTroopsPos(), petSet);
                }
                petSet.add(warPetData);
            }
            teamBuilder.putPetData(pos, petIdx);
        }
        getPlayerData().getTeamDbDataBuilder().putTeamData(teamType, teamBuilder.build());
        if (removedTroopsPetGridMap != null) {
            Event event = Event.valueOf(EventType.ET_TheWar_RemoveTroopsPetFromGrid, this, GameUtil.getDefaultEventSource());
            event.pushParam(removedTroopsPetGridMap);
            EventManager.getInstance().dispatchEvent(event);

            LogUtil.debug("Remove Troops pets:" + removedTroopsPetGridMap);
        }
        SC_UpdateWarFightTeam.Builder builder = SC_UpdateWarFightTeam.newBuilder();
        for (Entry<Integer, String> entry : teamBuilder.getPetDataMap().entrySet()) {
            builder.getPetInfoBuilder().addPos(entry.getKey()).addPetIdx(entry.getValue());
        }
        sendTransMsgToServer(MsgIdEnum.SC_UpdateWarFightTeam_VALUE, builder);
        return TheWarRetCode.TWRC_Success;
    }

    public TheWarRetCode updateTeamSkill(int teamType, WarTeamSkillDict skillDict) {
        WarTeamData.Builder teamBuilder;
        WarTeamData teamData = getPlayerData().getTeamDbDataBuilder().getTeamDataMap().get(teamType);
        if (teamData != null) {
            teamBuilder = teamData.toBuilder();
            teamBuilder.clearSkillData();
        } else {
            teamBuilder = WarTeamData.newBuilder();
        }
        for (int i = 0; i < skillDict.getPosCount(); i++) {
            int pos = skillDict.getPos(i);
            if (pos > TeamSkillPositionEnum.TSPE_Position_2_VALUE) {
                return TheWarRetCode.TWRC_TeamSkillPosError; // 技能位置错误
            }
            SkillBattleDict skillInfo = skillDict.getSkill(i);
            teamBuilder.putSkillData(pos, skillInfo);
        }
        getPlayerData().getTeamDbDataBuilder().putTeamData(teamType, teamBuilder.build());
        return TheWarRetCode.TWRC_Success;
    }

    public List<BattlePetData> getTeamBattlePets(int teamType) {
        WarTeamData teamData = getPlayerData().getTeamDbData().getTeamDataMap().get(teamType);
        if (teamData == null) {
            return Collections.EMPTY_LIST;
        }
        return buildBattlePets(teamData.getPetDataMap().values().stream().distinct().collect(Collectors.toList()));
    }

    public boolean isPetInTeam(int teamType, String petIdx) {
        WarTeamData teamData = getPlayerData().getTeamDbData().getTeamDataMap().get(teamType);
        if (teamData == null) {
            return false;
        }
        return teamData.getPetDataMap().containsValue(petIdx);
    }

    public List<WarPetData> getTeamWarPets(int teamType) {
        WarTeamData teamData = getPlayerData().getTeamDbData().getTeamDataMap().get(teamType);
        if (teamData == null) {
            return Collections.EMPTY_LIST;
        }
        return buildWarPets(teamData.getPetDataMap().values().stream().distinct().collect(Collectors.toList()));
    }

    public List<SkillBattleDict> getTeamSkillDict(int teamType) {
        WarTeamData teamData = getPlayerData().getTeamDbDataBuilder().getTeamDataMap().get(teamType);
        if (teamData == null) {
            return Collections.EMPTY_LIST;
        }
        List<SkillBattleDict> skillList = new ArrayList<>();
        for (Entry<Integer, SkillBattleDict> entry : teamData.getSkillDataMap().entrySet()) {
            SkillBattleDict.Builder builder = SkillBattleDict.newBuilder().setSkillId(entry.getValue().getSkillId()).setSkillLv(entry.getValue().getSkillLv());
            skillList.add(entry.getKey(), builder.build());
        }
        return skillList;
    }

    public void removePetFromTeam(int teamType, String petIdx) {
        WarTeamData teamData = getPlayerData().getTeamDbDataBuilder().getTeamDataMap().get(teamType);
        if (teamData == null) {
            return;
        }

        for (Entry<Integer, String> entry : teamData.getPetDataMap().entrySet()) {
            if (entry.getValue().equals(petIdx)) {
                WarTeamData.Builder teamBuilder = teamData.toBuilder();
                teamBuilder.removePetData(entry.getKey());
                getPlayerData().getTeamDbDataBuilder().putTeamData(teamType, teamBuilder.build());
                break;
            }
        }
    }

    // ---------------------------------Team end------------------------------

    // ---------------------------------Item------------------------------
    // 穿装备
    public TheWarRetCode equipOnItem(CS_EquipOnItem req) {
        TheWarItemData itemData = getPlayerData().getTechDbData().getOwedItemsMap().get(req.getEquipItemIdx());
        if (itemData == null) {
            return TheWarRetCode.TWRC_NotOwnedItem; // 未拥有道具
        }
        Map<Integer, WarProfessionData> professionDataMap = getPlayerData().getTechDbData().getProfessionDataMap();
        WarProfessionData profData = professionDataMap.get(req.getProfessionType());
        if (profData == null) {
            return TheWarRetCode.TWRC_ItemDataError; // 种族信息错误
        }

        TheWarTechConfigObject warTechCfg = TheWarTechConfig.getInstance().getTechCfgByRaceAndLevel(req.getProfessionType(), profData.getTechnicalLevel());
        if (warTechCfg == null) {
            return TheWarRetCode.TWRC_ItemCfgError; // 未找到配置错误
        }

        TheWarItemConfigObject itemCfg = TheWarItemConfig.getByItemid(itemData.getItemCfgId());
        if (itemCfg == null) {
            return TheWarRetCode.TWRC_ItemCfgError; // 未找到配置错误
        }
        if (itemCfg.getProdefine() != req.getProfessionType()) {
            return TheWarRetCode.TWRC_ItemProfessionNotMatch; // 道具不可装备该种族
        }
        if (itemCfg.getPosdefine() != req.getItemPosValue()) {
            return TheWarRetCode.TWRC_ItemPosNotMatch; // 道具不可装备该位置
        }
        if (warTechCfg.getNeedquality() != itemCfg.getQuality()) {
            return TheWarRetCode.TWRC_ItemQualityError; // 道具品质不匹配
        }
        if (itemData.getEquippedProfession() > 0) {
            return TheWarRetCode.TWRC_ItemEquipped; // 该装备已经装备到其他职业
        }

        WarProfessionData.Builder profDataBuilder = profData.toBuilder();
        profDataBuilder.putEquipItems(req.getItemPosValue(), req.getEquipItemIdx());
        //添加装备效果
        if (itemCfg.getBuffid() > 0) {
            profDataBuilder.addTechBuff(itemCfg.getBuffid());
        }
        getPlayerData().getTechDbDataBuilder().putProfessionData(req.getProfessionType(), profDataBuilder.build());
        // 道具标记为已装备
        TheWarItemData.Builder itemBuilder = itemData.toBuilder();
        itemBuilder.setEquippedProfession(req.getProfessionType());
        getPlayerData().getTechDbDataBuilder().putOwedItems(itemBuilder.getIdx(), itemBuilder.build());
        addTargetProgress(TargetTypeEnum.TTE_TheWar_EquipOnItem, itemCfg.getQuality(), 1);
        return TheWarRetCode.TWRC_Success;
    }

    // 脱装备
    public TheWarRetCode equipOffItem(CS_EquipOffItem req) {
        WarProfessionData profData = getPlayerData().getTechDbData().getProfessionDataMap().get(req.getProfessionType());
        if (profData == null) {
            return TheWarRetCode.TWRC_ItemDataError; // 种族信息错误
        }
        WarProfessionData.Builder profDataBuilder = profData.toBuilder();
        String itemIdx = profDataBuilder.getEquipItemsMap().get(req.getItemPosValue());
        if (StringHelper.isNull(itemIdx)) {
            return TheWarRetCode.TWRC_ItemNotEquipped; // 未装备道具
        }
        TheWarItemData itemData = getPlayerData().getTechDbData().getOwedItemsMap().get(itemIdx);
        if (itemData != null) {
            //移除装备效果
            TheWarItemConfigObject itemCfg = TheWarItemConfig.getByItemid(itemData.getItemCfgId());
            if (itemCfg != null && itemCfg.getBuffid() > 0) {
                for (int i = 0; i < profDataBuilder.getTechBuffCount(); i++) {
                    if (profDataBuilder.getTechBuff(i) == itemCfg.getBuffid()) {
                        profDataBuilder.getTechBuffList().remove(i);
                        break;
                    }
                }
            }
            // 移除装备标记
            TheWarItemData.Builder itemBuilder = itemData.toBuilder();
            itemBuilder.setEquippedProfession(0);
            getPlayerData().getTechDbDataBuilder().putOwedItems(itemBuilder.getIdx(), itemBuilder.build());
        }
        profDataBuilder.removeEquipItems(req.getItemPosValue());
        getPlayerData().getTechDbDataBuilder().putProfessionData(req.getProfessionType(), profDataBuilder.build());
        return TheWarRetCode.TWRC_Success;
    }

    protected boolean checkMaterialItemEnough(int itemCfgId, int itemCount, Map<Integer, Integer> removeItemMap) {
        TheWarItemConfigObject cfg = TheWarItemConfig.getByItemid(itemCfgId);
        if (cfg == null || CollectionUtils.isEmpty(cfg.getComposite())) {
            return false;
        }
        int needCount;
        for (Entry<Integer, Integer> entry : cfg.getComposite().entrySet()) {
            long ownedCount = getPlayerData().getTechDbData().getOwedItemsMap().values()
                    .stream().filter(itemData -> itemData.getItemCfgId() == entry.getKey()).count();
            needCount = entry.getValue() * itemCount;
            if (needCount == 0 || ownedCount < needCount) {
                return false;
            }
            removeItemMap.put(entry.getKey(), needCount);
        }
        return true;
    }

    protected int checkMaterialItemEnoughWithGold(int itemCfgId, int itemCount, Map<Integer, Integer> removeItemMap) {
        TheWarItemConfigObject cfg = TheWarItemConfig.getByItemid(itemCfgId);
        if (cfg == null || cfg.getComposite() == null) {
            return -1;
        }
        int needCount;
        int sumCost = 0;
        TheWarItemConfigObject itemCfg;
        if (cfg.getComposite().isEmpty()) {
            return cfg.getPrice() * itemCount;
        }
        for (Entry<Integer, Integer> entry : cfg.getComposite().entrySet()) {
            int ownedCount = (int) getPlayerData().getTechDbData().getOwedItemsMap().values()
                    .stream().filter(itemData -> itemData.getItemCfgId() == itemCfgId).count();
            needCount = entry.getValue() * itemCount;
            int deltaCount = ownedCount - needCount;
            if (deltaCount >= 0) {
                removeItemMap.put(entry.getKey(), needCount);
            } else {
                int costCount = needCount - ownedCount;
                removeItemMap.put(entry.getKey(), ownedCount);
                itemCfg = TheWarItemConfig.getByItemid(entry.getKey());
                if (itemCfg != null) {
                    sumCost += itemCfg.getPrice() * costCount;
                }
            }
        }
        return sumCost;
    }

    // 合成装备
    public TheWarRetCode composeItem(CS_ComposeNewItem req, Set<String> removeIdxList) {
        // 判断是否拥有所需合成的所有道具材料
//        if (!getPlayerData().getTechDbData().getProfessionDataMap().values().containsAll(req.getItemMaterialsList())) {
//            return TheWarRetCode.RCE_ErrorParam; // 未装备道具
//        }
        TheWarItemConfigObject itemCfg = TheWarItemConfig.getByItemid(req.getComposeItemCfgId());
        if (itemCfg == null) {
            return TheWarRetCode.TWRC_ConfigNotFound;
        }
        if (removeIdxList == null) {
            return TheWarRetCode.TWRC_ErrorParam;
        }
        if (req.getComposeNum() <= 0) {
            return TheWarRetCode.TWRC_InvalidComposeItemCount;
        }
        int costGold;
        Map<Integer, Integer> removeItemMap = new HashMap<>();
        if (req.getUseWarGold()) {
            costGold = checkMaterialItemEnoughWithGold(req.getComposeItemCfgId(), req.getComposeNum(), removeItemMap);
            if (getPlayerData().getWarGold() < costGold) {
                return TheWarRetCode.TWRC_WarGoldNotEnough; // 金币不足
            }
            // 扣除金币
            int oldGold = getPlayerData().getWarGold();
            int newGold = oldGold - costGold;
            getPlayerData().setWarGold(newGold);
            updatePlayerWarCurrency();

            CS_GS_TheWarCurrencyLog.Builder logBuilder = CS_GS_TheWarCurrencyLog.newBuilder();
            CurrencyLogData.Builder goldLogBuilder = CurrencyLogData.newBuilder();
            goldLogBuilder.setCurrencyType(TheWarResourceType.TWRT_WarGold).setConsume(true).setBeforeAmount(oldGold).setAmount(newGold).setReason("合成军备");
            logBuilder.addLogData(goldLogBuilder);
            logBuilder.setPlayerIdx(getIdx());
            GlobalData.getInstance().sendMsgToServer(getServerIndex(), MsgIdEnum.CS_GS_TheWarCurrencyLog_VALUE, logBuilder);
        } else {
            if (!checkMaterialItemEnough(req.getComposeItemCfgId(), req.getComposeNum(), removeItemMap)) {
                return TheWarRetCode.TWRC_ItemMaterialNotEnough; // 道具不足
            }
        }

        for (Entry<Integer, Integer> entry : removeItemMap.entrySet()) {
            int itemCfgId = entry.getKey();
            int itemCount = entry.getValue();
            Stream<Entry<String, TheWarItemData>> owedItemStream = getPlayerData().getTechDbDataBuilder().getOwedItemsMap().entrySet().stream();
            Stream<Entry<String, TheWarItemData>> removeItemStream = owedItemStream.filter(itemEntry ->
                    itemEntry.getValue().getItemCfgId() == itemCfgId).limit(itemCount);
            removeIdxList.addAll(removeItemStream.map(itemEntry -> itemEntry.getKey()).collect(Collectors.toSet()));
        }
        // 移除道具
        removeIdxList.forEach(itemIdx -> getPlayerData().getTechDbDataBuilder().removeOwedItems(itemIdx));
        // 生成新道具
        SC_UpdateNewItem.Builder updateItem = SC_UpdateNewItem.newBuilder();
        for (int i = 0; i < req.getComposeNum(); i++) {
            TheWarItemData.Builder builder = TheWarItemData.newBuilder();
            builder.setIdx(IdGenerator.getInstance().generateId());
            builder.setItemCfgId(req.getComposeItemCfgId());
            getPlayerData().getTechDbDataBuilder().putOwedItems(builder.getIdx(), builder.build());

            updateItem.addItemData(builder);
        }

        addTargetProgress(TargetTypeEnum.TTE_TheWar_CumuComposeTech, itemCfg.getQuality(), req.getComposeNum());
        addTargetProgress(TargetTypeEnum.TTE_TheWar_CumuGainTech, itemCfg.getQuality(), req.getComposeNum());

        sendTransMsgToServer(MsgIdEnum.SC_UpdateNewItem_VALUE, updateItem);
        return TheWarRetCode.TWRC_Success;
    }

    // 升级科技等级
    public TheWarRetCode levelUpTechnology(int professionType) {
        int raceMaxLevel = TheWarTechConfig.getInstance().getRaceMaxLevel(professionType);
        if (raceMaxLevel <= 0) {
            return TheWarRetCode.TWRC_TechCfgError; // 科技种族配置错误,未找到科技对应种族的最大等级配置
        }
        WarProfessionData profData = getPlayerData().getTechDbData().getProfessionDataMap().get(professionType);
        WarProfessionData.Builder profDataBuilder;
        if (profData != null) {
            profDataBuilder = profData.toBuilder();
        } else {
            profDataBuilder = WarProfessionData.newBuilder();
        }
        TheWarJobTileConfigObject jobTileCfg = TheWarJobTileConfig.getById(getJobTileLevel());
        if (jobTileCfg == null) {
            return TheWarRetCode.TWRC_JobTileCfgError; // 职位配置错误,未找到职位限制最大等级
        }
        if (profDataBuilder.getTechnicalLevel() >= jobTileCfg.getMaxtechlevel()) {
            return TheWarRetCode.TWRC_LimitJobTileLevel; // 达到职位限制的最大科技等级

        }
        int oldLevel = profDataBuilder.getTechnicalLevel();
        if (oldLevel >= raceMaxLevel) {
            return TheWarRetCode.TWRC_MaxProfessionLevel; // 达到当前种族最大等级
        }
        if (oldLevel > 0) {
            for (int i = TheWarItemPos.TWIP_Head_VALUE; i < TheWarItemPos.TWIP_Foot_VALUE; i++) {
                String itemIdx = profDataBuilder.getEquipItemsMap().get(i);
                if (StringHelper.isNull(itemIdx)) {
                    return TheWarRetCode.TWRC_NotEquipEveryItemPos; // 装备位置未装备满
                }
            }
        }

        // 清道具,清技能及其buff,升级
        for (String itemIdx : profDataBuilder.getEquipItemsMap().values()) {
            getPlayerData().getTechDbDataBuilder().removeOwedItems(itemIdx);
        }

        int newLevel = oldLevel + 1;
        profDataBuilder.clear();
        profDataBuilder.setTechnicalLevel(newLevel);

        TheWarTechConfigObject cfg = TheWarTechConfig.getInstance().getTechCfgByRaceAndLevel(professionType, newLevel);
        if (cfg != null) {
            if (cfg.getBasebuffid() != null && cfg.getBasebuffid().length > 0) {
                for (int i = 0; i < cfg.getBasebuffid().length; i++) {
                    int buffId = cfg.getBasebuffid()[i];
                    if (!profDataBuilder.getTechBuffList().contains(buffId)) {
                        profDataBuilder.addTechBuff(buffId);
                    }
                }
            }
            if (cfg.getSkilllist() != null) {
                for (int i = 0; i < cfg.getSkilllist().length; i++) {
                    int[] skillInfo = cfg.getSkilllist()[i];
                    if (skillInfo == null || skillInfo.length < 2) {
                        continue;
                    }
                    TheWarSkillData.Builder skillBuilder = TheWarSkillData.newBuilder();
                    skillBuilder.setSkillId(skillInfo[0]);
                    skillBuilder.setLevel(skillInfo[1]);
                    int buffId = TheWarSkillConfig.getInstance().getBuffListBySkillIdAndLevel(skillBuilder.getSkillId(), skillBuilder.getLevel());
                    if (buffId > 0) {
                        skillBuilder.addSkillBuff(buffId);
                    }
                    profDataBuilder.addSkillData(skillBuilder);
                }
            }
        }
        if (newLevel > 1) {
            WarRoom warRoom = WarRoomCache.getInstance().queryObject(getRoomIdx());
            if (warRoom != null) {
                warRoom.broadcastTips(EnumTheWarTips.EMTW_PromoteTech_VALUE, true, getIdx(), professionType, newLevel);
            }
        }

        addTargetProgress(TargetTypeEnum.TTE_TheWar_TechLevelUp, professionType, newLevel);

        getPlayerData().getTechDbDataBuilder().putProfessionData(professionType, profDataBuilder.build());
        return TheWarRetCode.TWRC_Success;
    }

    public TheWarTechnicalData.Builder buildTechnicalData() {
        TheWarTechnicalData.Builder builder = TheWarTechnicalData.newBuilder();
        for (Entry<Integer, WarProfessionData> techEntry : getPlayerData().getTechDbData().getProfessionDataMap().entrySet()) {
            builder.addProfessionType(techEntry.getKey());
            WarProfessionData profData = techEntry.getValue();

            TheWarTechData.Builder techData = TheWarTechData.newBuilder();
            techData.setTechnicalLevel(profData.getTechnicalLevel());
            for (Entry<Integer, String> itemEntry : profData.getEquipItemsMap().entrySet()) {
                TheWarItemData item = getPlayerData().getTechDbData().getOwedItemsMap().get(itemEntry.getValue());
                if (item == null) {
                    continue;
                }
                techData.addItemPosValue(itemEntry.getKey());
                techData.addItemIdx(item.getIdx());
            }
            techData.addAllSkillData(profData.getSkillDataList());
            builder.addTechData(techData);
        }
        return builder;
    }
    // ---------------------------------Item end------------------------------

    // ---------------------------------JobTile------------------------------
    public TheWarRetCode promoteJobTile(boolean gmPromote) {
        if (getJobTileLevel() >= TheWarJobTileConfig.getInstance().getMaxJobTileLevel()) {
            return TheWarRetCode.TWRC_MaxJobTileLevel; // 达到最大职位等级
        }
        if (!gmPromote && getPlayerData().getJobTileTaskData().getTaskState() == WarTaskStateEnum.WTSE_NotFinish) {
            return TheWarRetCode.TWRC_NotFinishEveryJobTileTask; // 任务未完成
        }

        int newJobTileLevel = getJobTileLevel() + 1;
        setJobTileLevel(newJobTileLevel);
        TheWarJobTileConfigObject newCfg = TheWarJobTileConfig.getById(newJobTileLevel);
        if (newCfg != null) {
            //发送职位奖励
            CS_GS_GetJobTileReward.Builder rewardBuilder = CS_GS_GetJobTileReward.newBuilder();
            rewardBuilder.setJobTileLevel(getJobTileLevel());
            rewardBuilder.setPlayerIdx(getIdx());
            GlobalData.getInstance().sendMsgToServer(getServerIndex(), MsgIdEnum.CS_GS_GetJobTileReward_VALUE, rewardBuilder);

            getPlayerData().getJobTileTaskDataBuilder().setTaskState(WarTaskStateEnum.WTSE_Claimed);
            getPlayerData().getJobTileTaskDataBuilder().clearTargetGroup();
            getPlayerData().getTeamDbDataBuilder().setUnlockPetNum(newCfg.getTeammaxpetcount());
        }

        WarRoom warRoom = WarRoomCache.getInstance().queryObject(getRoomIdx());
        if (warRoom != null) {
            warRoom.broadcastTips(EnumTheWarTips.EMTW_PromoteJobTile_VALUE, true, getIdx(), getJobTileLevel());
        }

        TheWarJobTileConfigObject nextCfg = TheWarJobTileConfig.getById(newJobTileLevel + 1);
        if (nextCfg != null) {
            addNewJobTileTask(nextCfg.getAchievecondition());
        }
        getPlayerData().getJobTileTaskDataBuilder().setTaskState(WarTaskStateEnum.WTSE_NotFinish);

        addTargetProgress(TargetTypeEnum.TTE_TheWar_JobTileLvReach, 0, getJobTileLevel());
        return TheWarRetCode.TWRC_Success;
    }

    public void addNewJobTileTask(int[] taskList) {
        if (taskList == null || taskList.length <= 0) {
            return;
        }
        TheWarTargetConfigObject cfg;
        boolean allFinished = true;
        for (int i = 0; i < taskList.length; i++) {
            cfg = TheWarTargetConfig.getById(taskList[i]);
            if (cfg == null) {
                continue;
            }
            WarTarget.Builder taskBuilder = WarTarget.newBuilder();
            taskBuilder.setCfgId(cfg.getId());
            if (cfg.getMissiontype() == TargetTypeEnum.TTE_TheWar_CumuCollectTheWarGold_VALUE) {
                int progress = (int) getPlayerData().getAccumulativeWarGold();
                if (progress >= cfg.getTargetcount()) {
                    taskBuilder.setProgress(cfg.getTargetcount());
                    taskBuilder.setFinished(true);
                } else {
                    taskBuilder.setProgress(progress);
                }
            } else if (cfg.getMissiontype() == TargetTypeEnum.TTE_TheWar_CumuCollectDP_VALUE) {
                int progress = (int) getPlayerData().getAccumulativeWarDp();
                if (progress >= cfg.getTargetcount()) {
                    taskBuilder.setProgress(cfg.getTargetcount());
                    taskBuilder.setFinished(true);
                } else {
                    taskBuilder.setProgress(progress);
                }
            }
            if (!taskBuilder.getFinished()) {
                allFinished = false;
            }
            getPlayerData().getJobTileTaskDataBuilder().putTargetGroup(cfg.getId(), taskBuilder.build());
        }
        getPlayerData().getJobTileTaskDataBuilder().setTaskState(allFinished ? WarTaskStateEnum.WTSE_Finish : WarTaskStateEnum.WTSE_NotFinish);
    }

    public void addOccupyGridTargetProp(FootHoldGrid fhGrid, String ownerName, boolean hasTroops) {
        if (fhGrid == null) {
            return;
        }
        int gridLevel = (int) fhGrid.getPropValue(TheWarCellPropertyEnum.TWCP_Level_VALUE);
        int tag = (int) fhGrid.getPropValue(TheWarCellPropertyEnum.TWCP_CellTag_VALUE);
        TargetTypeEnum targetTypeEnum = TargetTypeEnum.TTE_NULL;

        //占领不区分阵营
        if ((tag & WarCellTagFlag.WCTF_Normal_Manor_VALUE) > 0) {
            targetTypeEnum = TargetTypeEnum.TTE_TheWar_CumuOccupy_FootHoldGrid_Common;
        } else if ((tag & WarCellTagFlag.WCTF_WarGold_Mine_VALUE) > 0) {
            targetTypeEnum = TargetTypeEnum.TTE_TheWar_CumuOccupy_FootHoldGrid_WarGold;
        } else if ((tag & WarCellTagFlag.WCTF_OpenDoor_Mine_VALUE) > 0) {
            targetTypeEnum = TargetTypeEnum.TTE_TheWar_CumuOccupy_FootHoldGrid_DpResource;
        } else if ((tag & WarCellTagFlag.WCTF_HolyWater_Mine_VALUE) > 0) {
            targetTypeEnum = TargetTypeEnum.TTE_TheWar_CumuOccupy_FootHoldGrid_HolyWater;
        } else if ((tag & WarCellTagFlag.WCTF_Fortress_VALUE) > 0) {
            targetTypeEnum = TargetTypeEnum.TTE_TheWar_CumuOccupy_FootHoldGrid_BossGrid;
        }
        addTargetProgress(TargetTypeEnum.TTE_TheWar_CumuOccupy_FootHoldGrid, gridLevel, 1);
        addTargetProgress(targetTypeEnum, gridLevel, 1,
                StringHelper.IntTostring(fhGrid.getPos().getX(), ""), StringHelper.IntTostring(fhGrid.getPos().getY(), ""), "", String.valueOf(hasTroops));

        if (!StringHelper.isNull(ownerName)) {
            if ((tag & WarCellTagFlag.WCTF_Normal_Manor_VALUE) > 0) {
                targetTypeEnum = TargetTypeEnum.TTE_TheWar_CumuOccupy_Enemy_FootHoldGrid_Common;
            } else if ((tag & WarCellTagFlag.WCTF_WarGold_Mine_VALUE) > 0) {
                targetTypeEnum = TargetTypeEnum.TTE_TheWar_CumuOccupy_Enemy_FootHoldGrid_WarGold;
            } else if ((tag & WarCellTagFlag.WCTF_OpenDoor_Mine_VALUE) > 0) {
                targetTypeEnum = TargetTypeEnum.TTE_TheWar_CumuOccupy_Enemy_FootHoldGrid_DpResource;
            } else if ((tag & WarCellTagFlag.WCTF_HolyWater_Mine_VALUE) > 0) {
                targetTypeEnum = TargetTypeEnum.TTE_TheWar_CumuOccupy_Enemy_FootHoldGrid_HolyWater;
            } else if ((tag & WarCellTagFlag.WCTF_Fortress_VALUE) > 0) {
                targetTypeEnum = TargetTypeEnum.TTE_TheWar_CumuOccupy_Enemy_FootHoldGrid_BossGrid;
            }

            addTargetProgress(TargetTypeEnum.TTE_TheWar_CumuOccupy_Enemy_FootHoldGrid, gridLevel, 1);
            addTargetProgress(targetTypeEnum, gridLevel, 1,
                    StringHelper.IntTostring(fhGrid.getPos().getX(), ""), StringHelper.IntTostring(fhGrid.getPos().getY(), ""), ownerName, String.valueOf(hasTroops));
        }
    }

    public void addAttackGridTargetProp(FootHoldGrid fhGrid, String ownerName, boolean hasTroops, boolean attackResult) {
        if (fhGrid == null) {
            return;
        }
        int gridLevel = (int) fhGrid.getPropValue(TheWarCellPropertyEnum.TWCP_Level_VALUE);
        int tag = (int) fhGrid.getPropValue(TheWarCellPropertyEnum.TWCP_CellTag_VALUE);
        TargetTypeEnum targetTypeEnum = TargetTypeEnum.TTE_NULL;
        if ((tag & WarCellTagFlag.WCTF_Normal_Manor_VALUE) > 0) {
            targetTypeEnum = TargetTypeEnum.TTE_TheWar_AttackGrid_FootHoldGrid_Common;
        } else if ((tag & WarCellTagFlag.WCTF_WarGold_Mine_VALUE) > 0) {
            targetTypeEnum = TargetTypeEnum.TTE_TheWar_AttackGrid_FootHoldGrid_WarGold;
        } else if ((tag & WarCellTagFlag.WCTF_OpenDoor_Mine_VALUE) > 0) {
            targetTypeEnum = TargetTypeEnum.TTE_TheWar_AttackGrid_FootHoldGrid_HolyWater;
        } else if ((tag & WarCellTagFlag.WCTF_HolyWater_Mine_VALUE) > 0) {
            targetTypeEnum = TargetTypeEnum.TTE_TheWar_AttackGrid_FootHoldGrid_DpResource;
        } else if ((tag & WarCellTagFlag.WCTF_Fortress_VALUE) > 0) {
            targetTypeEnum = TargetTypeEnum.TTE_TheWar_AttackGrid_FootHoldGrid_BossGrid;
        }
        addTargetProgress(targetTypeEnum, gridLevel, 1,
                StringHelper.IntTostring(fhGrid.getPos().getX(), ""), StringHelper.IntTostring(fhGrid.getPos().getY(), ""), ownerName, String.valueOf(hasTroops), String.valueOf(attackResult));
    }

    public void addTroopsGridTargetProp(FootHoldGrid fhGrid) {
        if (fhGrid == null) {
            return;
        }
        int gridLevel = (int) fhGrid.getPropValue(TheWarCellPropertyEnum.TWCP_Level_VALUE);
        int tag = (int) fhGrid.getPropValue(TheWarCellPropertyEnum.TWCP_CellTag_VALUE);
        TargetTypeEnum targetTypeEnum = TargetTypeEnum.TTE_NULL;
        if ((tag & WarCellTagFlag.WCTF_Normal_Manor_VALUE) > 0) {
            targetTypeEnum = TargetTypeEnum.TTE_TheWar_CumuStationTroops_FootHoldGrid_Common;
        } else if ((tag & WarCellTagFlag.WCTF_WarGold_Mine_VALUE) > 0) {
            targetTypeEnum = TargetTypeEnum.TTE_TheWar_CumuStationTroops_FootHoldGrid_WarGold;
        } else if ((tag & WarCellTagFlag.WCTF_OpenDoor_Mine_VALUE) > 0) {
            targetTypeEnum = TargetTypeEnum.TTE_TheWar_CumuStationTroops_FootHoldGrid_DpResource;
        } else if ((tag & WarCellTagFlag.WCTF_HolyWater_Mine_VALUE) > 0) {
            targetTypeEnum = TargetTypeEnum.TTE_TheWar_CumuStationTroops_FootHoldGrid_HolyWater;
        } else if ((tag & WarCellTagFlag.WCTF_Fortress_VALUE) > 0) {
            targetTypeEnum = TargetTypeEnum.TTE_TheWar_CumuStationTroops_BossGrid;
        }

        addTargetProgress(TargetTypeEnum.TTE_TheWar_CumuStationTroops_FootHoldGrid, gridLevel, 1);
        addTargetProgress(targetTypeEnum, gridLevel, 1);
    }

    public void addTargetProgress(TargetTypeEnum targetType, int targetParam, int addCount, String... extraParams) {
        int targetTypeVal = targetType.getNumber();
        if (targetTypeVal < TargetTypeEnum.TTE_TheWar_CumuSubmitDp_VALUE || targetTypeVal > TargetTypeEnum.TTE_TheWar_EquipOnItem_VALUE) {
            return;
        }
        sendUpdateTargetProMsg(targetType, addCount, targetParam, extraParams);
//        if (targetTypeVal >= TargetTypeEnum.TTE_TheWar_AttackGrid_FootHoldGrid_Common_VALUE) {
//            return;
//        }
        if (getPlayerData().getJobTileTaskData().getTaskState() == WarTaskStateEnum.WTSE_NotFinish) {
            addJobTileTaskProgress(targetType, targetParam, addCount);
        }
        addSeasonMissionProgress(targetType, targetParam, addCount);
    }

    protected void addJobTileTaskProgress(TargetTypeEnum targetType, int targetParam, int addCount) {
        int finishCount = 0;
        TheWarTargetConfigObject cfg;
        WarTarget.Builder targetBuilder;
        WarJobTileTask.Builder taskBuilder = null;
        for (Entry<Integer, WarTarget> entry : getPlayerData().getJobTileTaskDataBuilder().getTargetGroupMap().entrySet()) {
            cfg = TheWarTargetConfig.getById(entry.getKey());
            if (cfg == null) {
                continue;
            }
            if (entry.getValue().getFinished()) {
                ++finishCount;
                continue;
            }
            if (cfg.getMissiontype() != targetType.getNumber() || (cfg.getAddtion() > 0 && cfg.getAddtion() > targetParam)) { // 暂时只需大于等于
                continue;
            }
            targetBuilder = entry.getValue().toBuilder();
            targetBuilder.setProgress(Integer.min(cfg.getTargetcount(), targetBuilder.getProgress() + addCount));
            if (targetBuilder.getProgress() >= cfg.getTargetcount()) {
                targetBuilder.setFinished(true);
                ++finishCount;
            }
            if (taskBuilder == null) {
                taskBuilder = WarJobTileTask.newBuilder();
            }
            taskBuilder.addTargetGroup(targetBuilder);
        }
        if (taskBuilder != null) {
            for (WarTarget target : taskBuilder.getTargetGroupList()) {
                getPlayerData().getJobTileTaskDataBuilder().putTargetGroup(target.getCfgId(), target);
            }
        }
        if (finishCount == getPlayerData().getJobTileTaskData().getTargetGroupCount()) {
            getPlayerData().getJobTileTaskDataBuilder().setTaskState(WarTaskStateEnum.WTSE_Finish);
            if (taskBuilder == null) {
                taskBuilder = WarJobTileTask.newBuilder();
            }
            taskBuilder.setTaskState(WarTaskStateEnum.WTSE_Finish);
        }
        if (taskBuilder != null && isOnline()) {
            SC_JobTileTaskData.Builder builder = SC_JobTileTaskData.newBuilder();
            builder.setJobTileTask(taskBuilder);
            sendTransMsgToServer(MsgIdEnum.SC_JobTileTaskData_VALUE, builder);
        }
    }

    protected void addSeasonMissionProgress(TargetTypeEnum targetType, int targetParam, int addCount) {
        boolean needUpdate = false;
        TheWarTargetConfigObject cfg;
        for (int i = 0; i < getPlayerData().getWarMissionCount(); i++) {
            WarSeasonMission.Builder targetBuilder = getPlayerData().getWarMissionBuilder(i);
            cfg = TheWarTargetConfig.getById(targetBuilder.getWarTaskBuilder().getCfgId());
            if (cfg == null) {
                continue;
            }
            if (targetBuilder.getWarTaskBuilder().getFinished()) {
                continue;
            }
            if (cfg.getMissiontype() != targetType.getNumber() || (cfg.getAddtion() > 0 && cfg.getAddtion() > targetParam)) { // 暂时只需大于等于
                continue;
            }
            if (isTotalAmountType(targetType)) {
                targetBuilder.getWarTaskBuilder().setProgress(Integer.min(cfg.getTargetcount(), addCount));
            } else {
                targetBuilder.getWarTaskBuilder().setProgress(Integer.min(cfg.getTargetcount(), targetBuilder.getWarTaskBuilder().getProgress() + addCount));
            }
            if (targetBuilder.getWarTaskBuilder().getProgress() >= cfg.getTargetcount()) {
                targetBuilder.getWarTaskBuilder().setFinished(true);
                targetBuilder.setStatus(MissionStatusEnum.MSE_Finished);
            }
            if (i == getPlayerData().getCurMissionIndex()) {
                needUpdate = true;
            }
        }
        if (needUpdate) {
            updateCurSeasonMission();
        }
    }

    /**
     * 判断任务类型是否是全量类型
     *
     * @return
     */
    private boolean isTotalAmountType(TargetTypeEnum typeEnum) {
        if (typeEnum == TargetTypeEnum.TTE_TheWar_JobTileLvReach) {
            return true;
        }
        return false;
    }

    public WarSeasonMission getCurMission() {
        int curMissionIndex = getPlayerData().getCurMissionIndex();
        if (curMissionIndex < 0 || curMissionIndex >= getPlayerData().getWarMissionCount()) {
            LogUtil.error("player[{}] get curMission is null, curIndex={}", getIdx(), curMissionIndex);
            return null;
        }
        return getPlayerData().getWarMission(getPlayerData().getCurMissionIndex());
    }

    public void updateCurSeasonMission() {
        sendTransMsgToServer(MsgIdEnum.SC_UpdateTheWarMission_VALUE, SC_UpdateTheWarMission.newBuilder().setCurMission(getCurMission()));
    }

    public TheWarRetCode claimWarSeasonMission() {
        int curMissionIndex = getPlayerData().getCurMissionIndex();
        if (curMissionIndex < 0 || curMissionIndex >= getPlayerData().getWarMissionCount()) {
            return TheWarRetCode.TWRC_ErrorParam;
        }
        WarSeasonMission.Builder targetData = getPlayerData().getWarMissionBuilder(getPlayerData().getCurMissionIndex());
        if (targetData == null) {
            return TheWarRetCode.TWRC_MissionNotFound;
        }
        if (targetData.getStatus() == MissionStatusEnum.MSE_FinishedAndClaim) {
            return TheWarRetCode.TWRC_MissionClaimed;
        }
        if (!targetData.getWarTask().getFinished() || targetData.getStatus() != MissionStatusEnum.MSE_Finished) {
            return TheWarRetCode.TWRC_MissionNotFinished;
        }
        TheWarTargetConfigObject targetCfg = TheWarTargetConfig.getById(targetData.getWarTask().getCfgId());
        if (targetCfg == null) {
            return TheWarRetCode.TWRC_ConfigNotFound;
        }
        gainWarReward(targetCfg.getFinishwarreward(), true, "远征赛季任务");
        CS_GS_UpdateWarReward.Builder builder = CS_GS_UpdateWarReward.newBuilder();
        builder.setPlayerIdx(getIdx());
        builder.addAllWarRewards(targetCfg.getFinishwarreward());
        builder.addAllRewards(targetCfg.getFinishnormalreward());
        sendMsgToServer(MsgIdEnum.CS_GS_UpdateWarReward_VALUE, builder);

        targetData.setStatus(MissionStatusEnum.MSE_FinishedAndClaim);

        if (curMissionIndex + 1 < getPlayerData().getWarMissionCount()) {
            getPlayerData().setCurMissionIndex(curMissionIndex + 1);
            updateCurSeasonMission();
        }
        return TheWarRetCode.TWRC_Success;
    }

    public WarJobTileTask.Builder builderJobTileTaskData() {
        WarJobTileTask.Builder builder = WarJobTileTask.newBuilder();
        builder.setTaskState(getPlayerData().getJobTileTaskData().getTaskState());
        builder.addAllTargetGroup(getPlayerData().getJobTileTaskData().getTargetGroupMap().values());
        return builder;
    }
    // ---------------------------------JobTile end------------------------------

    protected void updatePlayerStamina(WarRoom room, long curTime) {
        if (room == null) {
            return;
        }
        TheWarMapConfigObject mapConfig = TheWarMapConfig.getByMapname(room.getMapName());
        if (mapConfig == null) {
            return;
        }
        int stamina = getPlayerData().getStamina();
        if (stamina >= mapConfig.getMaxrecoverstamina()) {
            return;
        }
        if (getPlayerData().getUpdateStaminaTime() > curTime) {
            return;
        }
        int newStamina = Math.min(mapConfig.getMaxenergy(), stamina + mapConfig.getRecoverstamina());
        getPlayerData().setStamina(newStamina);
        getPlayerData().setUpdateStaminaTime(curTime + mapConfig.getRecoverstaminainterval() * TimeUtil.MS_IN_A_MIN);
        sendPlayerStamina();
    }

    public int addPlayerStamina(int addStamina) {
        WarRoom room = WarRoomCache.getInstance().queryObject(getRoomIdx());
        if (room == null) {
            return 0;
        }
        TheWarMapConfigObject mapConfig = TheWarMapConfig.getByMapname(room.getMapName());
        if (mapConfig == null) {
            return 0;
        }
        int stamina = getPlayerData().getStamina();
        if (stamina >= mapConfig.getMaxenergy()) {
            return 0;
        }
        int newValue = Math.min(mapConfig.getMaxenergy(), stamina + addStamina);
        getPlayerData().setStamina(newValue);
        sendPlayerStamina();
        return newValue - stamina;
    }

    public int decPlayerStamina(int deltaStamina) {
        WarRoom room = WarRoomCache.getInstance().queryObject(getRoomIdx());
        if (room == null) {
            return 0;
        }
        int stamina = getPlayerData().getStamina();
        getPlayerData().setStamina(stamina - deltaStamina);
        sendPlayerStamina();
        return deltaStamina;
    }

    public void sendPlayerStamina() {
        SC_UpdatePlayerStamia.Builder builder = SC_UpdatePlayerStamia.newBuilder();
        builder.setNewValue(getPlayerData().getStamina());
        sendTransMsgToServer(MsgIdEnum.SC_UpdatePlayerStamia_VALUE, builder);
    }

    protected void updatePetHp(long curTime) {
        if (getPlayerData().getUpdatePetHpTime() > curTime) {
            return;
        }
        if (getPlayerData().getBattleData().getEnterFightTime() > 0) {
            return;
        }
        TheWarConstConfigObject cfg = TheWarConstConfig.getById(GameConst.ConfigId);
        if (cfg == null) {
            return;
        }
        SC_UpdatePetProp.Builder builder = null;
        WarPetPropInfo.Builder propBuilder = WarPetPropInfo.newBuilder();
        long curHp;
        WarPetData.Builder newWarPet;
        for (WarPetData warPet : getPlayerData().getPlayerPetsMap().values()) {
            for (int i = 0; i < warPet.getPropDict().getKeysCount(); i++) {
                if (warPet.getPropDict().getKeys(i) == PetProperty.Current_Health_VALUE) {
                    curHp = warPet.getPropDict().getValues(i);
                    if (curHp >= 1000) {
                        break;
                    }
                    if (builder == null) {
                        builder = SC_UpdatePetProp.newBuilder();
                    }
                    curHp = Math.min(1000, curHp + cfg.getPetrecoverrate());
                    newWarPet = warPet.toBuilder();
                    newWarPet.getPropDictBuilder().setValues(i, curHp);
                    getPlayerData().putPlayerPets(warPet.getPetId(), newWarPet.build());

                    propBuilder.setPetIdx(warPet.getPetId());
                    propBuilder.getNewPropDictBuilder().addKeys(PetProperty.Current_Health_VALUE).addValues(curHp);
                    builder.addPetProp(propBuilder.build());
                    break;
                }
            }
        }
        if (builder != null) {
            sendTransMsgToServer(MsgIdEnum.SC_UpdatePetProp_VALUE, builder);
        }
        getPlayerData().setUpdatePetHpTime(curTime + cfg.getPetrecoverinterval() * TimeUtil.MS_IN_A_MIN);
    }

    public void clearBattleState(boolean updateGrid) {
        if (updateGrid) {
            WarMapData mapData = WarMapManager.getInstance().getRoomMapData(getRoomIdx());
            if (mapData != null) {
                WarMapGrid battlingGrid = mapData.getMapGridByPos(getPlayerData().getBattleData().getBattlingTargetPos());
                if (battlingGrid != null && battlingGrid.getPropValue(TheWarCellPropertyEnum.TWCP_BattlingTarget_VALUE) == GameUtil.stringToLong(getIdx(), 0)) {
                    Event event = Event.valueOf(EventType.ET_TheWar_ChangeTargetGridProperty, this, battlingGrid);
                    event.pushParam(TheWarCellPropertyEnum.TWCP_BattlingTarget_VALUE, 0l);
                    EventManager.getInstance().dispatchEvent(event);
                    LogUtil.info("Player[" + getIdx() + "] clear grid battle state,posx=" + battlingGrid.getPos().getX() + ",posy=" + battlingGrid.getPos().getY());
                }
            }
        }

        getPlayerData().getBattleDataBuilder().clearBattlingTargetPos();
        getPlayerData().getBattleDataBuilder().setEnterFightTime(0);
    }

    public void updatePlayerWarCurrency() {
        SC_UpdateWarCurrency.Builder builder = SC_UpdateWarCurrency.newBuilder();
        builder.setWarGold(getPlayerData().getWarGold());
        builder.setDoorOpenResource(getPlayerData().getWarDP());
        sendTransMsgToServer(MsgIdEnum.SC_UpdateWarCurrency_VALUE, builder);
    }

    public void addWarGridRecord(long curTime, Position pos, String targetPlayerId, int targetCamp, boolean occupyGrid) {
        if (getPlayerData().getWarGridRecordsCount() >= 50) {
            getPlayerData().removeWarGridRecords(0);
        }
        WarGridRecordData.Builder builder = WarGridRecordData.newBuilder();
        builder.setPos(pos);
        builder.setTargetPlayerId(targetPlayerId);
        builder.setTargetCamp(targetCamp);
        builder.setOccupyGrid(occupyGrid);
        builder.setTimeStamp(curTime);
        getPlayerData().addWarGridRecords(builder);

        if (isOnline()) {
            SC_UpdateWarGridRecord.Builder updateBuilder = SC_UpdateWarGridRecord.newBuilder();
            updateBuilder.setRecordData(builder);
            sendTransMsgToServer(MsgIdEnum.SC_UpdateWarGridRecord_VALUE, updateBuilder);
        }
    }

    public boolean sendMsgToServer(int msgId, Builder<?> builder) {
        if (getServerIndex() <= 0) {
            return false;
        }
        return GlobalData.getInstance().sendMsgToServer(getServerIndex(), msgId, builder);
    }

    public boolean sendTransMsgToServer(int msgId, Builder<?> builder) {
        if (getServerIndex() <= 0) {
            return false;
        }
        CS_GS_TheWarTransInfo.Builder builder1 = CS_GS_TheWarTransInfo.newBuilder();
        builder1.addPlayerIds(getIdx());
        builder1.setMsgId(msgId);
        builder1.setMsgData(builder.build().toByteString());
        return GlobalData.getInstance().sendMsgToServer(getServerIndex(), MsgIdEnum.CS_GS_TheWarTransInfo_VALUE, builder1);
    }

    public void onTick() {
        WarRoom room = WarRoomCache.getInstance().queryObject(getRoomIdx());
        if (room == null) {
            return;
        }
        long curTime = GlobalTick.getInstance().getCurrentTime();
        // 更新体力
        updatePlayerStamina(room, curTime);
        // 更新宠物血量
        updatePetHp(curTime);

        long enterFightTime = getPlayerData().getBattleData().getEnterFightTime();
        if (enterFightTime > 0 && enterFightTime + 5 * TimeUtil.MS_IN_A_MIN < curTime) {
            clearBattleState(true);
        }

        // 处理待清除格子
        updateClearingGrids(curTime);
    }

    protected boolean isTargetTypeOnlyInTheWar(TargetTypeEnum typeEnum) {
        if (typeEnum == TargetTypeEnum.TTE_TheWar_KillMonsterCount) {
            return true;
        }
        return false;
    }

    protected void initWarSeasonMission() {
        TheWarSeasonConfigObject seasonCfg = TheWarManager.getInstance().getWarSeasonConfig();
        if (seasonCfg == null || seasonCfg.getMissions() == null) {
            return;
        }
        TheWarTargetConfigObject targetCfg;
        for (int i = 0; i < seasonCfg.getMissions().length; i++) {
            targetCfg = TheWarTargetConfig.getById(seasonCfg.getMissions()[i]);
            if (targetCfg == null) {
                continue;
            }
            WarSeasonMission.Builder builder = WarSeasonMission.newBuilder();
            builder.getWarTaskBuilder().setCfgId(targetCfg.getId());
            getPlayerData().addWarMission(builder);
        }
    }

    public void sendUpdateTargetProMsg(TargetTypeEnum typeEnum, int addPro, int param, String... extraParams) {
        if (isTargetTypeOnlyInTheWar(typeEnum)) {
            return;
        }
        CS_GS_UpdateTheWarTargetPro.Builder builder = CS_GS_UpdateTheWarTargetPro.newBuilder();
        builder.setPlayerIdx(getIdx());
        builder.setTargetType(typeEnum);
        builder.setAddPro(addPro);
        builder.setParam(param);
        if (extraParams != null) {
            for (int i = 0; i < extraParams.length; i++) {
                builder.addLogParam(extraParams[i]);
            }
        }
        GlobalData.getInstance().sendMsgToServer(getServerIndex(), MsgIdEnum.CS_GS_UpdateTheWarTargetPro_VALUE, builder);
    }
}

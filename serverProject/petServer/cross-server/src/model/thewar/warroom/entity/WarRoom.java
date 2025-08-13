package model.thewar.warroom.entity;

import cfg.TheWarConstConfig;
import cfg.TheWarConstConfigObject;
import cfg.TheWarMapConfig;
import cfg.TheWarMapConfigObject;
import cfg.TheWarMonsterRefreshConfig;
import cfg.TheWarMonsterRefreshConfigObject;
import cfg.TheWarSeasonConfigObject;
import com.google.protobuf.GeneratedMessageV3.Builder;
import common.GameConst;
import common.GameConst.EventType;
import common.GameConst.RedisKey;
import common.GlobalData;
import common.GlobalTick;
import common.load.ServerConfig;
import datatool.StringHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import model.obj.BaseObj;
import model.thewar.TheWarManager;
import model.thewar.WarConst.RoomState;
import model.thewar.warmap.WarMapData;
import model.thewar.warmap.WarMapManager;
import model.thewar.warmap.grid.BossGrid;
import model.thewar.warmap.grid.FootHoldGrid;
import model.thewar.warmap.grid.PortalGrid;
import model.thewar.warmap.grid.WarMapGrid;
import model.thewar.warplayer.dbCache.WarPlayerCache;
import model.thewar.warplayer.entity.WarPlayer;
import org.springframework.util.CollectionUtils;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_BroadcastMarquee;
import protocol.ServerTransfer.CS_GS_TheWarRoomSettleData;
import protocol.ServerTransfer.CS_GS_TheWarTransInfo;
import protocol.TheWar.EnumTheWarTips;
import protocol.TheWar.SC_TheWarBroadCast;
import protocol.TheWar.SC_UpdateNewMemberJoin;
import protocol.TheWar.TheWarPlayerBaseInfo;
import protocol.TheWar.TheWarTipsParam;
import protocol.TheWarDB.GridCacheData;
import protocol.TheWarDB.PlayerCacheData;
import protocol.TheWarDB.RoomCacheData;
import protocol.TheWarDB.WarCampAkfInfo;
import protocol.TheWarDB.WarCampCanReachPos;
import protocol.TheWarDB.WarCampInfo;
import protocol.TheWarDB.WarMonsterGridTimeData;
import protocol.TheWarDB.WarMonsterRefreshData;
import protocol.TheWarDefine.BossAFKEfficacy;
import protocol.TheWarDefine.BossCellEfficacyMap;
import protocol.TheWarDefine.Position;
import protocol.TheWarDefine.SC_UpdateCampInfo;
import protocol.TheWarDefine.TheWarCampInfo;
import protocol.TheWarDefine.TheWarCellPropertyEnum;
import server.event.Event;
import server.event.EventManager;
import util.GameUtil;
import static util.JedisUtil.jedis;
import util.LogUtil;
import util.TimeUtil;

public class WarRoom extends BaseObj {
    private String idx;
    private String mapName;

    private int seasonId;
    private long createTime;
    private long updateStateTime;
    private long updateCacheTime;
    private int roomState;
    private boolean isPvpWar;

    private boolean roomController;
    private boolean initFinish;

    private boolean preSettleFlag;

    private long endPlayTimestamp;

    private int roomLevel;

    private Set<String> memberSet;
    private Map<Integer, WarCampInfo.Builder> totalCampInfo; // <WarCamp, CampInfo>
    private Map<Integer, Integer> fromSvrCountInfo;

    private Map<Integer, WarMonsterRefreshData> monsterRefreshTimeInfo; // <cfgId, freshTime>
    private Map<Position, Long> monsterGridExpireInfo; // <pos, expireTime>

    private RoomCacheData.Builder roomCacheBuilder;
    private Map<String, PlayerCacheData> playerCacheData;
    private Map<Position, GridCacheData> gridCacheData;

    public WarRoom() {
        this.totalCampInfo = new HashMap<>();
        this.fromSvrCountInfo = new HashMap<>();
        this.roomCacheBuilder = RoomCacheData.newBuilder();
        this.playerCacheData = new HashMap<>();
        this.gridCacheData = new HashMap<>();
        this.monsterRefreshTimeInfo = new HashMap<>();
        this.monsterGridExpireInfo = new HashMap<>();
        this.memberSet = new HashSet<>();
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
        return this.getClass().getSimpleName();
    }

    @Override
    public void putToCache() {
        transformDBData();
    }

    @Override
    public void transformDBData() {
//        roomCacheBuilder.setRoomState(getRoomState());
//        roomCacheBuilder.setInitFinish(initFinish);
//        roomCacheBuilder.setCreateTime(createTime);
//        roomCacheBuilder.setUpdateTime(updateStateTime);
//        roomCacheBuilder.setStartPlayTimeStamp(startPlayTimeStamp);
//
//        totalCampInfo.forEach((camp, campInfo) -> roomCacheBuilder.putTotalCampInfo(camp, campInfo.build()));
//        roomCacheBuilder.putAllFromSvrCountInfo(fromSvrCountInfo);
//        roomCacheBuilder.putAllMonsterFreshTime(monsterRefreshTimeInfo);
    }

    @Override
    public String getBaseIdx() {
        return idx;
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public int getRoomState() {
        return roomState;
    }

    public void setRoomState(int roomState) {
        this.roomState = roomState;
    }

    public boolean isRoomController() {
        return roomController;
    }

    public void setRoomController(boolean roomController) {
        this.roomController = roomController;
    }

    public boolean isInitFinish() {
        return initFinish;
    }

    public void setInitFinish(boolean initFinish) {
        this.initFinish = initFinish;
    }


    public int getSeasonId() {
        return seasonId;
    }

    public void setSeasonId(int seasonId) {
        this.seasonId = seasonId;
    }

    public long getEndPlayTimestamp() {
        return endPlayTimestamp;
    }

    public void setEndPlayTimestamp(long endPlayTimestamp) {
        this.endPlayTimestamp = endPlayTimestamp;
    }

    public boolean isPreSettleFlag() {
        return preSettleFlag;
    }

    public void setPreSettleFlag(boolean preSettleFlag) {
        this.preSettleFlag = preSettleFlag;
    }

    public int getRoomLevel() {
        return roomLevel;
    }

    public void setRoomLevel(int roomLevel) {
        this.roomLevel = roomLevel;
    }

    public void init(TheWarSeasonConfigObject warSeasonCfg) {
        if (warSeasonCfg != null) {
            setSeasonId(warSeasonCfg.getId());
            setMapName(warSeasonCfg.getOpenmapname());
            endPlayTimestamp = warSeasonCfg.getEndplaytime();
        }
        setRoomState(RoomState.FightingState);
        createTime = GlobalTick.getInstance().getCurrentTime();
        updateStateTime = createTime;

    }

    public void clear() {
        totalCampInfo.clear();
        fromSvrCountInfo.clear();
        monsterRefreshTimeInfo.clear();
        monsterGridExpireInfo.clear();
        roomCacheBuilder.clear();
        playerCacheData.clear();
        gridCacheData.clear();
        memberSet.clear();

        mapName = null;
        seasonId = 0;
        createTime = 0;
        updateStateTime = 0;
        updateCacheTime = 0;
        roomState = RoomState.ClosedState;
        isPvpWar = false;
        initFinish = false;
        preSettleFlag = false;
        endPlayTimestamp = 0;
    }

    public boolean containPlayer(String playerIdx) {
        return getAllMembers().contains(playerIdx);
    }

    public void addNewPlayer(WarPlayer player) {
        if (player == null || player.getCamp() <= 0) {
            return;
        }
        WarCampInfo.Builder campBuilder = totalCampInfo.get(player.getCamp());
        if (campBuilder == null) {
            campBuilder = WarCampInfo.newBuilder();
            campBuilder.setCamp(player.getCamp());
            totalCampInfo.put(player.getCamp(), campBuilder);
        }
        campBuilder.addMembers(player.getIdx());
        if (getAllMembers().size() > 0) {
            SC_UpdateNewMemberJoin.Builder builder = SC_UpdateNewMemberJoin.newBuilder();
            TheWarPlayerBaseInfo.Builder playerInfo = TheWarPlayerBaseInfo.newBuilder();
            playerInfo.setCamp(player.getCamp());
            playerInfo.setPlayerInfo(player.buildPlayerBaseInfo());
            playerInfo.setSpawnCellPos(player.getPlayerData().getBornPos());
            playerInfo.setJobTileLevel(player.getJobTileLevel());
            builder.addNewPlayers(playerInfo);
            broadcastMsg(MsgIdEnum.SC_UpdateNewMemberJoin_VALUE, builder, true);
        }
        addPlayerFromServer(player);
        memberSet.add(player.getIdx());
        addPlayerCache(player.getIdx(), player.buildPlayerCache());

        SC_UpdateCampInfo.Builder builder = SC_UpdateCampInfo.newBuilder();
        builder.getCampInfoBuilder().setIndex(player.getCamp());
        broadcastMsg(MsgIdEnum.SC_UpdateCampInfo_VALUE, builder, true);
    }

    protected void addPlayerFromServer(WarPlayer player) {
        fromSvrCountInfo.merge(player.getServerIndex(), 1, (oldVal, newVal) -> oldVal + newVal);
    }

    protected void removePlayerFromServer(WarPlayer player) {
        fromSvrCountInfo.computeIfPresent(player.getServerIndex(), (key, oldVal) -> oldVal <= 1 ? null : --oldVal);
    }

    public void broadcastMsg(int msgId, Builder<?> builder, boolean checkOnline) {
        if (totalCampInfo.isEmpty() || fromSvrCountInfo.isEmpty()) {
            return;
        }
        CS_GS_TheWarTransInfo.Builder builder1 = CS_GS_TheWarTransInfo.newBuilder();
        builder1.setMsgId(msgId);
        builder1.setMsgData(builder.build().toByteString());
        for (String playerIdx : memberSet) {
            if (checkOnline) {
                WarPlayer player = WarPlayerCache.getInstance().queryObject(playerIdx);
                if (player == null || !player.isOnline()) {
                    continue;
                }
            }
            builder1.addPlayerIds(playerIdx);
        }
        for (Map.Entry<Integer, Integer> entry : fromSvrCountInfo.entrySet()) {
            GlobalData.getInstance().sendMsgToServer(entry.getKey(), MsgIdEnum.CS_GS_TheWarTransInfo_VALUE, builder1);
        }
    }

    public void broadcastTips(int tipsType, boolean checkOnline, Object... params) {
        try {
            SC_TheWarBroadCast.Builder builder = SC_TheWarBroadCast.newBuilder();
            builder.setTipsTypeValue(tipsType);
            switch (tipsType) {
                case EnumTheWarTips.EMTW_AttackMonsterGrid_VALUE:
                case EnumTheWarTips.EMTW_OccupyMonsterGrid_VALUE:
                case EnumTheWarTips.EMTW_PromoteTech_VALUE: {
                    TheWarTipsParam.Builder tipsParam1 = TheWarTipsParam.newBuilder();
                    tipsParam1.setParamType(1); // string
                    tipsParam1.setParamVal(String.valueOf(params[0]));
                    builder.addTipsParams(tipsParam1);

                    TheWarTipsParam.Builder tipsParam2 = TheWarTipsParam.newBuilder();
                    tipsParam2.setParamType(0); // int32
                    tipsParam2.setParamVal(String.valueOf(params[1]));
                    builder.addTipsParams(tipsParam2);

                    TheWarTipsParam.Builder tipsParam3 = TheWarTipsParam.newBuilder();
                    tipsParam3.setParamType(0); // int32
                    tipsParam3.setParamVal(String.valueOf(params[2]));
                    builder.addTipsParams(tipsParam3);
                    break;
                }
                case EnumTheWarTips.EMTW_AttackPlayerGrid_VALUE:
                case EnumTheWarTips.EMTW_OccupyPlayerGrid_VALUE: {
                    TheWarTipsParam.Builder tipsParam1 = TheWarTipsParam.newBuilder();
                    tipsParam1.setParamType(1); // string
                    tipsParam1.setParamVal(String.valueOf(params[0]));
                    builder.addTipsParams(tipsParam1);

                    TheWarTipsParam.Builder tipsParam2 = TheWarTipsParam.newBuilder();
                    tipsParam2.setParamType(1); // string
                    tipsParam2.setParamVal(String.valueOf(params[1]));
                    builder.addTipsParams(tipsParam2);

                    TheWarTipsParam.Builder tipsParam3 = TheWarTipsParam.newBuilder();
                    tipsParam3.setParamType(0); // int32
                    tipsParam3.setParamVal(String.valueOf(params[2]));
                    builder.addTipsParams(tipsParam3);

                    TheWarTipsParam.Builder tipsParam4 = TheWarTipsParam.newBuilder();
                    tipsParam4.setParamType(0); // int32
                    tipsParam4.setParamVal(String.valueOf(params[3]));
                    builder.addTipsParams(tipsParam4);
                    break;
                }
                case EnumTheWarTips.EMTW_PromoteJobTile_VALUE: {
                    TheWarTipsParam.Builder tipsParam1 = TheWarTipsParam.newBuilder();
                    tipsParam1.setParamType(1); // string
                    tipsParam1.setParamVal(String.valueOf(params[0]));
                    builder.addTipsParams(tipsParam1);

                    TheWarTipsParam.Builder tipsParam2 = TheWarTipsParam.newBuilder();
                    tipsParam2.setParamType(0); // int32
                    tipsParam2.setParamVal(String.valueOf(params[1]));
                    builder.addTipsParams(tipsParam2);
                    break;
                }
                default:
                    break;
            }
            broadcastMsg(MsgIdEnum.SC_TheWarBroadCast_VALUE, builder, checkOnline);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    public void broadcastMarquee(int marqueeId, Object... params) {
        try {
            CS_GS_BroadcastMarquee.Builder builder = CS_GS_BroadcastMarquee.newBuilder();
            builder.setMarqueeId(marqueeId);
            builder.addAllPlayerIdList(getAllMembers());
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    builder.addParams(String.valueOf(params[i]));
                }
            }
            for (Map.Entry<Integer, Integer> entry : fromSvrCountInfo.entrySet()) {
                GlobalData.getInstance().sendMsgToServer(entry.getKey(), MsgIdEnum.CS_GS_BroadcastMarquee_VALUE, builder);
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    public boolean isRoomFull() {
        TheWarMapConfigObject cfg = TheWarMapConfig.getByMapname(getMapName());
        if (cfg == null) {
            return true;
        }
        return getAllMembers().size() >= cfg.getMaxplayercount();
    }

    public TheWarCampInfo.Builder getCampInfo(int camp) {
        WarCampInfo.Builder campInfo = totalCampInfo.get(camp);
        if (campInfo == null) {
            return null;
        }
        TheWarCampInfo.Builder builder = TheWarCampInfo.newBuilder();
        builder.setIndex(campInfo.getCamp());
        builder.setTheWarScore(campInfo.getTheWarScore());
        BossCellEfficacyMap.Builder bossTotalInfoBuilder = BossCellEfficacyMap.newBuilder();
        BossAFKEfficacy.Builder bossAfkInfoBuilder = BossAFKEfficacy.newBuilder();
        for (WarCampAkfInfo bossEfficacy : campInfo.getBossEfficacyPlusList()) {
            bossTotalInfoBuilder.addCellPos(bossEfficacy.getBossPos());
            bossAfkInfoBuilder.setGoldEfficacy(bossEfficacy.getCampBossGoldEfficacy());
            bossAfkInfoBuilder.setDpEfficacy(bossEfficacy.getCampBossDpEfficacy());
            bossAfkInfoBuilder.setHolyWaterEfficacy(bossEfficacy.getCampBossHolyWaterEfficacy());
            bossTotalInfoBuilder.addEfficacy(bossAfkInfoBuilder.build());
        }
        builder.setBossEfficacymap(bossTotalInfoBuilder);
        return builder;
    }

    public List<TheWarCampInfo> getAllCampInfo() {
        List<TheWarCampInfo> campInfos = new ArrayList<>();
        for (Integer camp : totalCampInfo.keySet()) {
            TheWarCampInfo.Builder builder = getCampInfo(camp);
            if (builder != null) {
                campInfos.add(builder.build());
            }
        }
        return campInfos;
    }

    public List<TheWarPlayerBaseInfo> buildPlayerListInfo() {
        List<TheWarPlayerBaseInfo> playerInfoList = new ArrayList<>();
        WarPlayer warPlayer;
        for (Entry<Integer, WarCampInfo.Builder> entry : totalCampInfo.entrySet()) {
            for (String playerIdx : entry.getValue().getMembersList()) {
                warPlayer = WarPlayerCache.getInstance().queryObject(playerIdx);
                if (warPlayer == null) {
                    continue;
                }
                TheWarPlayerBaseInfo.Builder playerInfo = TheWarPlayerBaseInfo.newBuilder();
                playerInfo.setCamp(entry.getKey());
                playerInfo.setPlayerInfo(warPlayer.buildPlayerBaseInfo());
                playerInfo.setSpawnCellPos(warPlayer.getPlayerData().getBornPos());
                playerInfo.setJobTileLevel(warPlayer.getJobTileLevel());
                playerInfoList.add(playerInfo.build());
            }
        }
        return playerInfoList;
    }

    public int getMergeCampPosGroup(WarMapData mapData, WarCampInfo.Builder campPosMap, int camp, Position pos) {
        if (mapData == null || camp == 0 || pos == null) {
            return 0;
        }
        WarMapGrid grid = mapData.getMapGridByPos(pos);
        if (grid == null) {
            return 0;
        }
        WarMapGrid aroundGrid;
        for (Position aroundPos : grid.getAroundGrids()) {
            aroundGrid = mapData.getMapGridByPos(aroundPos);
            if (aroundGrid == null || aroundGrid.getPropValue(TheWarCellPropertyEnum.TWCP_Camp_VALUE) != camp) {
                continue;
            }
            Optional<Entry<Integer, WarCampCanReachPos>> findOpt = campPosMap.getCampCanReachPosMap().entrySet().stream().filter(entry -> entry.getValue().getGroupPosList().contains(aroundPos)).findFirst();
            if (!findOpt.isPresent()) {
                continue;
            }
            return findOpt.get().getKey();
        }
        return 0;
    }

    public void calcAddCampPos(WarPlayer warPlayer, Position newPos) {
        WarMapData mapData = WarMapManager.getInstance().getRoomMapData(getIdx());
        if (mapData == null) {
            LogUtil.error("room[" + getIdx() + "] AddCampPos error,mapData is null");
            return;
        }
        WarMapGrid grid = mapData.getMapGridByPos(newPos);
        if (grid == null || grid.getPropValue(TheWarCellPropertyEnum.TWCP_OccupierPlayerId_VALUE) != GameUtil.stringToLong(warPlayer.getIdx(), 0)) {
            LogUtil.error("room[" + getIdx() + "] AddCampPos error,grid not owned");
            return;
        }
        int camp = warPlayer.getCamp();
        WarCampInfo.Builder campInfo = totalCampInfo.get(camp);
        if (campInfo == null) {
            campInfo = WarCampInfo.newBuilder();
            campInfo.setCamp(camp);
            totalCampInfo.put(camp, campInfo);
        }
        if (campInfo.getCampGroupIndex() == 0) {
            campInfo.setCampGroupIndex(1);
        }

        if (grid instanceof FootHoldGrid) {
            if (grid instanceof BossGrid) {
                addCampEfficacy(camp, (BossGrid) grid);
            }
            int score = (int) (campInfo.getTheWarScore() + grid.getPropValue(TheWarCellPropertyEnum.TWCP_OccCellWarScore_VALUE));
            campInfo.setTheWarScore(score);
            updateCampAfkEfficacy(camp);
        }

        // 先算出新节点所属组序号
        int mergeGroup = getMergeCampPosGroup(mapData, campInfo, camp, newPos);
        if (mergeGroup == 0) {
            WarCampCanReachPos.Builder newGroupBuilder = WarCampCanReachPos.newBuilder().putGroupPlayers(warPlayer.getIdx(), 1).addGroupPos(newPos);
            campInfo.putCampCanReachPos(campInfo.getCampGroupIndex(), newGroupBuilder.build());
            campInfo.setCampGroupIndex(campInfo.getCampGroupIndex() + 1);
            LogUtil.info("WarRoom[" + getIdx() + "] calcAddCampPos addNewCampGroup:" + newGroupBuilder);
            return;
        }

        HashSet<Position> checkedPosSet = new HashSet<>();
        Map<Integer, WarCampCanReachPos.Builder> campGroupMap = new HashMap<>();
        campInfo.getCampCanReachPosMap().forEach((group, warCampCanReachPos) -> campGroupMap.put(group, warCampCanReachPos.toBuilder()));
        // 合并所有所有附加节点到mergeGroup,递归调用
        mapData.calcAddCampPos(grid, campGroupMap, camp, mergeGroup, checkedPosSet, 0);

        for (Entry<Integer, WarCampCanReachPos.Builder> entry : campGroupMap.entrySet()) {
            campInfo.putCampCanReachPos(entry.getKey(), entry.getValue().build());
        }

    }

    public void calcRemoveCampPos(WarPlayer warPlayer, Position removePos) {
        WarMapData mapData = WarMapManager.getInstance().getRoomMapData(getIdx());
        if (mapData == null) {
            return;
        }

        WarMapGrid grid = mapData.getMapGridByPos(removePos);
        if (grid == null) {
            return;
        }

        long playerId = GameUtil.stringToLong(warPlayer.getIdx(), 0);
        if (playerId == 0) {
            return;
        }
        WarCampInfo.Builder campInfo = totalCampInfo.get(warPlayer.getCamp());
        if (campInfo == null) {
            return;
        }
        if (grid instanceof BossGrid) {
            removeCampEfficacy(warPlayer.getCamp(), removePos);
            updateCampAfkEfficacy(warPlayer.getCamp());
        }

        Optional<Entry<Integer, WarCampCanReachPos>> findOpt = campInfo.getCampCanReachPosMap().entrySet().stream().filter(entry -> entry.getValue().getGroupPosList().contains(removePos)).findFirst();
        if (!findOpt.isPresent()) {
            return;
        }
        int removeGroup = findOpt.get().getKey();
        if (removeGroup == 0) {
            return;
        }

        WarMapGrid aroundGrid;
        WarCampCanReachPos campGroupInfo = campInfo.getCampCanReachPosMap().get(removeGroup);
        WarCampCanReachPos.Builder campGroupInfoBuilder = campGroupInfo.toBuilder();
        HashSet<Position> checkedPosSet = new HashSet<>();
        Map<Integer, WarCampCanReachPos.Builder> campGroupMap = new HashMap<>();
        for (int i = 0; i < campGroupInfoBuilder.getGroupPosCount(); i++) {
            Position pos = campGroupInfoBuilder.getGroupPos(i);
            if (pos.equals(removePos)) {
                campGroupInfoBuilder.removeGroupPos(i);
                Integer count = campGroupInfoBuilder.getGroupPlayersMap().get(warPlayer.getIdx());
                if (count != null && count > 1) {
                    campGroupInfoBuilder.putGroupPlayers(warPlayer.getIdx(), --count);
                } else {
                    campGroupInfoBuilder.removeGroupPlayers(warPlayer.getIdx());
                }
                break;
            }
        }

        for (Position groupPos : campGroupInfoBuilder.getGroupPosList()) {
            if (checkedPosSet.contains(groupPos)) {
                continue;
            }
            aroundGrid = mapData.getMapGridByPos(groupPos);
            if (aroundGrid == null) {
                continue;
            }
            int campGroupIndex = campInfo.getCampGroupIndex();
            mapData.calcRemoveCampPos(aroundGrid, campGroupMap, warPlayer.getCamp(), campGroupIndex, checkedPosSet, 0);
            campInfo.setCampGroupIndex(++campGroupIndex);
        }
        for (Entry<Integer, WarCampCanReachPos.Builder> entry : campGroupMap.entrySet()) {
            campInfo.putCampCanReachPos(entry.getKey(), entry.getValue().build());
        }
        campInfo.removeCampCanReachPos(removeGroup);
        LogUtil.debug("calcRemoveCampPos " + removeGroup + " after removeGird GroupMap:" + campInfo);
    }


    public boolean checkOwnedAroundPos(int camp, Position pos) {
        WarMapData mapData = WarMapManager.getInstance().getRoomMapData(getIdx());
        if (mapData == null) {
            return false;
        }
        WarMapGrid grid = mapData.getMapGridByPos(pos);
        if (grid == null) {
            return false;
        }
        if (!(grid instanceof PortalGrid)) { // 先检查传送门格子是否激活和相邻
            boolean flag = grid.getPropValue(TheWarCellPropertyEnum.TWCP_ValidPortalSourcePos_VALUE) > 0;
            if (flag) {
                long portalPosPos = grid.getPropValue(TheWarCellPropertyEnum.TWCP_PortalSourcePos_VALUE);
                Position.Builder builder = Position.newBuilder().setX((int) (portalPosPos >>> 32)).setY((int) portalPosPos);
                WarMapGrid portalGrid = mapData.getMapGridByPos(builder.build());
                if (portalGrid instanceof PortalGrid
                        && portalGrid.getPropValue(TheWarCellPropertyEnum.TWCP_PortalEnable_VALUE) > 0
                        && checkOwnedAroundPos(camp, portalGrid.getPos())) {
                    return true;
                }
            }
        }
        WarMapGrid aroundGrid;
        for (Position aroundPos : grid.getAroundGrids()) {
            aroundGrid = mapData.getMapGridByPos(aroundPos);
            if (aroundGrid != null && getCampPosInGroup(camp, aroundPos) > 0) {
                return true;
            }
        }
        return false;
    }

    public int getCampPosInGroup(int camp, Position pos) {
        WarCampInfo.Builder campData = totalCampInfo.get(camp);
        if (campData == null) {
            return 0;
        }
        for (Entry<Integer, WarCampCanReachPos> entry : campData.getCampCanReachPosMap().entrySet()) {
            if (entry.getValue().getGroupPosList().contains(pos)) {
                return entry.getKey();
            }
        }
        return 0;
    }

    public void settleRank() {
        List<WarCampInfo.Builder> list = totalCampInfo.entrySet().stream().sorted(
                ((o1, o2) -> o2.getValue().getTheWarScore() - o1.getValue().getTheWarScore())).map(entry -> entry.getValue()).collect(Collectors.toList());
        WarCampInfo.Builder campInfo;
        CS_GS_TheWarRoomSettleData.Builder builder = CS_GS_TheWarRoomSettleData.newBuilder();
        for (int i = 0; i < list.size(); i++) {
            campInfo = list.get(i);
            builder.clear();
            builder.setRank(i + 1);
            builder.addAllPlayerIdx(campInfo.getMembersList());
            for (Map.Entry<Integer, Integer> entry : fromSvrCountInfo.entrySet()) {
                GlobalData.getInstance().sendMsgToServer(entry.getKey(), MsgIdEnum.CS_GS_TheWarRoomSettleData_VALUE, builder);
            }
        }
    }

    public void settleTheWar() {
        settleRank();

        Event event = Event.valueOf(EventType.ET_TheWar_RoomSettle, GameUtil.getDefaultEventSource(), GameUtil.getDefaultEventSource());
        event.pushParam(getIdx(), getAllMembers().stream().collect(Collectors.toSet()));
        EventManager.getInstance().dispatchEvent(event);
        clear();

        try {
            String serverIndex = StringHelper.IntTostring(ServerConfig.getInstance().getServer(), "");
            String roomSvrIndex = jedis.hget(RedisKey.TheWarRoomServerIndex, getIdx());
            if (!StringHelper.isNull(roomSvrIndex) && !serverIndex.equals(roomSvrIndex)) {
                return;
            }
            jedis.hdel(RedisKey.TheWarRoomServerIndex, getIdx());
            Long result = jedis.hdel(RedisKey.TheWarRoomData.getBytes(), getIdx().getBytes());
            if (result == null) {
                LogUtil.error("WarRoom[" + getIdx() + "] remove redis key TheWarRoomData error, result == null,idxBytes=" + getIdx().getBytes());
            } else {
                LogUtil.info("WarRoom[" + getIdx() + "] remove redis key TheWarRoomData error, result=" + result + ",idxBytes=" + getIdx().getBytes());
            }
            jedis.del((RedisKey.TheWarPlayerData + getIdx()).getBytes());
            jedis.del((RedisKey.TheWarGridData + getIdx()).getBytes());
            jedis.hdel(RedisKey.TheWarAvailableJoinRoomInfo, getIdx());
            LogUtil.info("WarRoom[" + getIdx() + "] settle finished");
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    public boolean revertByRoomCache(RoomCacheData roomData) {
        setMapName(roomData.getMapName());
        setSeasonId(roomData.getSeasonId());
        setRoomState(roomData.getRoomState());
        updateStateTime = roomData.getUpdateTime();
        createTime = roomData.getCreateTime();
        isPvpWar = roomData.getIpPvpWar();
        preSettleFlag = roomData.getPreSettleFlag();
        roomLevel = roomData.getMaxGridLevel();


        for (Entry<Integer, WarCampInfo> entry : roomData.getTotalCampInfoMap().entrySet()) {
            totalCampInfo.put(entry.getKey(), entry.getValue().toBuilder());
            memberSet.addAll(entry.getValue().getMembersList());
        }

        // 兼容代码
        for (Entry<String, Integer> entry : roomData.getFromSvrCountInfoMap().entrySet()) {
            int svrIndex = GlobalData.getInstance().getServerIndexByIp(entry.getKey());
            fromSvrCountInfo.put(svrIndex, entry.getValue());
        }

        fromSvrCountInfo.putAll(roomData.getFromSvrIndexCountInfoMap());
        monsterRefreshTimeInfo.putAll(roomCacheBuilder.getMonsterFreshTimeMap());

        if (roomCacheBuilder.getMonsterGridTimeCount() > 0) {
            for (WarMonsterGridTimeData.Builder monsterGrid : roomCacheBuilder.getMonsterGridTimeBuilderList()) {
                monsterGridExpireInfo.put(monsterGrid.getPos(), monsterGrid.getExpireTime());
            }
        }

        initFinish = roomData.getInitFinish();
        roomController = true;

        // 赛季不同时按自己的结算时间结算，赛季相同时按最新的赛季结算时间结算
        TheWarSeasonConfigObject curSeason = TheWarManager.getInstance().getWarSeasonConfig();
        if (curSeason == null || curSeason.getId() != getSeasonId()) {
            setEndPlayTimestamp(roomData.getEndPlayTimestamp());
        } else {
            setEndPlayTimestamp(curSeason.getEndplaytime());
        }
        return true;
    }

    public RoomCacheData buildRoomCache() {
        roomCacheBuilder.clear();
        roomCacheBuilder.setMapName(getMapName());
        roomCacheBuilder.setSeasonId(getSeasonId());
        roomCacheBuilder.setCreateTime(createTime);
        roomCacheBuilder.setUpdateTime(updateStateTime);
        roomCacheBuilder.setRoomState(getRoomState());
        roomCacheBuilder.setIpPvpWar(isPvpWar);
        roomCacheBuilder.setInitFinish(initFinish);
        roomCacheBuilder.setPreSettleFlag(preSettleFlag);
        roomCacheBuilder.setMaxGridLevel(getRoomLevel());
        for (Entry<Integer, WarCampInfo.Builder> entry : totalCampInfo.entrySet()) {
            roomCacheBuilder.putTotalCampInfo(entry.getKey(), entry.getValue().build());
        }
        roomCacheBuilder.putAllFromSvrIndexCountInfo(fromSvrCountInfo);
        roomCacheBuilder.putAllMonsterFreshTime(monsterRefreshTimeInfo);

        if (!monsterGridExpireInfo.isEmpty()) {
            WarMonsterGridTimeData.Builder monsterGridExpireTime = WarMonsterGridTimeData.newBuilder();
            for (Entry<Position, Long> entry : monsterGridExpireInfo.entrySet()) {
                monsterGridExpireTime.setPos(entry.getKey());
                monsterGridExpireTime.setExpireTime(entry.getValue());
                roomCacheBuilder.addMonsterGridTime(monsterGridExpireTime.build());
            }
        }
        roomCacheBuilder.setEndPlayTimestamp(getEndPlayTimestamp());
        return roomCacheBuilder.build();
    }

    public void addPlayerCache(String playerIdx, PlayerCacheData playerCache) {
        playerCacheData.put(playerIdx, playerCache);
    }

    public void addGridCache(Position pos, GridCacheData gridCache) {
        gridCacheData.put(pos, gridCache);
    }

    public void updateRoomCache() {
        try {
            String roomSvrIndex = jedis.hget(RedisKey.TheWarRoomServerIndex, getIdx());
            if (StringHelper.isNull(roomSvrIndex)) {
                return;
            }
            String serverIndex = StringHelper.IntTostring(ServerConfig.getInstance().getServer(), "");
            if (!roomSvrIndex.equals(serverIndex)) {
                roomController = false;
                return;
            }
            jedis.hset((RedisKey.TheWarRoomData).getBytes(), getIdx().getBytes(), buildRoomCache().toByteArray());
            playerCacheData.forEach((playerIdx, playerCache) -> jedis.hset((RedisKey.TheWarPlayerData + getIdx()).getBytes(), playerIdx.getBytes(), playerCache.toByteArray()));
            gridCacheData.forEach((pos, gridCache) -> jedis.hset((RedisKey.TheWarGridData + getIdx()).getBytes(), pos.toByteArray(), gridCache.toByteArray()));

            // 房间不满时更新人数信息，用于新用户进入
            TheWarMapConfigObject cfg = TheWarMapConfig.getByMapname(getMapName());
            if (cfg != null) {
                int remainNum = Math.max(0, cfg.getMaxplayercount() - getAllMembers().size());
                jedis.hset(RedisKey.TheWarAvailableJoinRoomInfo, getIdx(), StringHelper.IntTostring(remainNum, "0"));
            }

            playerCacheData.clear();
            gridCacheData.clear();
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    public boolean needClear() {
        return roomState == RoomState.ClosedState || updateStateTime == 0 || (initFinish && !roomController);
    }

    public Set<String> getAllMembers() {
        return this.memberSet;
    }

    public void addRefreshedMonsterInfo(Map<Integer, List<Position>> monsterGrids) {
        if (CollectionUtils.isEmpty(monsterGrids)) {
            return;
        }
        long curTime = GlobalTick.getInstance().getCurrentTime();
        monsterGrids.forEach((cfgId, posList) -> {
            TheWarMonsterRefreshConfigObject cfg = TheWarMonsterRefreshConfig.getById(cfgId);
            if (cfg == null) {
                return;
            }
            WarMonsterRefreshData refreshInfo = monsterRefreshTimeInfo.get(cfgId);
            WarMonsterRefreshData.Builder builder;
            if (refreshInfo == null) {
                builder = WarMonsterRefreshData.newBuilder();
            } else {
                builder = refreshInfo.toBuilder();
            }
            builder.setFreshTime(curTime + cfg.getRefreshinterval() * TimeUtil.MS_IN_A_MIN);
            builder.addAllGridGroup(posList);
            monsterRefreshTimeInfo.put(cfgId, builder.build());
        });

    }

    public void decreaseRefreshedMonsterCount(int cfgId) {
        WarMonsterRefreshData refreshInfo = monsterRefreshTimeInfo.get(cfgId);
        if (refreshInfo != null) {
            WarMonsterRefreshData.Builder builder = refreshInfo.toBuilder();
            builder.setRefreshedCount(Integer.max(0, refreshInfo.getRefreshedCount() - 1)).build();
            monsterRefreshTimeInfo.put(cfgId, builder.build());
        }
    }

    public void refreshMonster(long curTime) {
        WarMapData mapData = WarMapManager.getInstance().getRoomMapData(getIdx());
        if (mapData != null) {
            monsterRefreshTimeInfo.forEach((cfgId, refreshData) -> {
                if (updateStateTime > refreshData.getFreshTime()) {
                    TheWarMonsterRefreshConfigObject cfg = TheWarMonsterRefreshConfig.getById(cfgId);
                    int refreshedCount = refreshData.getGridGroupCount();
                    if (cfg == null || refreshedCount >= cfg.getMaxrefreshnum()) {
                        return;
                    }
                    int i = 0;

                    WarMapGrid grid;
                    Collections.shuffle(refreshData.getGridGroupList());
                    List<WarMapGrid> refreshList = new ArrayList<>();
                    while (i < cfg.getRefreshnum() && refreshedCount < cfg.getMaxrefreshnum()) {
                        grid = mapData.getMapGridByPos(refreshData.getGridGroup(i));
                        if (grid == null || grid.getPropValue(TheWarCellPropertyEnum.TWCP_IsRefreshed_VALUE) > 0) {
                            continue;
                        }
                        if (grid.getPropValue(TheWarCellPropertyEnum.TWCP_OccupierPlayerId_VALUE) > 0) {
                            continue;
                        }
                        if (grid.getPropValue(TheWarCellPropertyEnum.TWCP_BattlingTarget_VALUE) > 0) {
                            continue;
                        }
                        refreshList.add(grid);
                        ++i;
                        ++refreshedCount;
                    }
                    Event event = Event.valueOf(EventType.ET_TheWar_AddRefreshMonsterGrid, this, GameUtil.getDefaultEventSource());
                    event.pushParam(cfgId, refreshList);
                    EventManager.getInstance().dispatchEvent(event);

                    setModified(true);
                    WarMonsterRefreshData.Builder builder = refreshData.toBuilder();
                    builder.setFreshTime(curTime + cfg.getRefreshinterval() * TimeUtil.MS_IN_A_MIN);
                    builder.setRefreshedCount(refreshedCount);
                    monsterRefreshTimeInfo.put(cfgId, builder.build());
                }
            });
        }
    }

    public void addMonsterExpireInfo(Position pos, long expireTime) {
        monsterGridExpireInfo.put(pos, expireTime);
    }

    public void updateMonsterExpireInfo(long curTime) {
        WarMapData mapData = WarMapManager.getInstance().getRoomMapData(getIdx());
        if (mapData == null) {
            return;
        }
        Iterator<Entry<Position, Long>> iter = monsterGridExpireInfo.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<Position, Long> entry = iter.next();
            if (entry.getValue() > curTime) {
                return;
            }
            WarMapGrid grid = mapData.getMapGridByPos(entry.getKey());
            if (grid == null) {
                return;
            }
            int cfgId = (int) grid.getPropValue(TheWarCellPropertyEnum.TWCP_MonsterRefreshCfgId_VALUE);
            Event event = Event.valueOf(EventType.ET_TheWar_RemoveRefreshMonsterGrid, this, grid);
            EventManager.getInstance().dispatchEvent(event);
            WarMonsterRefreshData timeInfo = monsterRefreshTimeInfo.get(cfgId);
            if (timeInfo != null) {
                WarMonsterRefreshData.Builder builder = timeInfo.toBuilder();
                builder.setRefreshedCount(timeInfo.getGridGroupCount() - 1);
                monsterRefreshTimeInfo.put(cfgId, builder.build());
            }
            iter.remove();
        }
    }

    public void updateBossGridHp(long curTime) {
        WarMapData mapData = WarMapManager.getInstance().getRoomMapData(getIdx());
        if (mapData == null) {
            return;
        }
        TheWarConstConfigObject config = TheWarConstConfig.getById(GameConst.ConfigId);
        if (config == null) {
            return;
        }
        List<BossGrid> updateGridList = null;
        Set<Position> bossGridList = mapData.getAllBossGrids();
        BossGrid bossGrid;
        WarMapGrid grid;
        long updateInterval = config.getPetrecoverinterval() * TimeUtil.MS_IN_A_MIN;
        for (Position pos : bossGridList) {
            grid = mapData.getMapGridByPos(pos);
            if (!(grid instanceof BossGrid)) {
                continue;
            }
            if (grid.getPropValue(TheWarCellPropertyEnum.TWCP_BattlingTarget_VALUE) > 0
                    || grid.getPropValue(TheWarCellPropertyEnum.TWCP_OccupierPlayerId_VALUE) > 0) {
                continue;
            }
            bossGrid = (BossGrid) grid;
            if (bossGrid.getLastRecoverHpTime() + updateInterval > curTime) {
                continue;
            }
            if (updateGridList == null) {
                updateGridList = new ArrayList<>();
            }
            updateGridList.add(bossGrid);
        }
        if (!CollectionUtils.isEmpty(updateGridList)) {
            Event event = Event.valueOf(EventType.ET_TheWar_UpdateBossPetHp, GameUtil.getDefaultEventSource(), GameUtil.getDefaultEventSource());
            event.pushParam(updateGridList);
            EventManager.getInstance().dispatchEvent(event);
        }
    }

    public void addCampEfficacy(int camp, BossGrid ftGrid) {
        if (ftGrid == null) {
            return;
        }
        WarCampInfo.Builder warCampBuilder = totalCampInfo.get(camp);
        if (warCampBuilder == null) {
            return;
        }

        WarCampAkfInfo.Builder afkBuilder = WarCampAkfInfo.newBuilder();
        afkBuilder.setBossPos(ftGrid.getPos());
        afkBuilder.setDefeatBossTime(GlobalTick.getInstance().getCurrentTime());
        afkBuilder.setCampBossGoldEfficacy(ftGrid.getPropValue(TheWarCellPropertyEnum.TWCP_CampProducEfficacyPlus_WarGold_VALUE));
        afkBuilder.setCampBossDpEfficacy(ftGrid.getPropValue(TheWarCellPropertyEnum.TWCP_CampProducEfficacyPlus_DP_VALUE));
        afkBuilder.setCampBossHolyWaterEfficacy(ftGrid.getPropValue(TheWarCellPropertyEnum.TWCP_CampProducEfficacyPlus_HolyWater_VALUE));
        warCampBuilder.addBossEfficacyPlus(afkBuilder);
    }

    public void removeCampEfficacy(int camp, Position pos) {
        WarCampInfo.Builder warCampBuilder = totalCampInfo.get(camp);
        if (warCampBuilder == null) {
            return;
        }
        for (int i = 0; i < warCampBuilder.getBossEfficacyPlusCount(); i++) {
            WarCampAkfInfo efficacy = warCampBuilder.getBossEfficacyPlus(i);
            if (efficacy.getBossPos().equals(pos)) {
                warCampBuilder.removeBossEfficacyPlus(i);
                return;
            }
        }
    }

    public void updateCampAfkEfficacy(int camp) {
        WarCampInfo.Builder campBuilder = totalCampInfo.get(camp);
        if (campBuilder == null) {
            return;
        }
        SC_UpdateCampInfo.Builder builder = SC_UpdateCampInfo.newBuilder();
        builder.getCampInfoBuilder().setIndex(camp);
        builder.getCampInfoBuilder().setTheWarScore(campBuilder.getTheWarScore());
        BossAFKEfficacy.Builder efficacyBuilder = BossAFKEfficacy.newBuilder();
        for (WarCampAkfInfo efficacyInfo : campBuilder.getBossEfficacyPlusList()) {
            builder.getCampInfoBuilder().getBossEfficacymapBuilder().addCellPos(efficacyInfo.getBossPos());
            efficacyBuilder.setGoldEfficacy(efficacyInfo.getCampBossGoldEfficacy());
            efficacyBuilder.setDpEfficacy(efficacyInfo.getCampBossDpEfficacy());
            efficacyBuilder.setHolyWaterEfficacy(efficacyInfo.getCampBossHolyWaterEfficacy());
            builder.getCampInfoBuilder().getBossEfficacymapBuilder().addEfficacy(efficacyBuilder.build());
        }
        broadcastMsg(MsgIdEnum.SC_UpdateCampInfo_VALUE, builder, true);
    }

    public WarCampAkfInfo getBossEfficacyPlus(int camp) {
        WarCampInfo.Builder builder = totalCampInfo.get(camp);
        if (builder == null) {
            return null;
        }
        WarCampAkfInfo.Builder efficacyBuilder = WarCampAkfInfo.newBuilder();
        for (WarCampAkfInfo bossEfficacy : builder.getBossEfficacyPlusList()) {
            efficacyBuilder.setCampBossGoldEfficacy(efficacyBuilder.getCampBossGoldEfficacy() + bossEfficacy.getCampBossGoldEfficacy());
            efficacyBuilder.setCampBossDpEfficacy(efficacyBuilder.getCampBossDpEfficacy() + bossEfficacy.getCampBossDpEfficacy());
            efficacyBuilder.setCampBossHolyWaterEfficacy(efficacyBuilder.getCampBossHolyWaterEfficacy() + bossEfficacy.getCampBossHolyWaterEfficacy());
        }
        return efficacyBuilder.build();
    }

    // boss格子金币产出结算，暂保留
    public long calcCampPlayerAfkGoldReward(int camp, long playerLastSettleTime) {
        WarCampInfo.Builder builder = totalCampInfo.get(camp);
        if (builder == null) {
            return 0;
        }
        long curTime = GlobalTick.getInstance().getCurrentTime();
        if (playerLastSettleTime > curTime) {
            return 0;
        }
        // 已扩大1000倍
        long warGold = 0;
        for (WarCampAkfInfo bossEfficacy : builder.getBossEfficacyPlusList()) {
            if (bossEfficacy.getDefeatBossTime() > curTime) {
                continue;
            }
            if (playerLastSettleTime == 0 || playerLastSettleTime < bossEfficacy.getDefeatBossTime()) {
                warGold += (curTime - bossEfficacy.getDefeatBossTime()) * bossEfficacy.getCampBossGoldEfficacy() / TimeUtil.MS_IN_A_MIN;
            } else {
                warGold += (curTime - playerLastSettleTime) * bossEfficacy.getCampBossGoldEfficacy() / TimeUtil.MS_IN_A_MIN;
            }
        }
        return warGold;
    }

    // boss格子dp产出结算，暂保留
    public long calcCampPlayerAfkDpReward(int camp, long playerLastSettleTime) {
        WarCampInfo.Builder builder = totalCampInfo.get(camp);
        if (builder == null) {
            return 0;
        }
        long curTime = GlobalTick.getInstance().getCurrentTime();
        if (playerLastSettleTime > curTime) {
            return 0;
        }
        // 已扩大1000倍
        long warDp = 0;
        for (WarCampAkfInfo bossEfficacy : builder.getBossEfficacyPlusList()) {
            if (bossEfficacy.getDefeatBossTime() > curTime) {
                continue;
            }
            if (playerLastSettleTime == 0 || playerLastSettleTime < bossEfficacy.getDefeatBossTime()) {
                warDp += (curTime - bossEfficacy.getDefeatBossTime()) * bossEfficacy.getCampBossDpEfficacy() / TimeUtil.MS_IN_A_MIN;
            } else {
                warDp += (curTime - playerLastSettleTime) * bossEfficacy.getCampBossDpEfficacy() / TimeUtil.MS_IN_A_MIN;
            }
        }
        return warDp;
    }

    // boss格子圣水产出结算，暂保留
    public long calcCampPlayerAfkHolyWaterReward(int camp, long playerLastSettleTime) {
        WarCampInfo.Builder builder = totalCampInfo.get(camp);
        if (builder == null) {
            return 0;
        }
        long curTime = GlobalTick.getInstance().getCurrentTime();
        if (playerLastSettleTime > curTime) {
            return 0;
        }
        // 已扩大1000倍
        long warHolyWater = 0;
        for (WarCampAkfInfo bossEfficacy : builder.getBossEfficacyPlusList()) {
            if (bossEfficacy.getDefeatBossTime() > curTime) {
                continue;
            }
            if (playerLastSettleTime == 0 || playerLastSettleTime < bossEfficacy.getDefeatBossTime()) {
                warHolyWater += (curTime - bossEfficacy.getDefeatBossTime()) * bossEfficacy.getCampBossHolyWaterEfficacy() / TimeUtil.MS_IN_A_MIN;
            } else {
                warHolyWater += (curTime - playerLastSettleTime) * bossEfficacy.getCampBossHolyWaterEfficacy() / TimeUtil.MS_IN_A_MIN;
            }
        }
        return warHolyWater;
    }

    public void onTick() {
        long curTime = GlobalTick.getInstance().getCurrentTime();
        if (updateStateTime == 0 || updateStateTime > curTime) {
            return;
        }
        switch (roomState) {
            case RoomState.EndState: {
                clear();
                updateStateTime = 0;
                break;
            }
            case RoomState.SettleState: {
                if (curTime >= getEndPlayTimestamp() - TimeUtil.MS_IN_A_MIN) {
                    if (!roomController) {
                        break;
                    }
                    settleTheWar();
                    setRoomState(RoomState.EndState);
                    setModified(true);
                }
                updateStateTime = curTime + 500l;
                break;
            }
            case RoomState.FightingState: {
                if (initFinish && roomController) {
                    if (curTime >= getEndPlayTimestamp() - TheWarConstConfig.getById(GameConst.ConfigId).getPreendtime() * TimeUtil.MS_IN_A_MIN) {
                        setRoomState(RoomState.SettleState);
                        setModified(true);
                        setPreSettleFlag(true);
                    }

                    if (curTime > updateCacheTime) {
                        updateRoomCache();
                        setModified(true);
                        updateCacheTime = curTime + 3000l;
                    }

                    refreshMonster(curTime);
                    updateMonsterExpireInfo(curTime);
                    updateBossGridHp(curTime);
                }
                updateStateTime = curTime + 500l;
                break;
            }
            default: {
                break;
            }
        }
    }

}

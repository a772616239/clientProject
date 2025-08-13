package model.thewar.warmap;

import common.GameConst.EventType;
import common.SyncExecuteFunction;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import model.thewar.warmap.config.TotalWarMapCfgData;
import model.thewar.warmap.config.WarMapConfig;
import model.thewar.warmap.grid.BossGrid;
import model.thewar.warmap.grid.CrystalTowerGrid;
import model.thewar.warmap.grid.FootHoldGrid;
import model.thewar.warmap.grid.WarGridDefaultProp;
import model.thewar.warmap.grid.WarMapGrid;
import model.thewar.warplayer.dbCache.WarPlayerCache;
import model.thewar.warplayer.entity.WarPlayer;
import model.thewar.warroom.entity.WarRoom;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TheWar.StationTroopsInfo;
import protocol.TheWarDB.WarCampCanReachPos;
import protocol.TheWarDB.WarCampCanReachPos.Builder;
import protocol.TheWarDefine.Position;
import protocol.TheWarDefine.TheWarCell;
import protocol.TheWarDefine.TheWarCellPropertyEnum;
import protocol.TheWarDefine.TheWarCellTypeEnum;
import protocol.TheWarDefine.TheWarRetCode;
import protocol.TheWarDefine.WarPetData;
import protocol.TheWarDefine.WarTeamType;
import server.event.Event;
import server.event.EventManager;
import util.GameUtil;
import util.LogUtil;

public class WarMapData {
    private String roomIdx; // 房间唯一id，亦作为自己的唯一id
    private int length;
    private int height;
    private Map<Position, WarMapGrid> gridMap; // 所有格子信息

    private Map<Position, BossGrid> bossGirdMap; // 所有boss格子信息

    private Map<Position, Position> masterPosMap; // <格子位置，主格子位置>

    private List<WarMapGrid> bornPosList;

    public WarMapData(String roomIdx) {
        this.roomIdx = roomIdx;
        this.gridMap = new ConcurrentHashMap<>();
        this.masterPosMap = new ConcurrentHashMap<>();
        this.bornPosList = new ArrayList<>();
        this.bossGirdMap = new ConcurrentHashMap<>();
    }

    public String getRoomIdx() {
        return roomIdx;
    }

    public void setRoomIdx(String roomIdx) {
        this.roomIdx = roomIdx;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean init(WarRoom warRoom) {
//        Map<Integer, List<Position>> monsterGroupMap = new HashMap<>();
//        if (!initMapGrids(warRoom.getIdx(), mapName, monsterGroupMap)) {
//            return false;
//        }
//        if (!monsterGroupMap.isEmpty()) {
//            Event event = Event.valueOf(EventType.ET_TheWar_AddRefreshMonsterCount, GameUtil.getDefaultEventSource(), warRoom);
//            event.pushParam(monsterGroupMap);
//            EventManager.getInstance().dispatchEvent(event);
//        }
        if (!initMapGrids(warRoom.getIdx(), warRoom.getMapName())) {
            return false;
        }
        LogUtil.info("WarRoom[" + getRoomIdx() + "] map init finished");
        return true;
    }

    public boolean initMapGrids(String roomIdx, String mapName) {
        WarMapConfig mapInitData = TotalWarMapCfgData.getInitMapData(mapName);
        if (mapInitData == null) {
            return false;
        }
        setRoomIdx(roomIdx);
        length = mapInitData.getLength();
        height = mapInitData.getHeight();
        Position.Builder masterPosBuilder = Position.newBuilder();
        for (Entry<Position, TheWarCell> entry : mapInitData.getInitGrids().entrySet()) {
            TheWarCell cell = entry.getValue();
            int gridType = (int) WarGridDefaultProp.getDefaultPropVal(cell.getName(), TheWarCellPropertyEnum.TWCP_CellType_VALUE);
            WarMapGrid grid = WarMapManager.createGridByType(gridType);
            grid.setRoomIdx(roomIdx);
            grid.setName(cell.getName());
            grid.setPos(cell.getPos());
            if (grid.getPropValue(TheWarCellPropertyEnum.TWCP_CellType_VALUE) == TheWarCellTypeEnum.TWCT_NULL_VALUE) {
                long posLongVal = grid.getPropValue(TheWarCellPropertyEnum.TWCP_MasterCellPos_VALUE);
                masterPosBuilder.setX((int) (posLongVal >>> 32));  // 高32位x
                masterPosBuilder.setY((int) posLongVal);           // 低32位y
            } else {
                // 记录出生点
                addIfIsBornPos(grid);
                masterPosBuilder.setX(grid.getPos().getX());
                masterPosBuilder.setY(grid.getPos().getY());
            }
            masterPosMap.put(grid.getPos(), masterPosBuilder.build());
            addNewGrid(grid);
        }

        // 添加相邻节点
        Position pos;
        Position masterPos;
        Position aroundMasterPos;
        WarMapGrid warMapGrid;
        WarMapGrid masterGrid;
        WarMapGrid anotherMasterGrid;
        Position.Builder anotherPos = Position.newBuilder();

        for (Entry<Position, WarMapGrid> entry : gridMap.entrySet()) {
            pos = entry.getKey();
            warMapGrid = entry.getValue();
            masterPos = masterPosMap.get(warMapGrid.getPos());
            masterGrid = getMapGridByPos(masterPos);
            if (masterGrid == null) {
                continue;
            }
            // 传送门进度初始化,后续计算不遍历
            if (warMapGrid instanceof CrystalTowerGrid) {
                long portalPosVal = warMapGrid.getPropValue(TheWarCellPropertyEnum.TWCP_CrTowerHostPortalPos_VALUE);
                anotherPos.setX((int) (portalPosVal >>> 32));
                anotherPos.setY((int) portalPosVal);
                Position portalPos = masterPosMap.get(anotherPos.build());
                WarMapGrid portalGrid = portalPos != null ? gridMap.get(portalPos) : null;
                if (portalGrid != null) {
                    long crystalMaxVal = warMapGrid.getPropValue(TheWarCellPropertyEnum.TWCP_CrTowerLoadMaxValue_VALUE);
                    crystalMaxVal += portalGrid.getPropValue(TheWarCellPropertyEnum.TWCP_CrTowerLoadMaxValue_VALUE);
                    portalGrid.setPropValue(TheWarCellPropertyEnum.TWCP_CrTowerLoadMaxValue_VALUE, crystalMaxVal);
                }
            }
            if (warMapGrid instanceof FootHoldGrid) {
                FootHoldGrid ftGrid = (FootHoldGrid) warMapGrid;
//                if (monsterGroupMap != null) {
//                    int monsterCfgId = (int) ftGrid.getPropValue(TheWarCellPropertyEnum.TWCP_MonsterRefreshCfgId_VALUE);
//                    if (monsterCfgId > 0) {
//                        List<Position> monsterGrids = monsterGroupMap.get(monsterCfgId);
//                        if (monsterGrids == null) {
//                            monsterGrids = new ArrayList<>();
//                            monsterGroupMap.put(monsterCfgId, monsterGrids);
//                        }
//                        monsterGrids.add(masterPos);
//                    }
//                }

                ftGrid.refreshFightMakeId();

                if (ftGrid.getPropValue(TheWarCellPropertyEnum.TWCP_CellType_VALUE) == TheWarCellTypeEnum.TWCT_Boss_FootHold_VALUE) {
                    bossGirdMap.put(ftGrid.getPos(), (BossGrid) ftGrid);
                }
            }
            aroundMasterPos = masterPosMap.get(anotherPos.setX(pos.getX()).setY(pos.getY() - 1).build()); // 下
            if (aroundMasterPos != null && !aroundMasterPos.equals(masterPos)) {
                anotherMasterGrid = getMapGridByPos(aroundMasterPos);
                if (anotherMasterGrid != null) {
                    masterGrid.addAroundGrid(anotherMasterGrid);
                }
            }

            aroundMasterPos = masterPosMap.get(anotherPos.setX(pos.getX() + 1).setY(pos.getY()).build()); // 右
            if (aroundMasterPos != null && !aroundMasterPos.equals(masterPos)) {
                anotherMasterGrid = getMapGridByPos(aroundMasterPos);
                if (anotherMasterGrid != null) {
                    masterGrid.addAroundGrid(anotherMasterGrid);
                }
            }

            aroundMasterPos = masterPosMap.get(anotherPos.setX(pos.getX()).setY(pos.getY() + 1).build()); // 上
            if (aroundMasterPos != null && !aroundMasterPos.equals(masterPos)) {
                anotherMasterGrid = getMapGridByPos(aroundMasterPos);
                if (anotherMasterGrid != null) {
                    masterGrid.addAroundGrid(anotherMasterGrid);
                }
            }

            aroundMasterPos = masterPosMap.get(anotherPos.setX(pos.getX() - 1).setY(pos.getY()).build()); // 左
            if (aroundMasterPos != null && !aroundMasterPos.equals(masterPos)) {
                anotherMasterGrid = getMapGridByPos(aroundMasterPos);
                if (anotherMasterGrid != null) {
                    masterGrid.addAroundGrid(anotherMasterGrid);
                }
            }

            if (pos.getY() % 2 == 0) {
                aroundMasterPos = masterPosMap.get(anotherPos.setX(pos.getX() - 1).setY(pos.getY() + 1).build()); // y为偶数左上(与客户端约定)
            } else {
                aroundMasterPos = masterPosMap.get(anotherPos.setX(pos.getX() + 1).setY(pos.getY() + 1).build()); // y为奇数右上(与客户端约定)
            }
            if (aroundMasterPos != null && !aroundMasterPos.equals(masterPos)) {
                anotherMasterGrid = getMapGridByPos(aroundMasterPos);
                if (anotherMasterGrid != null) {
                    masterGrid.addAroundGrid(anotherMasterGrid);
                }
            }

            if (pos.getY() % 2 == 0) {
                aroundMasterPos = masterPosMap.get(anotherPos.setX(pos.getX() - 1).setY(pos.getY() - 1).build()); // y为偶数左下(与客户端约定)
            } else {
                aroundMasterPos = masterPosMap.get(anotherPos.setX(pos.getX() + 1).setY(pos.getY() - 1).build()); // y为偶数右下(与客户端约定)
            }
            if (aroundMasterPos != null && !aroundMasterPos.equals(masterPos)) {
                anotherMasterGrid = getMapGridByPos(aroundMasterPos);
                if (anotherMasterGrid != null) {
                    masterGrid.addAroundGrid(anotherMasterGrid);
                }
            }
        }

        // 移除空节点
        masterPosMap.forEach((gridPos, gridMasterPos) -> {
            if (gridPos.equals(gridMasterPos)) {
                return;
            }
            gridMap.remove(gridPos);
        });

        bornPosOrder();

        LogUtil.debug("WarRoom[" + getRoomIdx() + "] init map bornPos size=" + bornPosList.size());
        return true;
    }

    public void addIfIsBornPos(WarMapGrid grid) {
        if (grid.getPropValue(TheWarCellPropertyEnum.TWCP_PlayerSpawn_VALUE) > 0) {
            bornPosList.add(grid);
        }
    }

    protected void bornPosOrder() {
        bornPosList.sort((pos1, pos2) -> (int) (pos1.getPropValue(TheWarCellPropertyEnum.TWCP_SpawnOrder_VALUE) - pos2.getPropValue(TheWarCellPropertyEnum.TWCP_SpawnOrder_VALUE)));
    }

    public WarMapGrid getCurrentBornPosGrid(int curPlayerSize) {
        if (curPlayerSize >= bornPosList.size()) {
            return null;
        }
        WarMapGrid bornGrid = bornPosList.get(curPlayerSize);
        if (bornGrid == null) {
            return null;
        }
        return bornGrid;
    }

//    public void distributeBornPos(WarRoom warRoom, Map<Integer, WarCampInfo.Builder> playerMap) {
//        try {
//            for (Entry<Integer, WarCampInfo.Builder> entry : playerMap.entrySet()) {
//                List<WarMapGrid> posList = bornPos.get(entry.getKey());
//                if (CollectionUtils.isEmpty(posList)) {
//                    LogUtil.error("WarRoom[" + getRoomIdx() + "] distributeBornPos is null,camp=" + entry.getKey());
//                    continue;
//                }
//                WarCampInfo.Builder playerList = entry.getValue();
//                if (posList.size() < playerList.getMembersCount()) {
//                    LogUtil.error("WarRoom[" + getRoomIdx() + "] distributeBornPos playerList is null,camp=" + entry.getKey());
//                    return;
//                }
//                Collections.shuffle(posList);
//                for (int i = 0; i < playerList.getMembersCount(); i++) {
//                    WarMapGrid grid = posList.get(i);
//                    String playerIdx = playerList.getMembers(i);
//                    WarPlayer warPlayer = WarPlayerCache.getInstance().queryObject(playerIdx);
//                    if (warPlayer == null) {
//                        continue;
//                    }
//                    SyncExecuteFunction.executeConsumer(grid, gridEntity->{
//                        gridEntity.setPropValue(TheWarCellPropertyEnum.TWCP_Camp_VALUE, warPlayer.getCamp());
//                        gridEntity.setPropValue(TheWarCellPropertyEnum.TWCP_OccupierPlayerId_VALUE, GameUtil.stringToLong(playerIdx, 0));
////                        gridEntity.setPropValue(TheWarCellPropertyEnum.TWCP_PlayerSpawn_VALUE, 0);
//                    });
//
//                    Event event = Event.valueOf(EventType.ET_TheWar_AddFootHoldGrid, grid, warPlayer);
//                    EventManager.getInstance().dispatchEvent(event);
//
//                    Event event1 = Event.valueOf(EventType.ET_TheWar_AddPosGroupGrid, warPlayer, warRoom);
//                    event1.pushParam(grid);
//                    EventManager.getInstance().dispatchEvent(event1);
//                    LogUtil.debug("WarRoom[" + warRoom.getIdx() + "] init player[" + warPlayer.getIdx() + "] bornPosGroup pos x:" + grid.getPos().getX() + ",y:" + grid.getPos().getY());
//                }
//                LogUtil.info("WarRoom[" + warRoom.getIdx() + "] distributeBornPos finish");
//            }
//        } catch (Exception e) {
//            LogUtil.printStackTrace(e);
//        }
//    }

    public WarMapGrid getMapGridByPos(Position pos) {
        if (pos == null) {
            return null;
        }
        Position masterPos = masterPosMap.get(pos);
        if (masterPos == null) {
            return null;
        }
        return gridMap.get(masterPos);
    }

    public Map<Position, WarMapGrid> getAllGrids() {
        return gridMap;
    }

    public void addNewGrid(WarMapGrid grid) {
        gridMap.put(grid.getPos(), grid);
    }

    public boolean isPosBlock(Position pos) {
        WarMapGrid warGrid = gridMap.get(pos);
        return warGrid == null || warGrid.isBlock();
    }

    public RetCodeEnum playerOccupyGrid(WarPlayer warPlayer, Position pos, boolean skipBattle) {
        WarMapGrid grid = getMapGridByPos(pos);
        if (!(grid instanceof FootHoldGrid)) {
            return RetCodeEnum.RCE_TheWar_InvalidGrid; // 该位置不是可占领格子
        }
        FootHoldGrid footHoldGrid = (FootHoldGrid) grid;
        RetCodeEnum ret = SyncExecuteFunction.executeFunction(footHoldGrid, entity -> entity.playerAttackGrid(warPlayer, skipBattle));
        return ret;
    }


    public TheWarRetCode playerStationTroopsGrid(WarPlayer warPlayer, StationTroopsInfo troopsInfo) {
        WarMapGrid grid = getMapGridByPos(troopsInfo.getCellPos());
        if (!(grid instanceof FootHoldGrid)) {
            return TheWarRetCode.TWRC_InvalidPos; // 该位置不是可占领格子
        }
        if (grid.isBlock()) {
            return TheWarRetCode.TWRC_GridIsBlock; // 阻挡格子
        }
        long ownerId = grid.getPropValue(TheWarCellPropertyEnum.TWCP_OccupierPlayerId_VALUE);
        if (GameUtil.stringToLong(warPlayer.getIdx(), 0) != ownerId) {
            return TheWarRetCode.TWRC_NotOccupiedGrid; // 未占领格子
        }
        WarPetData troopsPet = warPlayer.getPlayerData().getPlayerPetsMap().get(troopsInfo.getPetIdx());
        if (troopsPet == null) {
            return TheWarRetCode.TWRC_NotFoundTroopsPet; // 未找到驻防宠物，无法驻扎
        }
        boolean isInAttackTeam = warPlayer.isPetInTeam(WarTeamType.WTT_AttackTeam_VALUE, troopsInfo.getPetIdx());
        if (isInAttackTeam){
            if (warPlayer.getPlayerData().getBattleData().getEnterFightTime() > 0) {
                return TheWarRetCode.TWRC_AttackingOtherGird; // 战斗中，攻击队伍中宠物无法驻防
            } else {
                Event event = Event.valueOf(EventType.ET_TheWar_RemovePetFromTeam, grid, warPlayer);
                event.pushParam(WarTeamType.WTT_AttackTeam_VALUE, troopsInfo.getPetIdx());
                EventManager.getInstance().dispatchEvent(event);
            }
        } else {
            WarMapGrid oldTroopsGrid = null;
            if (troopsPet.getStationIndex() >= 0) {
                oldTroopsGrid = getMapGridByPos(troopsPet.getStationTroopsPos());
                if (oldTroopsGrid.getPropValue(TheWarCellPropertyEnum.TWCP_BattlingTarget_VALUE) > 0) {
                    return TheWarRetCode.TWRC_GridIsUnderAttack; // 宠物驻防的格子正在被攻击
                }
            }
            if (oldTroopsGrid instanceof FootHoldGrid) {
                FootHoldGrid oldFhGrid = (FootHoldGrid) oldTroopsGrid;
                if (oldTroopsGrid.getPos().equals(grid.getPos())) {
                    return TheWarRetCode.TWRC_PetAlreadyStationTroops; // 该宠物已驻防过该位置
                }
                SyncExecuteFunction.executeFunction(oldFhGrid, entity -> entity.clearStationTroopsPet(warPlayer, false));
            }
        }
        FootHoldGrid footHoldGrid = (FootHoldGrid) grid;
        return SyncExecuteFunction.executeFunction(footHoldGrid, entity -> entity.stationTroopsGrid(warPlayer, troopsInfo));
    }

    public TheWarRetCode removeAllStationTroopsPets(WarPlayer warPlayer, Position pos) {
        WarMapGrid grid = getMapGridByPos(pos);
        if (!(grid instanceof FootHoldGrid)) {
            return TheWarRetCode.TWRC_InvalidPos; // 该位置不是可占领格子
        }
        if (grid.isBlock()) {
            return TheWarRetCode.TWRC_GridIsBlock; // 阻挡格子
        }
        long ownerId = grid.getPropValue(TheWarCellPropertyEnum.TWCP_OccupierPlayerId_VALUE);
        if (GameUtil.stringToLong(warPlayer.getIdx(), 0) != ownerId) {
            return TheWarRetCode.TWRC_NotOccupiedGrid; // 未占领格子
        }
        FootHoldGrid footHoldGrid = (FootHoldGrid) grid;
        return SyncExecuteFunction.executeFunction(footHoldGrid, entity -> entity.clearStationTroopsPet(warPlayer, false));
    }

    public TheWarRetCode submitCrystalDp(WarPlayer warPlayer, Position pos, int count) {
        WarMapGrid grid = getMapGridByPos(pos);
        if (!(grid instanceof CrystalTowerGrid)) {
            return TheWarRetCode.TWRC_InvalidPos;
        }
        CrystalTowerGrid crystalGrid = (CrystalTowerGrid) grid;
        TheWarRetCode ret = SyncExecuteFunction.executeFunction(crystalGrid, entity -> entity.submitDpResource(this, warPlayer, count));
        return ret;
    }

    public void calcAddCampPos(WarMapGrid grid, Map<Integer, WarCampCanReachPos.Builder> campGroupMap, int camp, int mergeGroup, HashSet<Position> checkedPosSet, int i) {
        if (mergeGroup == 0 || i >= 100000) {
            return;
        }
        LogUtil.debug("WarRoom[" + getRoomIdx() + "] CalcAddPos camp =" + camp + ",mergeGroup=" + mergeGroup + ",pos=[" + grid.getPos().getX() + "," + grid.getPos().getY() + "]");
        int aroundGroup = 0;
        WarMapGrid aroundGrid;
        for (Position aroundPos : grid.getAroundGrids()) {
            if (checkedPosSet.contains(aroundPos)) {
                continue;
            }
            checkedPosSet.add(aroundPos);
            aroundGrid = getMapGridByPos(aroundPos);
            if (!(aroundGrid instanceof FootHoldGrid)) {
                continue;
            }
            String playerIdx = GameUtil.longToString(aroundGrid.getPropValue(TheWarCellPropertyEnum.TWCP_OccupierPlayerId_VALUE), "");
            WarPlayer player = WarPlayerCache.getInstance().queryObject(playerIdx);
            if (player == null || player.getCamp() != camp) {
                continue;
            }
            Optional<Entry<Integer, Builder>> findOpt = campGroupMap.entrySet().stream().filter(entry -> entry.getValue().getGroupPosList().contains(aroundPos)).findFirst();
            if (findOpt.isPresent()) {
                aroundGroup = findOpt.get().getKey();
            }
            WarCampCanReachPos.Builder campPosBuilder;
            if (aroundGroup != mergeGroup) {
                if (aroundGroup > 0) {
                    campPosBuilder = campGroupMap.get(aroundGroup);
                    if (campPosBuilder != null) {
                        campPosBuilder.clear();
                    }
                }
            }
            campPosBuilder = campGroupMap.get(mergeGroup);
            if (campPosBuilder == null) {
                campPosBuilder = WarCampCanReachPos.newBuilder();
                campGroupMap.put(mergeGroup, campPosBuilder);
            }
            if (!campPosBuilder.getGroupPosList().contains(aroundPos)) {
                campPosBuilder.addGroupPos(aroundPos);
                Integer count = campPosBuilder.getGroupPlayersMap().get(playerIdx);
                campPosBuilder.putGroupPlayers(playerIdx, count != null ? ++count : 1);
            }
            calcAddCampPos(aroundGrid, campGroupMap, camp, mergeGroup, checkedPosSet, ++i);
        }
    }

//    public void checkAndAddToCampGroup(WarMapGrid grid, Map<Integer, WarCampCanReachPos.Builder> campGroupMap, int camp, int mergeGroup, HashSet<Position> checkedPosSet) {
//        if (grid == null) {
//            return;
//        }
//
//    }

    public void calcRemoveCampPos(WarMapGrid grid, Map<Integer, WarCampCanReachPos.Builder> campGroupMap, int camp, int mergeGroup, HashSet<Position> checkedPosSet, int i) {
        if (grid == null || mergeGroup == 0 || i >= 100000) {
            return;
        }
        String playerIdx;
        WarPlayer player;
        if (!checkedPosSet.contains(grid.getPos())) {
            checkedPosSet.add(grid.getPos());
            playerIdx = GameUtil.longToString(grid.getPropValue(TheWarCellPropertyEnum.TWCP_OccupierPlayerId_VALUE), "");
            player = WarPlayerCache.getInstance().queryObject(playerIdx);
            if (player != null && player.getCamp() == camp) {
                WarCampCanReachPos.Builder campPosBuilder = campGroupMap.get(mergeGroup);
                if (campPosBuilder == null) {
                    campPosBuilder = WarCampCanReachPos.newBuilder();
                    campGroupMap.put(mergeGroup, campPosBuilder);
                }
                if (!campPosBuilder.getGroupPosList().contains(grid)) {
                    campPosBuilder.addGroupPos(grid.getPos());
                    Integer count = campPosBuilder.getGroupPlayersMap().get(playerIdx);
                    campPosBuilder.putGroupPlayers(playerIdx, count != null ? ++count : 1);
                }
            }
        }
        WarMapGrid aroundGrid;
        for (Position aroundPos : grid.getAroundGrids()) {
            if (checkedPosSet.contains(aroundPos)) {
                continue;
            }
            checkedPosSet.add(aroundPos);
            aroundGrid = getMapGridByPos(aroundPos);
            if (!(aroundGrid instanceof FootHoldGrid)) {
                continue;
            }
            playerIdx = GameUtil.longToString(aroundGrid.getPropValue(TheWarCellPropertyEnum.TWCP_OccupierPlayerId_VALUE), "");
            player = WarPlayerCache.getInstance().queryObject(playerIdx);
            if (player == null || player.getCamp() != camp) {
                continue;
            }

            WarCampCanReachPos.Builder campPosBuilder = campGroupMap.get(mergeGroup);
            if (campPosBuilder == null) {
                campPosBuilder = WarCampCanReachPos.newBuilder();
                campGroupMap.put(mergeGroup, campPosBuilder);
            }
            if (!campPosBuilder.getGroupPosList().contains(aroundPos)) {
                campPosBuilder.addGroupPos(aroundPos);
                Integer count = campPosBuilder.getGroupPlayersMap().get(playerIdx);
                campPosBuilder.putGroupPlayers(playerIdx, count != null ? ++count : 1);
            }
            calcRemoveCampPos(aroundGrid, campGroupMap, camp, mergeGroup, checkedPosSet, ++i);
        }
    }

    public Set<Position> getAllBossGrids() {
        return bossGirdMap.keySet();
    }
}

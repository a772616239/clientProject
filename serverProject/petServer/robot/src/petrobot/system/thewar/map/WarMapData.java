package petrobot.system.thewar.map;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.Setter;
import petrobot.system.thewar.WarRoomCache;
import petrobot.system.thewar.config.TotalWarMapCfgData;
import petrobot.system.thewar.config.WarMapConfig;
import petrobot.system.thewar.map.grid.BossGrid;
import petrobot.system.thewar.map.grid.CrystalTowerGrid;
import petrobot.system.thewar.map.grid.FootHoldGrid;
import petrobot.system.thewar.map.grid.WarMapGrid;
import petrobot.util.LogUtil;
import protocol.TheWarDefine.Position;
import protocol.TheWarDefine.TheWarCell;
import protocol.TheWarDefine.TheWarCellPropertyEnum;
import protocol.TheWarDefine.TheWarCellTypeEnum;

@Getter
@Setter
public class WarMapData {
    private String roomIdx; // 房间唯一id，亦作为自己的唯一id
    private String mapName;
    private int length;
    private int height;
    private Map<Position, WarMapGrid> gridMap; // 所有格子信息

    private Map<Position, BossGrid> bossGirdMap; // 所有boss格子信息

    private Map<Position, Position> masterPosMap; // <格子位置，主格子位置>

    public WarMapData(String roomIdx, String mapName) {
        this.roomIdx = roomIdx;
        this.mapName = mapName;
        this.gridMap = new ConcurrentHashMap<>();
        this.masterPosMap = new ConcurrentHashMap<>();
        this.bossGirdMap = new ConcurrentHashMap<>();
        init(mapName);
    }

    public void init(String mapName) {
        initMapGrids(mapName);
        LogUtil.info("WarRoom[" + getRoomIdx() + "] map init finished");
    }

    public void clear() {
        mapName = null;
        length = 0;
        height = 0;
        gridMap.clear();
        bossGirdMap.clear();
        masterPosMap.clear();
    }

    public boolean initMapGrids(String mapName) {
        WarMapConfig mapInitData = TotalWarMapCfgData.getInitMapData(mapName);
        if (mapInitData == null) {
            return false;
        }
        setLength(mapInitData.getLength());
        setHeight(mapInitData.getHeight());
        Position.Builder masterPosBuilder = Position.newBuilder();
        for (Entry<Position, TheWarCell> entry : mapInitData.getInitGrids().entrySet()) {
            TheWarCell cell = entry.getValue();
            int gridType = (int) WarGridDefaultProp.getDefaultPropVal(cell.getName(), TheWarCellPropertyEnum.TWCP_CellType_VALUE);
            WarMapGrid grid = WarRoomCache.getInstance().createGridByType(gridType);
            grid.setRoomIdx(roomIdx);
            grid.setName(cell.getName());
            grid.setPos(cell.getPos());
            if (grid.getPropValue(TheWarCellPropertyEnum.TWCP_CellType_VALUE) == TheWarCellTypeEnum.TWCT_NULL_VALUE) {
                long posLongVal = grid.getPropValue(TheWarCellPropertyEnum.TWCP_MasterCellPos_VALUE);
                masterPosBuilder.setX((int) (posLongVal >>> 32));  // 高32位x
                masterPosBuilder.setY((int) posLongVal);           // 低32位y
            } else {
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
        return true;
    }

    public void addNewGrid(WarMapGrid grid) {
        gridMap.put(grid.getPos(), grid);
    }

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
}

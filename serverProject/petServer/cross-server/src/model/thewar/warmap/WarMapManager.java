package model.thewar.warmap;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import model.thewar.warmap.grid.BossGrid;
import model.thewar.warmap.grid.CrystalTowerGrid;
import model.thewar.warmap.grid.FootHoldGrid;
import model.thewar.warmap.grid.PortalGrid;
import model.thewar.warmap.grid.WarMapGrid;
import protocol.TheWarDefine.TheWarCellTypeEnum;

public class WarMapManager {
    private static WarMapManager instance;

    private Map<String, WarMapData> totalWarMapData = new ConcurrentHashMap<>();

    public static WarMapManager getInstance() {
        if (instance == null) {
            synchronized(WarMapManager.class) {
                instance = new WarMapManager();
            }
        }
        return instance;
    }

    public static WarMapGrid createGridByType(int gridType) {
        switch (gridType) {
            case TheWarCellTypeEnum.TWCT_FootHold_VALUE:
                return new FootHoldGrid();
            case TheWarCellTypeEnum.TWCT_Boss_FootHold_VALUE:
                return new BossGrid();
            case TheWarCellTypeEnum.TWCT_CrystalTower_VALUE:
                return new CrystalTowerGrid();
            case TheWarCellTypeEnum.TWCT_Portal_VALUE:
                return new PortalGrid();
            default:
                return new WarMapGrid();
        }
    }

    public void addMapData(WarMapData mapData) {
        if (mapData == null) {
            return;
        }
        totalWarMapData.merge(mapData.getRoomIdx(), mapData, (oldVal, newVal) -> newVal);
    }

    public WarMapData getRoomMapData(String roomIdx) {
        return totalWarMapData.get(roomIdx);
    }

    public void clearMapData(String roomIdx) {
        totalWarMapData.remove(roomIdx);
    }
}

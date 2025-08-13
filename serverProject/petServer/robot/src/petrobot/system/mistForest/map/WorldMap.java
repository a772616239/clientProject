package petrobot.system.mistForest.map;

import cfg.MistMapConfig;
import cfg.MistMapConfigObject;
import cfg.MistWorldMapConfig;
import cfg.MistWorldMapConfigObject;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import petrobot.system.mistForest.map.grid.Grid;
import petrobot.system.mistForest.map.grid.ShopGrid;
import petrobot.system.mistForest.obj.MistObj;
import petrobot.system.mistForest.obj.staticobj.MistBag;
import petrobot.util.LogUtil;
import protocol.MistForest.EnumGridType;

@Getter
@Setter
public class WorldMap {
    private Grid[][] worldMap; // 地图信息
    private int weight;
    private int height;

    private int safeX0;
    private int safeY0;
    private int safeX1;
    private int safeY1;

    public boolean init(int mapId) {
        MistWorldMapConfigObject mapData = MistWorldMapConfig.getByMapid(mapId);
        if (mapData == null) {
            return false;
        }
        MistMapConfigObject mapCfg = MistMapConfig.getByMapid(mapData.getMapid());
        if (mapCfg == null) {
            return false;
        }
        // 地图size
        weight = mapData.getMapsize()[0];
        height = mapData.getMapsize()[1];

        // 安全区
        int[] safeRegion = mapData.getSaferegion();
        if (safeRegion != null && safeRegion.length >= 4) {
            safeX0 = Integer.min(safeRegion[0], safeRegion[2]);
            safeY0 = Integer.min(safeRegion[1], safeRegion[3]);
            safeX1 = Integer.max(safeRegion[0], safeRegion[2]);
            safeY1 = Integer.max(safeRegion[1], safeRegion[3]);
        }

        worldMap = new Grid[weight][height];
        for (int i = 0; i < worldMap.length; i++) {
            worldMap[i] = new Grid[height];
        }

        generateMapBlock(mapCfg.getMapblock());
        generateMapBlock(mapCfg.getMapblock1());
        generateMapBlock(mapCfg.getMapblock2());

        for (int i = 0; i < worldMap.length; i++) {
            for (int j = 0; j < worldMap[i].length; j++) {
                if (worldMap[i][j] == null) {
                    worldMap[i][j] = generateGrid(0);
                    if (isInSafeRegion(i, j)) {
                        worldMap[i][j].setSafeRegion(true);
                    }
                }
            }
        }
        return true;
    }

    protected void generateMapBlock(int[][] gridData) {
        if (gridData == null || gridData.length <= 1) {
            return;
        }
        for (int i = 0; i < gridData.length; i++) {
            int posX = gridData[i][0];
            int posY = gridData[i][1];
            int block = gridData[i][2];
            int gridType = 0;
            if (gridData[i].length > 3) {
                gridType = gridData[i][3];
            }
            Grid grid = generateGrid(gridType);
            worldMap[posX][posY] = grid;
            if (isInSafeRegion(posX, posY)) {
                grid.setSafeRegion(true);
            }
            grid.setBlocked(block > 0);
            if (grid instanceof ShopGrid) {
                ((ShopGrid) grid).setShopId(gridData[i][4]);
            }
        }
    }

    protected Grid generateGrid(int gridType) {
        switch (gridType) {
            case EnumGridType.EGT_Shop_VALUE:
                return new ShopGrid(gridType);
            default:
                return new Grid(gridType);
        }
    }

    public void clear() {
        if (worldMap != null) {
            worldMap = null;
        }

        weight = 0;
        height = 0;
    }


    public boolean isPosValid(int x, int y) {
        return x >= 0 && x < weight && y >= 0 && y < height;
    }

    public boolean isPosReachable(int x, int y) {
        if (!isPosValid(x, y)) {
            return false;
        }
        return !worldMap[x][y].isGridBlocked();
    }

    public boolean isInSafeRegion(int x, int y) {
        return x >= safeX0 && x < safeX1 && y >= safeY0 && y < safeY1;
    }

    public Grid getGridByPos(int x, int y) {
        if (!isPosValid(x, y)) {
            return null;
        }
        try {
            return worldMap[x][y];
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return null;
        }
    }

    public void addPosObj(MistObj obj) {
        if (obj == null) {
            return;
        }
        int x = obj.getPos().getX() / 1000;
        int y = obj.getPos().getY() / 1000;
        if (!isPosReachable(x, y)) {
            return;
        }
        worldMap[x][y].getGridObjMap().put(obj.getId(), obj);
    }

    public void removePosObj(MistObj obj) {
        if (obj == null) {
            return;
        }
        int x = obj.getPos().getX() / 1000;
        int y = obj.getPos().getY() / 1000;
        if (!isPosValid(x, y)) {
            return;
        }
        if (worldMap[x][y].getGridObjMap() != null && worldMap[x][y].getGridObjMap().containsKey(obj.getId())) {
            worldMap[x][y].getGridObjMap().remove(obj.getId());
        }
    }

    public List<Long> getBagsByPos(int x, int y) {
        if (!isPosValid(x, y)) {
            return null;
        }
        List<Long> bagList = null;
        for (MistObj obj : worldMap[x][y].getGridObjMap().values()) {
            if (obj instanceof MistBag) {
                if (bagList == null) {
                    bagList = new ArrayList<>();
                }
                bagList.add(obj.getId());
            }
        }
        return bagList;
    }
}

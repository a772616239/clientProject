package model.mistforest.map;

import cfg.MistMapConfig;
import cfg.MistMapConfigObject;
import cfg.MistWorldMapConfig;
import cfg.MistWorldMapConfigObject;
import model.mistforest.MistConst;
import model.mistforest.map.AStar.Astar;
import model.mistforest.map.AStar.Coord;
import model.mistforest.map.AStar.Node;
import model.mistforest.map.Aoi.AoiNode;
import model.mistforest.map.grid.GrassGrid;
import model.mistforest.map.grid.Grid;
import model.mistforest.map.grid.ShopGrid;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.mistobj.MistObject;
import protocol.MistForest.EnumGridType;
import protocol.MistForest.ProtoVector;
import util.LogUtil;

import java.util.List;

public class WorldMap {
    private Grid[][] worldMap; // 地图信息
    private AoiNode[][] aoiNodeArray; // 灯塔列表
    private int weight;
    private int height;

    private int safeX0;
    private int safeY0;
    private int safeX1;
    private int safeY1;

    private int aoiWeight;
    private int aoiHeight;
    private int aoiWeightSize;
    private int aoiHeightSize;

    public boolean init(int rule, int level) {
        MistWorldMapConfigObject mapData = MistWorldMapConfig.getInstance().getByRuleAndLevel(rule, level);
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

        int aoiWeight = mapData.getAoiarea()[0];
        int aoiHeight = mapData.getAoiarea()[1];

        return generateAoiNodes(aoiWeight, aoiHeight);
    }

    protected void generateMapBlock(int[][] gridData) {
        if (gridData == null || gridData.length <= 0) {
            return;
        }
        for (int i = 0; i < gridData.length; i++) {
            if (gridData[i].length < 3) {
                continue;
            }
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
            if (grid instanceof GrassGrid) {
                if (gridData[i].length > 4) {
                    ((GrassGrid) grid).setGrassGroup(gridData[i][4]);
                }
            } else if (grid instanceof ShopGrid) {
                if (gridData[i].length > 4) {
                    ((ShopGrid) grid).setShopId(gridData[i][4]);
                }
            }
        }
    }

    protected Grid generateGrid(int gridType) {
        switch (gridType) {
            case EnumGridType.EGT_Grass_VALUE:
                return new GrassGrid(gridType);
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
        if (aoiNodeArray != null) {
            for (int i = 0; i < aoiNodeArray.length; i++) {
                for (int j = 0; j < aoiNodeArray.length; j++) {
                    aoiNodeArray[i][j].clear();
                }
            }
            aoiNodeArray = null;
        }

        weight = 0;
        height = 0;
        aoiWeightSize = 0;
        aoiHeightSize = 0;
    }

    private boolean generateAoiNodes(int aoiWeight, int aoiHeight) {
        if (aoiWeight <= 0 || aoiHeight <= 0) {
            return false;
        }
        this.aoiWeight = aoiWeight;
        this.aoiHeight = aoiHeight;
        aoiWeightSize = this.weight % aoiWeight == 0 ? this.weight / aoiWeight : this.weight / aoiWeight + 1;
        aoiHeightSize = this.height % aoiHeight == 0 ? this.height / aoiHeight : this.height / aoiHeight + 1;
        aoiNodeArray = new AoiNode[aoiWeightSize][aoiHeightSize];
        for (int i = 0; i < aoiWeightSize; i++) {
            aoiNodeArray[i] = new AoiNode[aoiHeightSize];
            for (int j = 0; j < aoiHeightSize; j++) {
                // 高16位x,低15位y+1(+1为了区分0)
                aoiNodeArray[i][j] = new AoiNode(i << 16 | (j + 1));
            }
        }

        //  添加相邻灯塔
        for (int i = 0; i < aoiWeightSize; i++) {
            for (int j = 0; j < aoiHeightSize; j++) {
                for (int k = -1; k <= 1; k++) {
                    for (int l = -1; l <= 1; l++) {
                        if (k == 0 && l == 0) {
                            continue;
                        }
                        if (i + k >= 0 && i + k < aoiWeightSize && j + l >= 0 && j + l < aoiHeightSize) {
                            aoiNodeArray[i][j].addAroundAoiNode(aoiNodeArray[i + k][j + l]);
                        }
                    }
                }
            }
        }
        return true;
    }

    public int getMaxLength() {
        return Integer.max(weight, height);
    }

    public boolean isPosValid(int x, int y) {
        return x >= 0 && x < weight && y >= 0 && y < height;
    }

    public boolean isPosReachable(int x, int y) {
        if (!isPosValid(x, y)) {
            return false;
        }
        return isPosReachable(null, x, y);
    }

    public boolean isPosReachable(MistFighter fighter, int x, int y) {
        if (!isPosValid(x, y)) {
            return false;
        }
        return !worldMap[x][y].isGridBlockForFighter(fighter);
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

    public AoiNode getAoiNodeById(int key) {
        try {
            int x = key >> 16;
            int y = (short) key - 1;
            if (!isPosValid(x, y)) {
                return null;
            }
            return aoiNodeArray[x][y];
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return null;
        }
    }

    public AoiNode getAoiNodeByPos(int x, int y) {
        try {
            if (!isPosValid(x, y)) {
                return null;
            }
            int xIndex = x / aoiWeight;
            int yIndex = y / aoiHeight;
            if (xIndex < 0 || xIndex >= aoiWeightSize || yIndex < 0 || yIndex >= aoiHeightSize) {
                return null;
            }
            return aoiNodeArray[xIndex][yIndex];
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return null;
        }
    }

    public boolean objFirstEnter(MistObject obj) {
        return objMove(obj, -1, -1);
    }

    public boolean objMove(MistObject obj, int oldX, int oldY) {
        if (obj == null) {
            return false;
        }
        int newX = obj.getPos().getX();
        int newY = obj.getPos().getY();
        if (!isPosValid(newX, newY)){
            return false;
        }
        if (obj instanceof MistFighter) {
            MistFighter fighter = (MistFighter) obj;
            Grid oldGrid = getGridByPos(oldX, oldY);
            if (oldGrid != null) {
                oldGrid.onObjLeave(fighter);
            }
            Grid newGrid = getGridByPos(newX, newY);
            if (newGrid != null) {
                newGrid.onObjEnter(fighter);
            }
        }

        AoiNode oldNode = getAoiNodeById(obj.getAoiNodeKey());
        AoiNode newNode = getAoiNodeByPos(newX, newY);
        if (newNode == null && oldNode != null) {
            oldNode.onObjLeave(obj, null);
        } else if (newNode != null && oldNode == null) {
            newNode.onObjEnter(obj, null);
        } else if (newNode != null && oldNode != null && newNode.getKey() != oldNode.getKey()) {
            oldNode.onObjLeave(obj, newNode);
            newNode.onObjEnter(obj, oldNode);
        }
        return true;
    }

    public List<Coord> findPath(Astar astar, ProtoVector startPos, ProtoVector endPos) {
        List<Coord> path = findPath(astar, startPos.getX(), startPos.getY(), endPos.getX(), endPos.getY());
        if (path != null && !path.isEmpty()) {
            // 去掉初始点
            Coord firstPos = path.get(0);
            if (MistConst.checkSamePos(startPos.getX(), startPos.getY(), firstPos.x, firstPos.y)) {
                path.remove(0);
            }
        }
        return path;
    }

    public List<Coord> findPath(Astar astar, int startX, int startY, int endX, int endY) {
        List<Coord> path = astar.findPath(this.worldMap, this.weight, this.height, new Node(startX, startY), new Node(endX, endY));
//        if (path != null) {
//            LogUtil.debug("========test astar start========");
//            for (Coord coord : path) {
//                LogUtil.debug("========path:[" + coord.x + "," + coord.y + "]========");
//            }
//            LogUtil.debug("========test astar end========");
//        }
        return path;
    }
}

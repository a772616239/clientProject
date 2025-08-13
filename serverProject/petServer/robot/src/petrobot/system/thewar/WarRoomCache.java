package petrobot.system.thewar;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import petrobot.system.thewar.map.grid.BossGrid;
import petrobot.system.thewar.map.grid.CrystalTowerGrid;
import petrobot.system.thewar.map.grid.FootHoldGrid;
import petrobot.system.thewar.map.grid.PortalGrid;
import petrobot.system.thewar.map.grid.WarMapGrid;
import petrobot.system.thewar.room.WarRoom;
import protocol.TheWarDefine.TheWarCellTypeEnum;

public class WarRoomCache {
    private static WarRoomCache instance;

    protected Lock lock = new ReentrantLock();

    public static WarRoomCache getInstance() {
        if (instance == null) {
            synchronized (WarRoomCache.class) {
                if (instance == null) {
                    instance = new WarRoomCache();
                }
            }
        }
        return instance;
    }

    private WarRoomCache() {
        warRoomMap = new ConcurrentHashMap<>();
    }

    private Map<String, WarRoom> warRoomMap;

    public WarRoom getWarRoomByIdx(String roomIdx) {
        return warRoomMap.get(roomIdx);
    }

    public WarRoom createWarRoom(String roomIdx, String mapName) {
        WarRoom room = new WarRoom(roomIdx, mapName);
        warRoomMap.put(roomIdx, room);
        return room;
    }

    public void removeWarRoom(String roomIdx) {
        warRoomMap.remove(roomIdx);
    }

    public WarMapGrid createGridByType(int gridType) {
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
}

package model.mistforest.room.cache;

import cfg.MistWorldMapConfig;
import cfg.MistWorldMapConfigObject;
import common.GameConst;
import common.GlobalData;
import common.GlobalTick;
import common.IdGenerator;
import common.SyncExecuteFunction;
import common.load.ServerConfig;
import datatool.StringHelper;
import java.util.Iterator;
import java.util.Map.Entry;
import model.mistforest.MistConst;
import model.mistforest.room.entity.MistGhostBusterRoom.MistGhostBusterManager;
import model.mistforest.room.entity.MistGhostBusterRoom.MistGhostBusterRoom;
import model.mistforest.room.entity.MistMazeRoom.MazeDataManager;
import model.mistforest.room.entity.MistMazeRoom.MistMazeRoom;
import model.mistforest.room.entity.MistRoom;
import model.obj.ObjCache;
import model.obj.ObjPool;
import model.thewar.warroom.dbCache.WarRoomCache;
import protocol.MistForest.EnumMistRuleKind;
import util.GameUtil;
import util.LogUtil;

public class MistRoomCache extends ObjCache<MistRoom> {
    private static MistRoomCache instance = null;

    private long printRoomInfoTime;

    private MazeDataManager mazeDataManager;
    private MistGhostBusterManager ghostBusterManager;

    public static MistRoomCache getInstance() {
        if (instance == null) {
            instance = new MistRoomCache();
            instance.setObjPool(new ObjPool<>(() -> new MistRoom()));
            instance.init();
        }
        return instance;
    }

    public MazeDataManager getMazeDataManager() {
        return mazeDataManager;
    }

    public int getMistRoomCount() {
        return objMap.size();
    }

    // 获取第一个合适的房间
    public MistRoom getFitMistRoom(int mistRule, int level) {
        MistWorldMapConfigObject mapCfg = MistWorldMapConfig.getInstance().getByRuleAndLevel(mistRule, level);
        if (mapCfg == null || mapCfg.getMaxplayercount() <= 0) {
            return null;
        }
        MistRoom room;
        if (level < 1000) {
            int maxPlayerCount = 0;
            MistRoom maxPlayerCountRoom = null;
            for (Entry<String, MistRoom> entry : objMap.entrySet()) {
                room = entry.getValue();
                if (room.getMistRule() != mistRule || room.getLevel() != level || room.isRoomFull()) {
                    continue;
                }
                if (maxPlayerCount == 0 || maxPlayerCount < room.getMemberCount()) {
                    maxPlayerCount = room.getMemberCount();
                    maxPlayerCountRoom = room;
                }
            }
            if (maxPlayerCountRoom != null) {
                return maxPlayerCountRoom;
            }
        }
        String id = IdGenerator.getInstance().generateId();
        room = createMistRoom(mistRule, id);
        if (room != null) {
            room.init(mapCfg);
        }
        return room;
    }

    public <T extends MistRoom> T createMistRoom(int mistRule, String id) {
        MistRoom room;
        switch (mistRule) {
            case EnumMistRuleKind.EMRK_Common_VALUE: {
                room = new MistRoom();
                break;
            }
            case EnumMistRuleKind.EMRK_Maze_VALUE: {
                room = new MistMazeRoom();
                break;
            }
            case EnumMistRuleKind.EMRK_GhostBuster_VALUE: {
                room = new MistGhostBusterRoom();
                break;
            }
            default:
                return null;
        }
        room.setIdx(id);
        return (T) room;
    }

    // 负载均衡积分简单算法
    public long calcCrossServerInfo(int mistMode, int level) {
        int lowPlayerCountIntegral = 0; // 最小房间剩余人数
        int highPlayerCountIntegral = 0; // 其他影响因素积分
        if (GlobalData.getInstance().isServerFull()) {
            highPlayerCountIntegral = 1000;
            lowPlayerCountIntegral = 100000; // 服务器人数已满加100000权重
            return GameUtil.mergeIntToLong(highPlayerCountIntegral, lowPlayerCountIntegral);
        }

        for (MistRoom room : objMap.values()) {
            if (room.getMistRule() == mistMode && room.getLevel() == level) {
                int emptyCount = MistConst.MistRoomMaxPlayerCount - room.getMemberCount();
                if (highPlayerCountIntegral <= 0 || highPlayerCountIntegral > emptyCount) {
                    highPlayerCountIntegral = emptyCount;
                }
                if (GlobalData.getInstance().getOnlinePlayerCount() + emptyCount >= GameConst.Max_Online_Player_Count - 10) {
                    lowPlayerCountIntegral += 10000; // 快到服务器上限的影响权重加10000
                }
            } else {
                lowPlayerCountIntegral += room.getMemberList().size() * 100;
            }
        }

        boolean isArenaServer = ServerConfig.getInstance().isArenaServer();
        if (isArenaServer) {
            lowPlayerCountIntegral += 2000; // 竞技场服加2000权重
        }

        lowPlayerCountIntegral += WarRoomCache.getInstance().getWarRoomCount() * 5000; // 每个远征房间数加5000权重
        if (highPlayerCountIntegral <= 0) {
            highPlayerCountIntegral = 1000;
        }
        return GameUtil.mergeIntToLong(highPlayerCountIntegral, lowPlayerCountIntegral);
    }

    @Deprecated
    public int calcCrossServerInfo_tmp(int level) {
        int integral = 0;
        if (GlobalData.getInstance().isServerFull()) {
            integral += 100000; // 服务器人数已满加100000权重
            return integral;
        }
        boolean isArenaServer = ServerConfig.getInstance().isArenaServer();
        if (isArenaServer) {
            integral += 2000; // 竞技场服加2000权重
        }

        int otherLvlIntegral = 0;
        int lowPlayerCountIntegral = 0;
        int highPlayerCountIntegral = 0;
        for (MistRoom room : objMap.values()) {
            if (room.getLevel() == level) {
                int emptyCount = MistConst.MistRoomMaxPlayerCount - room.getMemberCount();
                if (GlobalData.getInstance().getOnlinePlayerCount() + emptyCount >= GameConst.Max_Online_Player_Count - 10) {
                    return integral + 100000;
                } else if (emptyCount > 0 && emptyCount <= 30) { // 剩余空闲人数小于30时 权重缩小十倍
                    highPlayerCountIntegral = Integer.min(highPlayerCountIntegral, emptyCount);
                } else {
                    lowPlayerCountIntegral += emptyCount > 30 ? emptyCount * 10 : 100;
                }
            } else {
                otherLvlIntegral += room.getMemberList().size() * 100;
            }
        }
        if (highPlayerCountIntegral > 0) {
            integral += highPlayerCountIntegral;
        } else if (lowPlayerCountIntegral > 0) {
            integral += lowPlayerCountIntegral;
        } else {
            integral += otherLvlIntegral;
        }
        return integral;
    }

    public void init() {
        mazeDataManager = new MazeDataManager();
        ghostBusterManager = new MistGhostBusterManager();
    }

    public void onGameServerClose(int serverIndex) {
        if (serverIndex <= 0) {
            return;
        }
        for (MistRoom room : objMap.values()) {
            SyncExecuteFunction.executeConsumer(room, e -> e.onGameServerClose(serverIndex));
        }
    }

    public void onTick() {
        MistRoom room;
        long curTime = GlobalTick.getInstance().getCurrentTime();
        boolean printRoomInfo = false;
        if (printRoomInfoTime <= curTime) {
            printRoomInfo = true;
            printRoomInfoTime = curTime + ServerConfig.getInstance().getPrintSvrInfoCycle();
        }
        Iterator<Entry<String, MistRoom>> iter = objMap.entrySet().iterator();
        while (iter.hasNext()) {
            room = iter.next().getValue();
            if (printRoomInfo) {
                LogUtil.info("MistRoom[" + room.getIdx() + "] Tick finish,level="
                        + room.getLevel() + ",memberCount=" + room.getMemberCount());
            }
            try {
                room.lockObj();
                room.onTick(curTime);
            } finally {
                room.unlockTickObj();
            }
            if (room.checkRoomNeedRemove(curTime)) {
                room.getObjGenerator().activityBossSettleWhenRoomClear();
                room.clear();
                iter.remove();
            }
        }
        getMazeDataManager().onTick(curTime);
        ghostBusterManager.onTick(curTime);
    }

    /**
     * 当前在迷雾深林中的玩家数
     * @return
     */
    public int getMistTotalPlayerCount() {
        int totalPlayerCount = 0;
        for (MistRoom value : objMap.values()) {
            totalPlayerCount += value.getMemberCount();
        }
        return totalPlayerCount;
    }
}

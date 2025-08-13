package model.mistforest.room.entity.MistMazeRoom;

import cfg.MistMazeAreaConfig;
import cfg.MistMazeAreaConfigObject;
import common.GameConst.RedisKey;
import common.GlobalTick;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import protocol.MistForest.ProtoVector;
import protocol.TransServerCommon.MazeServerRecordData;
import protocol.TransServerCommon.MistMazeSyncData;
import util.JedisUtil;
import static util.JedisUtil.jedis;
import util.LogUtil;
import util.TimeUtil;

public class MazeDataManager {
    protected MistMazeSyncData.Builder mazeSyncData;

    protected long nextSyncTime;

    public MazeDataManager() {
        mazeSyncData = MistMazeSyncData.newBuilder();
    }

    public boolean isMazeOpen() {
        if (mazeSyncData.getActivityId() == 0) {
            return false;
        }
        long curTime = GlobalTick.getInstance().getCurrentTime();
        return mazeSyncData.getStartTime() <= curTime && mazeSyncData.getEndTime() > curTime;
    }

    public synchronized void refreshLevelArea(long curTime) {
        for (MistMazeAreaConfigObject cfg : MistMazeAreaConfig._ix_level.values()) {
            Map<ProtoVector, Integer> mazeTranPosInfo = cfg.getMazeTransPosInfo();
            List<Integer> mazeTransNumList = cfg.getMazenum();
            if (mazeTranPosInfo == null || mazeTransNumList == null) {
                continue;
            }
            boolean oldDataFlag = false;
            MazeServerRecordData.Builder recordBuilder = null;
            MazeServerRecordData.Builder tmpRecordData;
            for (int i = 0; i < mazeSyncData.getMazeServerRecordCount(); i++) {
                tmpRecordData = mazeSyncData.getMazeServerRecordBuilder(i);
                if (tmpRecordData.getAreaLevel() == cfg.getLevel()) {
                    recordBuilder = tmpRecordData;
                    oldDataFlag = true;
                    break;
                }
            }

            if (recordBuilder == null) {
                recordBuilder = MazeServerRecordData.newBuilder();
                recordBuilder.setAreaLevel(cfg.getLevel());
            }
            recordBuilder.clearPos();
            recordBuilder.clearDeltaLv();
            recordBuilder.clearToward();


            if (mazeTransNumList.size() > mazeTranPosInfo.size()) {
                List<Integer> mazeNumList = mazeTransNumList.stream().collect(Collectors.toList());
                Collections.shuffle(mazeNumList);

                int i = 0;
                for (Entry<ProtoVector, Integer> entry : mazeTranPosInfo.entrySet()) {
                    if (i >= mazeTranPosInfo.size()) {
                        break;
                    }
                    recordBuilder.addPos(entry.getKey());
                    recordBuilder.addDeltaLv(mazeNumList.get(i));
                    recordBuilder.addToward(entry.getValue());
                    ++i;
                }
            } else {
                List<ProtoVector> transPosList = mazeTranPosInfo.keySet().stream().collect(Collectors.toList());
                Collections.shuffle(transPosList);
                int tmpCount;
                for (int i = 0; i < transPosList.size(); i++) {
                    if (i < mazeTransNumList.size()) {
                        tmpCount = i;
                    } else {
                        tmpCount = i % mazeTransNumList.size();
                    }
                    ProtoVector pos = transPosList.get(i);
                    Integer toward = mazeTranPosInfo.get(pos);
                    recordBuilder.addPos(pos);
                    recordBuilder.addDeltaLv(mazeTransNumList.get(tmpCount));
                    recordBuilder.addToward(toward != null ? toward : 0);
                }
            }

            if (!oldDataFlag) {
                mazeSyncData.addMazeServerRecord(recordBuilder);
            }
        }
        mazeSyncData.setNextRefreshTime(curTime + mazeSyncData.getRefreshInterval() * TimeUtil.MS_IN_A_HOUR);
    }

    public void notifyRefreshAllRoom() {
//        List<MistRoom> roomList = new ArrayList<>();
//        MistRoomCache.getInstance().collectObj(roomList);
//        for (MistRoom room : roomList) {
//            if (room instanceof MistMazeRoom) {
//                MistMazeRoom mazeRoom = (MistMazeRoom) room;
//                mazeRoom.refreshMazeRoom();
//            }
//        }
    }

    public int getTransDeltaLevel(int level, ProtoVector pos) {
        if (mazeSyncData.getActivityId() == 0) {
            return 0;
        }
        MazeServerRecordData recordData;
        for (int i = 0; i < mazeSyncData.getMazeServerRecordCount(); i++) {
            recordData = mazeSyncData.getMazeServerRecord(i);
            if (recordData.getAreaLevel() != level) {
                continue;
            }
            for (int j = 0; j < recordData.getPosCount(); j++) {
                if (recordData.getPos(j).equals(pos)) {
                    return recordData.getDeltaLv(j);
                }
            }
        }
        return 0;
    }

    public int getTransToward(int level, ProtoVector pos) {
        if (mazeSyncData.getActivityId() == 0) {
            return 0;
        }
        MazeServerRecordData recordData;
        for (int i = 0; i < mazeSyncData.getMazeServerRecordCount(); i++) {
            recordData = mazeSyncData.getMazeServerRecord(i);
            if (recordData.getAreaLevel() != level) {
                continue;
            }
            for (int j = 0; j < recordData.getPosCount(); j++) {
                if (recordData.getPos(j).equals(pos)) {
                    return recordData.getToward(j);
                }
            }
        }
        return 0;
    }

    protected boolean needUpdateLocalMazeData(MistMazeSyncData mazeData, long curTime) {
        if (mazeData.getActivityId() != mazeSyncData.getActivityId()) {
            return true;
        }
        if (mazeData.getStartTime() != mazeSyncData.getStartTime()) {
            return true;
        }
        if (mazeData.getEndTime() != mazeSyncData.getEndTime()) {
            return true;
        }
        if (mazeData.getRefreshInterval() != mazeSyncData.getRefreshInterval()) {
            return true;
        }
        if (mazeData.getNextRefreshTime() != mazeSyncData.getNextRefreshTime()) {
            return true;
        }
        return false;
    }

    public void updateMazeSyncData(long curTime) {
        try {
            byte[] mazeBytesData = jedis.get(RedisKey.MistMazeSyncData.getBytes());
            if (mazeBytesData == null) {
                return;
            }
            MistMazeSyncData mazeData = MistMazeSyncData.parseFrom(mazeBytesData);
            if (mazeData.getStartTime() > curTime || mazeData.getEndTime() <= curTime) {
                return;
            }
            boolean needRefreshMapRecord = mazeSyncData.getNextRefreshTime() > 0 && mazeData.getNextRefreshTime() > mazeSyncData.getNextRefreshTime();
            if (needUpdateLocalMazeData(mazeData, curTime)) {
                if (needRefreshMapRecord) {
                    notifyRefreshAllRoom();
                }
                mazeSyncData.mergeFrom(mazeData);
            } else if (mazeData.getMazeServerRecordCount() <= 0
                    || (mazeData.getRefreshInterval() > 0 && mazeData.getNextRefreshTime() == mazeSyncData.getNextRefreshTime()
                    && mazeSyncData.getNextRefreshTime() <= curTime)) {
                if (!JedisUtil.lockRedisKey(RedisKey.MistMazeUpdateLock, 5000)) {
                    return;
                }
                refreshLevelArea(curTime);
                if (needRefreshMapRecord) {
                    notifyRefreshAllRoom();
                }
                jedis.set(RedisKey.MistMazeSyncData.getBytes(), mazeSyncData.build().toByteArray());
                JedisUtil.unlockRedisKey(RedisKey.MistMazeUpdateLock);
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    public void onTick(long curTime) {
        if (nextSyncTime < curTime) {
            nextSyncTime = curTime + TimeUtil.MS_IN_A_S;
            updateMazeSyncData(curTime);
        }
    }
}

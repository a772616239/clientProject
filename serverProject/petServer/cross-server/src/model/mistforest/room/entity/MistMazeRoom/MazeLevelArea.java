package model.mistforest.room.entity.MistMazeRoom;

import java.util.HashMap;
import java.util.Map;
import protocol.MistForest.ProtoVector;
import util.GameUtil;

public class MazeLevelArea {
    protected int mazeLevel;
    protected Map<Long, Long> gridDeltaLevel;// <(x,y),(deltaLv,toward)>

    public MazeLevelArea(int level) {
        this.mazeLevel = level;
        this.gridDeltaLevel = new HashMap<>();
    }

    public void init() {

    }

    public int getMazeLevel() {
        return mazeLevel;
    }

    public void setTransPosData(long posLong, int deltaLevel, int toward) {
        gridDeltaLevel.put(posLong, GameUtil.mergeIntToLong(deltaLevel, toward));
    }

    public int getTransDeltaLevel(ProtoVector pos) {
        Long transDataObj = gridDeltaLevel.get(GameUtil.mergeIntToLong(pos.getX(), pos.getY()));
        return transDataObj != null ? GameUtil.getHighLong(transDataObj) : 0;
    }

    public int getTransToward(ProtoVector pos) {
        Long transDataObj = gridDeltaLevel.get(GameUtil.mergeIntToLong(pos.getX(), pos.getY()));
        return transDataObj != null ? GameUtil.getLowLong(transDataObj) : 0;
    }
}
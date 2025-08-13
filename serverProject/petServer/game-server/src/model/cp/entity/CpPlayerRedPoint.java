package model.cp.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

public class CpPlayerRedPoint implements Serializable {
    private static final long serialVersionUID = -2769525052696573711L;
    private String mapId;

    private String teamName;

    private long expireTime;

    private Map<Integer, CpCopyMapFloor> floors = new HashMap<>();
    /**
     * 成员
     */
    private List<String> members;
    /**
     * 正在玩的玩家
     */
    private List<String> playingPlayer;

    private Map<String, CpTeamCopyPlayerProgress> progress = new HashMap();


    public void addFloor(CpCopyMapFloor floorData) {
        if (floorData == null) {
            return;
        }
        floors.put(floorData.getFloor(), floorData);
    }

    public void addProgress(CpTeamCopyPlayerProgress progress) {
        if (progress != null) {
            this.progress.put(progress.getPlayerIdx(), progress);
        }

    }

    public CpTeamCopyPlayerProgress getProgress(String playerIdx) {
        return this.progress.get(playerIdx);
    }

    public int queryPlayerFloor(String playerIdx) {
        CpTeamCopyPlayerProgress progress = getProgress(playerIdx);
        if (progress == null) {
            return -1;
        }
        return progress.getFloor();
    }

    public int queryFloorByPointId(int pointId) {
        return pointId / 100;

    }

    public CpCopyMapPoint queryPointById(int pointId) {
        int floor = queryFloorByPointId(pointId);
        CpCopyMapFloor cpCopyMapFloor = floors.get(floor);
        if (cpCopyMapFloor == null) {
            return null;
        }
        return cpCopyMapFloor.getPoints().get(pointId);

    }

    public String getMapId() {
        return mapId;
    }

    public void setMapId(String mapId) {
        this.mapId = mapId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public Map<Integer, CpCopyMapFloor> getFloors() {
        return floors;
    }

    public void setFloors(Map<Integer, CpCopyMapFloor> floors) {
        this.floors = floors;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public List<String> getPlayingPlayer() {
        return playingPlayer;
    }

    public void setPlayingPlayer(List<String> playingPlayer) {
        this.playingPlayer = playingPlayer;
    }

    public Map<String, CpTeamCopyPlayerProgress> getProgress() {
        return progress;
    }

    public void setProgress(Map<String, CpTeamCopyPlayerProgress> progress) {
        this.progress = progress;
    }
}

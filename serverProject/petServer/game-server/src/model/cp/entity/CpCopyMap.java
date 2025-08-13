package model.cp.entity;

import common.tick.GlobalTick;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import model.cp.CpTeamCache;
import protocol.CpFunction;
import server.handler.cp.CpFunctionUtil;


public class CpCopyMap implements Serializable {
    private static final long serialVersionUID = -2769525052696573711L;
    private String mapId;

    private String teamName;

    private long expireTime;

    private Map<Integer, CpCopyMapFloor> floors = new HashMap<>();

    private int teamId;

    private boolean applySettle;

    private Map<String, List<Integer>> alreadyClaimRewardId = new HashMap<>();

    public Map<String, List<Integer>> getAlreadyClaimRewardId() {
        return alreadyClaimRewardId;
    }


    /**
     * 成员
     */
    private List<String> members;


    public boolean isApplySettle() {
        return applySettle;
    }

    public void setApplySettle(boolean applySettle) {
        this.applySettle = applySettle;
    }

    /**
     * 所有玩家
     */
    private List<String> initMembers = new ArrayList<>();

    private Map<String, Long> playerOfflineMap = new HashMap<>();

    private Map<String, CpTeamCopyPlayerProgress> progress = new HashMap();

    private Map<String, CpFunction.CpCopyPlayerState> playerState = new HashMap<>();

    public Map<String, Long> getPlayerOfflineMap() {
        return playerOfflineMap;
    }

    public void setPlayerOfflineMap(Map<String, Long> playerOfflineMap) {
        this.playerOfflineMap = playerOfflineMap;
    }

    public Map<String, CpFunction.CpCopyPlayerState> getPlayerState() {
        return playerState;
    }

    public void setPlayerState(Map<String, CpFunction.CpCopyPlayerState> playerState) {
        this.playerState = playerState;
    }

    public List<String> getRealPlayers() {
        return getOnPlayPlayer();
    }

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


    public CpFunction.CpCopyPlayerState queryPlayerState(String playerIdx) {
        return playerState.get(playerIdx);
    }

    public int getTeamScore() {
        return progress.values().stream().mapToInt(CpTeamCopyPlayerProgress::getStarScore).sum();
    }

    public void addAllInitPlayers(List<String> members) {
        initMembers.addAll(members);
    }

    public void removePlayingPlayerIdx(String playerIdx) {
        this.members.remove(playerIdx);
    }

    public List<String> getOnPlayPlayer() {
        return CpFunctionUtil.findPlayerIds(members);
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

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }



    public Map<String, CpTeamCopyPlayerProgress> getProgress() {
        return progress;
    }

    public void setProgress(Map<String, CpTeamCopyPlayerProgress> progress) {
        this.progress = progress;
    }

    public void putPlayerOffline(String member, long currentTime) {
        this.playerOfflineMap.put(member, currentTime);
    }

    public void playerOffline(String member) {
        this.playerOfflineMap.put(member, GlobalTick.getInstance().getCurrentTime());
        CpTeamCopyPlayerProgress progress = this.getProgress(member);
        if (progress != null) {
            progress.setOffline(true);
        }
        CpTeamCache.getInstance().saveCopyPlayerOfflineTime(member,GlobalTick.getInstance().getCurrentTime());
    }

    public void playerOnline(String member) {
        this.playerOfflineMap.remove(member);
        CpTeamCopyPlayerProgress progress = this.getProgress(member);
        if (progress != null) {
            progress.setOffline(false);
        }
        CpTeamCache.getInstance().removeCopyPlayerLeaveTime(member);
    }

    public void updatePlayerState(String playerIdx, CpFunction.CpCopyPlayerState state) {
        playerState.put(playerIdx, state);
    }

    public boolean claimedReward(String playerIdx, int rewardId) {
        List<Integer> claimRecord = alreadyClaimRewardId.get(playerIdx);

        if (claimRecord == null) {
            return false;
        }
        return claimRecord.contains(rewardId);

    }

    public void addClaimReward(String playerIdx, int rewardId) {
        List<Integer> claimRecord = alreadyClaimRewardId.computeIfAbsent(playerIdx, a -> new ArrayList<>());
        claimRecord.add(rewardId);
    }

    public List<String> getInitRealPlayerIds() {
        return initMembers.stream().filter(e->!CpFunctionUtil.isRobot(e)).collect(Collectors.toList());
    }
}

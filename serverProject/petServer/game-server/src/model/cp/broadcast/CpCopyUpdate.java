package model.cp.broadcast;

import common.IdGenerator;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import model.cp.entity.CpCopyMapPoint;
import model.cp.entity.CpTeamCopyPlayerProgress;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import server.handler.cp.CpFunctionUtil;

public class CpCopyUpdate implements Serializable {

    private static final long serialVersionUID = 2500749495796833350L;
    private String id;

    private int mapId;

    //----------更新点位
    private int pointId;
    private int pointType;
    private int difficult;
    private String battlePlayerIdx;
    //----------更新点位

    //-----------更新玩家信息
    private String playerIdx;
    private int header;
    private int borderId;
    private int starScore;
    private List<Integer> passPoints;
    //-----------更新玩家信息

    private List<String> alreadySettlePlayerIdx = new LinkedList<>();
    private List<String> members = new LinkedList<>();

    public CpCopyUpdate(CpCopyMapPoint point, CpTeamCopyPlayerProgress progress, List<String> members) {
        if (point != null) {
            this.pointId = point.getId();
            this.pointType = point.getPointType();
        }
        if (progress != null) {
            this.playerIdx = progress.getPlayerIdx();
            this.header = progress.getHeader();
            this.borderId = progress.getBorderId();
            this.starScore = progress.getStarScore();
            this.passPoints = progress.getPassPointIds();
        }
        if (!CollectionUtils.isEmpty(members)) {
            this.members = CpFunctionUtil.findPlayerIds(members);
        }
        this.id = IdGenerator.getInstance().generateId();
    }

    public String getBattlePlayerIdx() {
        return battlePlayerIdx;
    }

    public void setBattlePlayerIdx(String battlePlayerIdx) {
        this.battlePlayerIdx = battlePlayerIdx;
    }

    public CpCopyUpdate(CpCopyMapPoint point, String battlePlayerIdx, List<String> members) {
        if (point != null) {
            this.pointId = point.getId();
            this.pointType = point.getPointType();
            this.battlePlayerIdx = battlePlayerIdx;
        }

        if (!CollectionUtils.isEmpty(members)) {
            this.members = CpFunctionUtil.findPlayerIds(members);
        }
        this.id = IdGenerator.getInstance().generateId();
    }


    public void addAllSettlePlayerIdx(Set<String> localPlayerIdx) {
        this.alreadySettlePlayerIdx.addAll(localPlayerIdx);
    }

    public boolean allSettle() {
        return alreadySettlePlayerIdx.size() >= members.size();

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getMapId() {
        return mapId;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    public int getPointId() {
        return pointId;
    }

    public void setPointId(int pointId) {
        this.pointId = pointId;
    }

    public int getPointType() {
        return pointType;
    }

    public void setPointType(int pointType) {
        this.pointType = pointType;
    }

    public int getDifficult() {
        return difficult;
    }

    public void setDifficult(int difficult) {
        this.difficult = difficult;
    }

    public String getPlayerIdx() {
        return playerIdx;
    }

    public void setPlayerIdx(String playerIdx) {
        this.playerIdx = playerIdx;
    }

    public int getHeader() {
        return header;
    }

    public void setHeader(int header) {
        this.header = header;
    }

    public int getBorderId() {
        return borderId;
    }

    public void setBorderId(int borderId) {
        this.borderId = borderId;
    }

    public int getStarScore() {
        return starScore;
    }

    public void setStarScore(int starScore) {
        this.starScore = starScore;
    }

    public List<Integer> getPassPoints() {
        return passPoints;
    }

    public void setPassPoints(List<Integer> passPoints) {
        this.passPoints = passPoints;
    }

    public List<String> getAlreadySettlePlayerIdx() {
        return alreadySettlePlayerIdx;
    }

    public void setAlreadySettlePlayerIdx(List<String> alreadySettlePlayerIdx) {
        this.alreadySettlePlayerIdx = alreadySettlePlayerIdx;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }
}

package model.cp.entity;

import java.io.Serializable;
import java.util.List;
import lombok.Data;
import protocol.PetMessage;

public class CpCopyMapPoint implements Serializable {
    private static final long serialVersionUID = -8336443516735945332L;
    private int id;
    private List<PetMessage.Pet> monsters;
    //0战斗 1事件
    private int pointType;
    //已占领玩家
    private String playerIdx;
    //是否通过
    private boolean pass;
    //难度
    private int difficulty;
    //父节点id
    private int parentId;
    //队伍战力
    private long ability;

    public long getAbility() {
        return ability;
    }

    public void setAbility(long ability) {
        this.ability = ability;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<PetMessage.Pet> getMonsters() {
        return monsters;
    }

    public void setMonsters(List<PetMessage.Pet> monsters) {
        this.monsters = monsters;
    }

    public int getPointType() {
        return pointType;
    }

    public void setPointType(int pointType) {
        this.pointType = pointType;
    }

    public String getPlayerIdx() {
        return playerIdx;
    }

    public void setPlayerIdx(String playerIdx) {
        this.playerIdx = playerIdx;
    }

    public boolean isPass() {
        return pass;
    }

    public void setPass(boolean pass) {
        this.pass = pass;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }
}

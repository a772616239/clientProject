package model.crazyDuel.entity;

import java.io.Serializable;
import lombok.Data;

public class BattleUpdateFloor implements Serializable {
    private static final long serialVersionUID = 4744559136582895727L;
    private int floor;
    private int winIncr;
    private int failIncr;

    public BattleUpdateFloor(int floor) {
        this.floor = floor;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public int getWinIncr() {
        return winIncr;
    }

    public void setWinIncr(int winIncr) {
        this.winIncr = winIncr;
    }

    public int getFailIncr() {
        return failIncr;
    }

    public void setFailIncr(int failIncr) {
        this.failIncr = failIncr;
    }
}

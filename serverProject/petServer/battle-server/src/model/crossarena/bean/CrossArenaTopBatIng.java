package model.crossarena.bean;

public class CrossArenaTopBatIng {
    private String py1;
    private String py2;
    private long roomid;
    private long endTime;
    private int campWin;

    public String getPy1() {
        return py1;
    }

    public void setPy1(String py1) {
        this.py1 = py1;
    }

    public String getPy2() {
        return py2;
    }

    public void setPy2(String py2) {
        this.py2 = py2;
    }

    public long getRoomid() {
        return roomid;
    }

    public void setRoomid(long roomid) {
        this.roomid = roomid;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getCampWin() {
        return campWin;
    }

    public void setCampWin(int campWin) {
        this.campWin = campWin;
    }
}

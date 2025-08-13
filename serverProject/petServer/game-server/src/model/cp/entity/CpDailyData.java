package model.cp.entity;

public class CpDailyData {

    public static final CpDailyData defaultInstance = new CpDailyData();

    //免费复活使用次数
    private int freeReviveNum;
    //购买复活使用次数
    private int buyReviveNum;

    public int getFreeReviveNum() {
        return freeReviveNum;
    }

    public void setFreeReviveNum(int freeReviveNum) {
        this.freeReviveNum = freeReviveNum;
    }

    public int getBuyReviveNum() {
        return buyReviveNum;
    }

    public void setBuyReviveNum(int buyReviveNum) {
        this.buyReviveNum = buyReviveNum;
    }
}

package cfg;

import model.base.baseConfigObject;

public class CumuSignInObject implements baseConfigObject {


    private int days;

    private int[][] rewards;


    public void setDays(int days) {

        this.days = days;

    }

    public int getDays() {

        return this.days;

    }


    public void setRewards(int[][] rewards) {

        this.rewards = rewards;

    }

    public int[][] getRewards() {

        return this.rewards;

    }


}

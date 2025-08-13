package cfg;

import model.base.baseConfigObject;

public class MistSpeedDecreaseObject implements baseConfigObject {


    private int mistlevel;

    private int baseplayerlevel;

    private int[][] speeddecrease;


    public void setMistlevel(int mistlevel) {

        this.mistlevel = mistlevel;

    }

    public int getMistlevel() {

        return this.mistlevel;

    }


    public void setBaseplayerlevel(int baseplayerlevel) {

        this.baseplayerlevel = baseplayerlevel;

    }

    public int getBaseplayerlevel() {

        return this.baseplayerlevel;

    }


    public void setSpeeddecrease(int[][] speeddecrease) {

        this.speeddecrease = speeddecrease;

    }

    public int[][] getSpeeddecrease() {

        return this.speeddecrease;

    }


}

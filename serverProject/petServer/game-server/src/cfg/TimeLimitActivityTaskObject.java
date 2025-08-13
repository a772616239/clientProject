package cfg;

import model.base.baseConfigObject;

public class TimeLimitActivityTaskObject implements baseConfigObject {


    private int id;

    private int missiontype;

    private int targetcount;

    private int addtion;

    private int missionname;

    private int missiondesc;

    private int[][] reward;

    private int endtime;


    public void setId(int id) {

        this.id = id;

    }

    public int getId() {

        return this.id;

    }


    public void setMissiontype(int missiontype) {

        this.missiontype = missiontype;

    }

    public int getMissiontype() {

        return this.missiontype;

    }


    public void setTargetcount(int targetcount) {

        this.targetcount = targetcount;

    }

    public int getTargetcount() {

        return this.targetcount;

    }


    public void setAddtion(int addtion) {

        this.addtion = addtion;

    }

    public int getAddtion() {

        return this.addtion;

    }


    public void setMissionname(int missionname) {

        this.missionname = missionname;

    }

    public int getMissionname() {

        return this.missionname;

    }


    public void setMissiondesc(int missiondesc) {

        this.missiondesc = missiondesc;

    }

    public int getMissiondesc() {

        return this.missiondesc;

    }


    public void setReward(int[][] reward) {

        this.reward = reward;

    }

    public int[][] getReward() {

        return this.reward;

    }


    public void setEndtime(int endtime) {

        this.endtime = endtime;

    }

    public int getEndtime() {

        return this.endtime;

    }


}

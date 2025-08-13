package cfg;

import model.base.baseConfigObject;

public class MistIntegralRewardObject implements baseConfigObject {


    private int index;

    private int rewardintegral;

    private int rewardid;


    public void setIndex(int index) {

        this.index = index;

    }

    public int getIndex() {

        return this.index;

    }


    public void setRewardintegral(int rewardintegral) {

        this.rewardintegral = rewardintegral;

    }

    public int getRewardintegral() {

        return this.rewardintegral;

    }


    public void setRewardid(int rewardid) {

        this.rewardid = rewardid;

    }

    public int getRewardid() {

        return this.rewardid;

    }


}

package cfg;

import model.base.baseConfigObject;

public class MistBagConfigObject implements baseConfigObject {


    private int bagid;

    private int rewardtype;

    private int rewardcount;

    private int bagprogress;


    public void setBagid(int bagid) {

        this.bagid = bagid;

    }

    public int getBagid() {

        return this.bagid;

    }


    public void setRewardtype(int rewardtype) {

        this.rewardtype = rewardtype;

    }

    public int getRewardtype() {

        return this.rewardtype;

    }


    public void setRewardcount(int rewardcount) {

        this.rewardcount = rewardcount;

    }

    public int getRewardcount() {

        return this.rewardcount;

    }


    public void setBagprogress(int bagprogress) {

        this.bagprogress = bagprogress;

    }

    public int getBagprogress() {

        return this.bagprogress;

    }


}

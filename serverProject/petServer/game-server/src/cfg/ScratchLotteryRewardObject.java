package cfg;

import model.base.baseConfigObject;

public class ScratchLotteryRewardObject implements baseConfigObject {


    private int id;

    private int petavatar;

    private int linkcount;

    private int odds;

    private int rewardsid;


    public void setId(int id) {

        this.id = id;

    }

    public int getId() {

        return this.id;

    }


    public void setPetavatar(int petavatar) {

        this.petavatar = petavatar;

    }

    public int getPetavatar() {

        return this.petavatar;

    }


    public void setLinkcount(int linkcount) {

        this.linkcount = linkcount;

    }

    public int getLinkcount() {

        return this.linkcount;

    }


    public void setOdds(int odds) {

        this.odds = odds;

    }

    public int getOdds() {

        return this.odds;

    }


    public void setRewardsid(int rewardsid) {

        this.rewardsid = rewardsid;

    }

    public int getRewardsid() {

        return this.rewardsid;

    }

    public int getQuality() {
        return PetBaseProperties.getQualityByPetId(getPetavatar());
    }

}

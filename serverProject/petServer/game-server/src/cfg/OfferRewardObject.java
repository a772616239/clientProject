package cfg;
import model.base.baseConfigObject;
public class  OfferRewardObject implements baseConfigObject{



private int id;

private int[][] boss;

private int time;

private int[][] petreward;

private int[][] runereward;

private int[][] gemreward;

private int[][] otherreward;

private int[] consume;

private int[] acceptconsume;

private int[][] acceptreward;

private int[][] reward;

private int rewardnum;

private int[] publisherrewardrate;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setBoss(int[][] boss) {

this.boss = boss;

}

public int[][] getBoss() {

return this.boss;

}


public void setTime(int time) {

this.time = time;

}

public int getTime() {

return this.time;

}


public void setPetreward(int[][] petreward) {

this.petreward = petreward;

}

public int[][] getPetreward() {

return this.petreward;

}


public void setRunereward(int[][] runereward) {

this.runereward = runereward;

}

public int[][] getRunereward() {

return this.runereward;

}


public void setGemreward(int[][] gemreward) {

this.gemreward = gemreward;

}

public int[][] getGemreward() {

return this.gemreward;

}


public void setOtherreward(int[][] otherreward) {

this.otherreward = otherreward;

}

public int[][] getOtherreward() {

return this.otherreward;

}


public void setConsume(int[] consume) {

this.consume = consume;

}

public int[] getConsume() {

return this.consume;

}


public void setAcceptconsume(int[] acceptconsume) {

this.acceptconsume = acceptconsume;

}

public int[] getAcceptconsume() {

return this.acceptconsume;

}


public void setAcceptreward(int[][] acceptreward) {

this.acceptreward = acceptreward;

}

public int[][] getAcceptreward() {

return this.acceptreward;

}


public void setReward(int[][] reward) {

this.reward = reward;

}

public int[][] getReward() {

return this.reward;

}


public void setRewardnum(int rewardnum) {

this.rewardnum = rewardnum;

}

public int getRewardnum() {

return this.rewardnum;

}


public void setPublisherrewardrate(int[] publisherrewardrate) {

this.publisherrewardrate = publisherrewardrate;

}

public int[] getPublisherrewardrate() {

return this.publisherrewardrate;

}




}

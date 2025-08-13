package cfg;
import model.base.baseConfigObject;
public class  PatrolMissionConfigObject implements baseConfigObject{



private int missionid;

private int weight;

private int limittime;

private int rewarduprate;




public void setMissionid(int missionid) {

this.missionid = missionid;

}

public int getMissionid() {

return this.missionid;

}


public void setWeight(int weight) {

this.weight = weight;

}

public int getWeight() {

return this.weight;

}


public void setLimittime(int limittime) {

this.limittime = limittime;

}

public int getLimittime() {

return this.limittime;

}


public void setRewarduprate(int rewarduprate) {

this.rewarduprate = rewarduprate;

}

public int getRewarduprate() {

return this.rewarduprate;

}




}

package cfg;
import model.base.baseConfigObject;
public class  RewardConfigObject implements baseConfigObject{



private int rewardid;

private int[][] mustreward;

private int[][] randomreward;

private int randomtimes;




public void setRewardid(int rewardid) {

this.rewardid = rewardid;

}

public int getRewardid() {

return this.rewardid;

}


public void setMustreward(int[][] mustreward) {

this.mustreward = mustreward;

}

public int[][] getMustreward() {

return this.mustreward;

}


public void setRandomreward(int[][] randomreward) {

this.randomreward = randomreward;

}

public int[][] getRandomreward() {

return this.randomreward;

}


public void setRandomtimes(int randomtimes) {

this.randomtimes = randomtimes;

}

public int getRandomtimes() {

return this.randomtimes;

}




}

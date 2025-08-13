package cfg;
import model.base.baseConfigObject;
public class  MineRewardConfigObject implements baseConfigObject{



private int minerewardtype;

private int rewartype;

private int rewardid;

private int dailyrrewarlimit;




public void setMinerewardtype(int minerewardtype) {

this.minerewardtype = minerewardtype;

}

public int getMinerewardtype() {

return this.minerewardtype;

}


public void setRewartype(int rewartype) {

this.rewartype = rewartype;

}

public int getRewartype() {

return this.rewartype;

}


public void setRewardid(int rewardid) {

this.rewardid = rewardid;

}

public int getRewardid() {

return this.rewardid;

}


public void setDailyrrewarlimit(int dailyrrewarlimit) {

this.dailyrrewarlimit = dailyrrewarlimit;

}

public int getDailyrrewarlimit() {

return this.dailyrrewarlimit;

}




}

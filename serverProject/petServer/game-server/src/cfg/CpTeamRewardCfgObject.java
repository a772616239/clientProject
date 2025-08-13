package cfg;
import model.base.baseConfigObject;
public class  CpTeamRewardCfgObject implements baseConfigObject{



private int id;

private int needscore;

private int[][] rewards;

private int scenceid;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setNeedscore(int needscore) {

this.needscore = needscore;

}

public int getNeedscore() {

return this.needscore;

}


public void setRewards(int[][] rewards) {

this.rewards = rewards;

}

public int[][] getRewards() {

return this.rewards;

}


public void setScenceid(int scenceid) {

this.scenceid = scenceid;

}

public int getScenceid() {

return this.scenceid;

}




}

package cfg;
import model.base.baseConfigObject;
public class  StoneRiftAchievementObject implements baseConfigObject{



private int id;

private int missionid;

private int[][] reward;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setMissionid(int missionid) {

this.missionid = missionid;

}

public int getMissionid() {

return this.missionid;

}


public void setReward(int[][] reward) {

this.reward = reward;

}

public int[][] getReward() {

return this.reward;

}




}

package cfg;
import model.base.baseConfigObject;
public class  KeyNodeConfigObject implements baseConfigObject{



private int id;

private int mainlinenodeid;

private int[][] reward;

private int[] missionids;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setMainlinenodeid(int mainlinenodeid) {

this.mainlinenodeid = mainlinenodeid;

}

public int getMainlinenodeid() {

return this.mainlinenodeid;

}


public void setReward(int[][] reward) {

this.reward = reward;

}

public int[][] getReward() {

return this.reward;

}


public void setMissionids(int[] missionids) {

this.missionids = missionids;

}

public int[] getMissionids() {

return this.missionids;

}




}

package cfg;
import model.base.baseConfigObject;
public class  CrossArenaTaskAwardObject implements baseConfigObject{



private int id;

private int missionid;

private int sceneid;

private int plan;

private int[][] award;




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


public void setSceneid(int sceneid) {

this.sceneid = sceneid;

}

public int getSceneid() {

return this.sceneid;

}


public void setPlan(int plan) {

this.plan = plan;

}

public int getPlan() {

return this.plan;

}


public void setAward(int[][] award) {

this.award = award;

}

public int[][] getAward() {

return this.award;

}




}

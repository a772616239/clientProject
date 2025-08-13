package cfg;
import model.base.baseConfigObject;
public class  MistSeasonMissionObject implements baseConfigObject{



private int id;

private int missiontype;

private int addtion;

private int targetcount;

private int[][] finishreward;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setMissiontype(int missiontype) {

this.missiontype = missiontype;

}

public int getMissiontype() {

return this.missiontype;

}


public void setAddtion(int addtion) {

this.addtion = addtion;

}

public int getAddtion() {

return this.addtion;

}


public void setTargetcount(int targetcount) {

this.targetcount = targetcount;

}

public int getTargetcount() {

return this.targetcount;

}


public void setFinishreward(int[][] finishreward) {

this.finishreward = finishreward;

}

public int[][] getFinishreward() {

return this.finishreward;

}




}

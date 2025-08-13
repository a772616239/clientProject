package cfg;
import model.base.baseConfigObject;
public class  NoviceTaskObject implements baseConfigObject{



private int id;

private int openday;

private int closeday;

private int enddisplay;

private int missiontype;

private int targetcount;

private int addtion;

private int[][] finishreward;

private int pointreward;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setOpenday(int openday) {

this.openday = openday;

}

public int getOpenday() {

return this.openday;

}


public void setCloseday(int closeday) {

this.closeday = closeday;

}

public int getCloseday() {

return this.closeday;

}


public void setEnddisplay(int enddisplay) {

this.enddisplay = enddisplay;

}

public int getEnddisplay() {

return this.enddisplay;

}


public void setMissiontype(int missiontype) {

this.missiontype = missiontype;

}

public int getMissiontype() {

return this.missiontype;

}


public void setTargetcount(int targetcount) {

this.targetcount = targetcount;

}

public int getTargetcount() {

return this.targetcount;

}


public void setAddtion(int addtion) {

this.addtion = addtion;

}

public int getAddtion() {

return this.addtion;

}


public void setFinishreward(int[][] finishreward) {

this.finishreward = finishreward;

}

public int[][] getFinishreward() {

return this.finishreward;

}


public void setPointreward(int pointreward) {

this.pointreward = pointreward;

}

public int getPointreward() {

return this.pointreward;

}




}

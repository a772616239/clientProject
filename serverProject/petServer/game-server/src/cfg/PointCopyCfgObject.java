package cfg;
import model.base.baseConfigObject;
public class  PointCopyCfgObject implements baseConfigObject{



private int id;

private int missiontype;

private int fightmakeid;

private int consume;

private int pointtarget;

private int[][] reward;

private int winunlock;




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


public void setFightmakeid(int fightmakeid) {

this.fightmakeid = fightmakeid;

}

public int getFightmakeid() {

return this.fightmakeid;

}


public void setConsume(int consume) {

this.consume = consume;

}

public int getConsume() {

return this.consume;

}


public void setPointtarget(int pointtarget) {

this.pointtarget = pointtarget;

}

public int getPointtarget() {

return this.pointtarget;

}


public void setReward(int[][] reward) {

this.reward = reward;

}

public int[][] getReward() {

return this.reward;

}


public void setWinunlock(int winunlock) {

this.winunlock = winunlock;

}

public int getWinunlock() {

return this.winunlock;

}




}

package cfg;
import model.base.baseConfigObject;
public class  PetMissionObject implements baseConfigObject{



private int missionlvl;

private int[] petrarity;

private int[] pettype;

private int require;

private int time;

private int fragemntnum;

private int limitmissionrarity;




public void setMissionlvl(int missionlvl) {

this.missionlvl = missionlvl;

}

public int getMissionlvl() {

return this.missionlvl;

}


public void setPetrarity(int[] petrarity) {

this.petrarity = petrarity;

}

public int[] getPetrarity() {

return this.petrarity;

}


public void setPettype(int[] pettype) {

this.pettype = pettype;

}

public int[] getPettype() {

return this.pettype;

}


public void setRequire(int require) {

this.require = require;

}

public int getRequire() {

return this.require;

}


public void setTime(int time) {

this.time = time;

}

public int getTime() {

return this.time;

}


public void setFragemntnum(int fragemntnum) {

this.fragemntnum = fragemntnum;

}

public int getFragemntnum() {

return this.fragemntnum;

}


public void setLimitmissionrarity(int limitmissionrarity) {

this.limitmissionrarity = limitmissionrarity;

}

public int getLimitmissionrarity() {

return this.limitmissionrarity;

}




}

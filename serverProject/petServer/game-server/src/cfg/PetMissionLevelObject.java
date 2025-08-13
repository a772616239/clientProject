package cfg;
import model.base.baseConfigObject;
public class  PetMissionLevelObject implements baseConfigObject{



private int missionlv;

private int[][] uptarget;

private int[][] missinratio;

private int[][] missiontypeweight;




public void setMissionlv(int missionlv) {

this.missionlv = missionlv;

}

public int getMissionlv() {

return this.missionlv;

}


public void setUptarget(int[][] uptarget) {

this.uptarget = uptarget;

}

public int[][] getUptarget() {

return this.uptarget;

}


public void setMissinratio(int[][] missinratio) {

this.missinratio = missinratio;

}

public int[][] getMissinratio() {

return this.missinratio;

}


public void setMissiontypeweight(int[][] missiontypeweight) {

this.missiontypeweight = missiontypeweight;

}

public int[][] getMissiontypeweight() {

return this.missiontypeweight;

}




}

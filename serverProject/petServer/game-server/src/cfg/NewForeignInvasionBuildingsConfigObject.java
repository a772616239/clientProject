package cfg;
import model.base.baseConfigObject;
public class  NewForeignInvasionBuildingsConfigObject implements baseConfigObject{



private int buildingid;

private int defaultmonsterwave;

private int[][] freerewards;

private int fightmake;

private int wavechangesneedcount;

private int riseratio;

private int lowerratio;

private int wavelowerlimit;

private int freemailtemplate;

private int buildingname;




public void setBuildingid(int buildingid) {

this.buildingid = buildingid;

}

public int getBuildingid() {

return this.buildingid;

}


public void setDefaultmonsterwave(int defaultmonsterwave) {

this.defaultmonsterwave = defaultmonsterwave;

}

public int getDefaultmonsterwave() {

return this.defaultmonsterwave;

}


public void setFreerewards(int[][] freerewards) {

this.freerewards = freerewards;

}

public int[][] getFreerewards() {

return this.freerewards;

}


public void setFightmake(int fightmake) {

this.fightmake = fightmake;

}

public int getFightmake() {

return this.fightmake;

}


public void setWavechangesneedcount(int wavechangesneedcount) {

this.wavechangesneedcount = wavechangesneedcount;

}

public int getWavechangesneedcount() {

return this.wavechangesneedcount;

}


public void setRiseratio(int riseratio) {

this.riseratio = riseratio;

}

public int getRiseratio() {

return this.riseratio;

}


public void setLowerratio(int lowerratio) {

this.lowerratio = lowerratio;

}

public int getLowerratio() {

return this.lowerratio;

}


public void setWavelowerlimit(int wavelowerlimit) {

this.wavelowerlimit = wavelowerlimit;

}

public int getWavelowerlimit() {

return this.wavelowerlimit;

}


public void setFreemailtemplate(int freemailtemplate) {

this.freemailtemplate = freemailtemplate;

}

public int getFreemailtemplate() {

return this.freemailtemplate;

}


public void setBuildingname(int buildingname) {

this.buildingname = buildingname;

}

public int getBuildingname() {

return this.buildingname;

}




}

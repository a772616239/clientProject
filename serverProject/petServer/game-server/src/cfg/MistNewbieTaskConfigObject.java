package cfg;
import model.base.baseConfigObject;
public class  MistNewbieTaskConfigObject implements baseConfigObject{



private int id;

private int missionid;

private int unittype;

private int[] unitpos;

private int[][] unitprop;




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


public void setUnittype(int unittype) {

this.unittype = unittype;

}

public int getUnittype() {

return this.unittype;

}


public void setUnitpos(int[] unitpos) {

this.unitpos = unitpos;

}

public int[] getUnitpos() {

return this.unitpos;

}


public void setUnitprop(int[][] unitprop) {

this.unitprop = unitprop;

}

public int[][] getUnitprop() {

return this.unitprop;

}




}

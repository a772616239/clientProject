package cfg;
import model.base.baseConfigObject;
public class  MistTimeLimitMissionObject implements baseConfigObject{



private int id;

private int[] missionlist;

private int[] openlevel;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setMissionlist(int[] missionlist) {

this.missionlist = missionlist;

}

public int[] getMissionlist() {

return this.missionlist;

}


public void setOpenlevel(int[] openlevel) {

this.openlevel = openlevel;

}

public int[] getOpenlevel() {

return this.openlevel;

}




}

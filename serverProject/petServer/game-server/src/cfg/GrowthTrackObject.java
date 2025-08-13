package cfg;
import model.base.baseConfigObject;
public class  GrowthTrackObject implements baseConfigObject{



private int id;

private int[] missionlist;

private boolean defaultunlock;

private int[] nextmissiongroup;




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


public void setDefaultunlock(boolean defaultunlock) {

this.defaultunlock = defaultunlock;

}

public boolean getDefaultunlock() {

return this.defaultunlock;

}


public void setNextmissiongroup(int[] nextmissiongroup) {

this.nextmissiongroup = nextmissiongroup;

}

public int[] getNextmissiongroup() {

return this.nextmissiongroup;

}




}

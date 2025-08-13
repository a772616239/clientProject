package cfg;

import model.base.baseConfigObject;
public class  TeamsConfigObject implements baseConfigObject{



private int teamid;

private int unlockneedlv;

private int unlockneeddiamond;




public void setTeamid(int teamid) {

this.teamid = teamid;

}

public int getTeamid() {

return this.teamid;

}


public void setUnlockneedlv(int unlockneedlv) {

this.unlockneedlv = unlockneedlv;

}

public int getUnlockneedlv() {

return this.unlockneedlv;

}


public void setUnlockneeddiamond(int unlockneeddiamond) {

this.unlockneeddiamond = unlockneeddiamond;

}

public int getUnlockneeddiamond() {

return this.unlockneeddiamond;

}




}

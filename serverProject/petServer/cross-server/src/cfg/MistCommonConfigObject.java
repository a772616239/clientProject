package cfg;
import model.base.baseConfigObject;
public class  MistCommonConfigObject implements baseConfigObject{



private int mistlevel;

private int[] entrancelevelsection;

private int reviselevel;

private int[][] bossactivityrankreward;

private int[][] activtiybosscfgid;

private int[] hiddenevilgroup;

private int[] alchemylist;

private int directsettlefightparam1;

private int directsettlefightparam2;




public void setMistlevel(int mistlevel) {

this.mistlevel = mistlevel;

}

public int getMistlevel() {

return this.mistlevel;

}


public void setEntrancelevelsection(int[] entrancelevelsection) {

this.entrancelevelsection = entrancelevelsection;

}

public int[] getEntrancelevelsection() {

return this.entrancelevelsection;

}


public void setReviselevel(int reviselevel) {

this.reviselevel = reviselevel;

}

public int getReviselevel() {

return this.reviselevel;

}


public void setBossactivityrankreward(int[][] bossactivityrankreward) {

this.bossactivityrankreward = bossactivityrankreward;

}

public int[][] getBossactivityrankreward() {

return this.bossactivityrankreward;

}


public void setActivtiybosscfgid(int[][] activtiybosscfgid) {

this.activtiybosscfgid = activtiybosscfgid;

}

public int[][] getActivtiybosscfgid() {

return this.activtiybosscfgid;

}


public void setHiddenevilgroup(int[] hiddenevilgroup) {

this.hiddenevilgroup = hiddenevilgroup;

}

public int[] getHiddenevilgroup() {

return this.hiddenevilgroup;

}


public void setAlchemylist(int[] alchemylist) {

this.alchemylist = alchemylist;

}

public int[] getAlchemylist() {

return this.alchemylist;

}


public void setDirectsettlefightparam1(int directsettlefightparam1) {

this.directsettlefightparam1 = directsettlefightparam1;

}

public int getDirectsettlefightparam1() {

return this.directsettlefightparam1;

}


public void setDirectsettlefightparam2(int directsettlefightparam2) {

this.directsettlefightparam2 = directsettlefightparam2;

}

public int getDirectsettlefightparam2() {

return this.directsettlefightparam2;

}




}

package cfg;
import model.base.baseConfigObject;
public class  PetRuneSuitPropertiesObject implements baseConfigObject{



private int suitid;

private int suitname;

private int suitrarity;

private int[][] suitproperties;

private int[][] buffid;

private int[][] fightadd;




public void setSuitid(int suitid) {

this.suitid = suitid;

}

public int getSuitid() {

return this.suitid;

}


public void setSuitname(int suitname) {

this.suitname = suitname;

}

public int getSuitname() {

return this.suitname;

}


public void setSuitrarity(int suitrarity) {

this.suitrarity = suitrarity;

}

public int getSuitrarity() {

return this.suitrarity;

}


public void setSuitproperties(int[][] suitproperties) {

this.suitproperties = suitproperties;

}

public int[][] getSuitproperties() {

return this.suitproperties;

}


public void setBuffid(int[][] buffid) {

this.buffid = buffid;

}

public int[][] getBuffid() {

return this.buffid;

}


public void setFightadd(int[][] fightadd) {

this.fightadd = fightadd;

}

public int[][] getFightadd() {

return this.fightadd;

}




}

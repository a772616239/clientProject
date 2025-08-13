package cfg;
import model.base.baseConfigObject;

public class  TheWarMapConfigObject implements baseConfigObject{



private String mapname;

private String mapfilename;

private int maxplayercount;

private int campnum;

private int petverifylevel;

private int initstamina;

private int recoverstamina;

private int recoverstaminainterval;

private int maxrecoverstamina;

private int maxenergy;




public void setMapname(String mapname) {

this.mapname = mapname;

}

public String getMapname() {

return this.mapname;

}


public void setMapfilename(String mapfilename) {

this.mapfilename = mapfilename;

}

public String getMapfilename() {

return this.mapfilename;

}


public void setMaxplayercount(int maxplayercount) {

this.maxplayercount = maxplayercount;

}

public int getMaxplayercount() {

return this.maxplayercount;

}


public void setCampnum(int campnum) {

this.campnum = campnum;

}

public int getCampnum() {

return this.campnum;

}


public void setPetverifylevel(int petverifylevel) {

this.petverifylevel = petverifylevel;

}

public int getPetverifylevel() {

return this.petverifylevel;

}


public void setInitstamina(int initstamina) {

this.initstamina = initstamina;

}

public int getInitstamina() {

return this.initstamina;

}


public void setRecoverstamina(int recoverstamina) {

this.recoverstamina = recoverstamina;

}

public int getRecoverstamina() {

return this.recoverstamina;

}


public void setRecoverstaminainterval(int recoverstaminainterval) {

this.recoverstaminainterval = recoverstaminainterval;

}

public int getRecoverstaminainterval() {

return this.recoverstaminainterval;

}


public void setMaxrecoverstamina(int maxrecoverstamina) {

this.maxrecoverstamina = maxrecoverstamina;

}

public int getMaxrecoverstamina() {

return this.maxrecoverstamina;

}


public void setMaxenergy(int maxenergy) {

this.maxenergy = maxenergy;

}

public int getMaxenergy() {

return this.maxenergy;

}




}

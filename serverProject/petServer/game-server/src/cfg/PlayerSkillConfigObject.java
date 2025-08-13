package cfg;
import model.base.baseConfigObject;
public class  PlayerSkillConfigObject implements baseConfigObject{



private String key;

private int uptype;

private int playerlv;

private int skillid;

private int level;

private int[] upconsume;

private int[][] increaseproperty;

private int[][] extraproperty;

private int[][] cumuproperty;




public void setKey(String key) {

this.key = key;

}

public String getKey() {

return this.key;

}


public void setUptype(int uptype) {

this.uptype = uptype;

}

public int getUptype() {

return this.uptype;

}


public void setPlayerlv(int playerlv) {

this.playerlv = playerlv;

}

public int getPlayerlv() {

return this.playerlv;

}


public void setSkillid(int skillid) {

this.skillid = skillid;

}

public int getSkillid() {

return this.skillid;

}


public void setLevel(int level) {

this.level = level;

}

public int getLevel() {

return this.level;

}


public void setUpconsume(int[] upconsume) {

this.upconsume = upconsume;

}

public int[] getUpconsume() {

return this.upconsume;

}


public void setIncreaseproperty(int[][] increaseproperty) {

this.increaseproperty = increaseproperty;

}

public int[][] getIncreaseproperty() {

return this.increaseproperty;

}


public void setExtraproperty(int[][] extraproperty) {

this.extraproperty = extraproperty;

}

public int[][] getExtraproperty() {

return this.extraproperty;

}


public void setCumuproperty(int[][] cumuproperty) {

this.cumuproperty = cumuproperty;

}

public int[][] getCumuproperty() {

return this.cumuproperty;

}




}

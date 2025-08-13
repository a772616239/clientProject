package cfg;
import model.base.baseConfigObject;
public class  ArtifactEnhancePointConfigObject implements baseConfigObject{



private int key;

private int id;

private int level;

private int[] upconsume;

private int[][] increaseproperty;

private int needplayerlv;

private int[][] cumuproperty;




public void setKey(int key) {

this.key = key;

}

public int getKey() {

return this.key;

}


public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

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


public void setNeedplayerlv(int needplayerlv) {

this.needplayerlv = needplayerlv;

}

public int getNeedplayerlv() {

return this.needplayerlv;

}


public void setCumuproperty(int[][] cumuproperty) {

this.cumuproperty = cumuproperty;

}

public int[][] getCumuproperty() {

return this.cumuproperty;

}




}

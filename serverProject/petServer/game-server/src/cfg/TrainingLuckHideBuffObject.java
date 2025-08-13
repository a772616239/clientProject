package cfg;
import model.base.baseConfigObject;
public class  TrainingLuckHideBuffObject implements baseConfigObject{



private int id;

private int mapid;

private int type;

private int[] configformat;

private int buff;

private int weight;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setMapid(int mapid) {

this.mapid = mapid;

}

public int getMapid() {

return this.mapid;

}


public void setType(int type) {

this.type = type;

}

public int getType() {

return this.type;

}


public void setConfigformat(int[] configformat) {

this.configformat = configformat;

}

public int[] getConfigformat() {

return this.configformat;

}


public void setBuff(int buff) {

this.buff = buff;

}

public int getBuff() {

return this.buff;

}


public void setWeight(int weight) {

this.weight = weight;

}

public int getWeight() {

return this.weight;

}




}

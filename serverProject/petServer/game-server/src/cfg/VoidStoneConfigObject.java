package cfg;
import model.base.baseConfigObject;
public class  VoidStoneConfigObject implements baseConfigObject{



private int id;

private int rarity;

private int lv;

private int resourcelv;

private int needpetlv;

private int probability;

private int propertytype;

private int[][] properties;

private int[][] upconsume;

private int[][] changeconsume;

private int[][] lockconsume;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setRarity(int rarity) {

this.rarity = rarity;

}

public int getRarity() {

return this.rarity;

}


public void setLv(int lv) {

this.lv = lv;

}

public int getLv() {

return this.lv;

}


public void setResourcelv(int resourcelv) {

this.resourcelv = resourcelv;

}

public int getResourcelv() {

return this.resourcelv;

}


public void setNeedpetlv(int needpetlv) {

this.needpetlv = needpetlv;

}

public int getNeedpetlv() {

return this.needpetlv;

}


public void setProbability(int probability) {

this.probability = probability;

}

public int getProbability() {

return this.probability;

}


public void setPropertytype(int propertytype) {

this.propertytype = propertytype;

}

public int getPropertytype() {

return this.propertytype;

}


public void setProperties(int[][] properties) {

this.properties = properties;

}

public int[][] getProperties() {

return this.properties;

}


public void setUpconsume(int[][] upconsume) {

this.upconsume = upconsume;

}

public int[][] getUpconsume() {

return this.upconsume;

}


public void setChangeconsume(int[][] changeconsume) {

this.changeconsume = changeconsume;

}

public int[][] getChangeconsume() {

return this.changeconsume;

}


public void setLockconsume(int[][] lockconsume) {

this.lockconsume = lockconsume;

}

public int[][] getLockconsume() {

return this.lockconsume;

}




}

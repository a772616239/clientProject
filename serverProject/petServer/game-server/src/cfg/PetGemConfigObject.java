package cfg;
import model.base.baseConfigObject;
public class  PetGemConfigObject implements baseConfigObject{



private int id;

private int rarity;

private int gemtype;

private int lv;

private int star;

private int maxlv;

private int advanceneedplayerlv;

private int uplvneedrarity;

private int uplvneedstar;

private int[][] baseproperties;




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


public void setGemtype(int gemtype) {

this.gemtype = gemtype;

}

public int getGemtype() {

return this.gemtype;

}


public void setLv(int lv) {

this.lv = lv;

}

public int getLv() {

return this.lv;

}


public void setStar(int star) {

this.star = star;

}

public int getStar() {

return this.star;

}


public void setMaxlv(int maxlv) {

this.maxlv = maxlv;

}

public int getMaxlv() {

return this.maxlv;

}


public void setAdvanceneedplayerlv(int advanceneedplayerlv) {

this.advanceneedplayerlv = advanceneedplayerlv;

}

public int getAdvanceneedplayerlv() {

return this.advanceneedplayerlv;

}


public void setUplvneedrarity(int uplvneedrarity) {

this.uplvneedrarity = uplvneedrarity;

}

public int getUplvneedrarity() {

return this.uplvneedrarity;

}


public void setUplvneedstar(int uplvneedstar) {

this.uplvneedstar = uplvneedstar;

}

public int getUplvneedstar() {

return this.uplvneedstar;

}


public void setBaseproperties(int[][] baseproperties) {

this.baseproperties = baseproperties;

}

public int[][] getBaseproperties() {

return this.baseproperties;

}




}

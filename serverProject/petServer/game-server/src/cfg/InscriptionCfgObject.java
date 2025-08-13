package cfg;
import model.base.baseConfigObject;
public class  InscriptionCfgObject implements baseConfigObject{



private int id;

private int rarity;

private int type;

private int[][] property;

private int[] buffdata;

private int[][] equipconsume;

private int probability;




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


public void setType(int type) {

this.type = type;

}

public int getType() {

return this.type;

}


public void setProperty(int[][] property) {

this.property = property;

}

public int[][] getProperty() {

return this.property;

}


public void setBuffdata(int[] buffdata) {

this.buffdata = buffdata;

}

public int[] getBuffdata() {

return this.buffdata;

}


public void setEquipconsume(int[][] equipconsume) {

this.equipconsume = equipconsume;

}

public int[][] getEquipconsume() {

return this.equipconsume;

}


public void setProbability(int probability) {

this.probability = probability;

}

public int getProbability() {

return this.probability;

}




}

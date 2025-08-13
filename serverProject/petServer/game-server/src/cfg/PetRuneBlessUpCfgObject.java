package cfg;
import model.base.baseConfigObject;
public class  PetRuneBlessUpCfgObject implements baseConfigObject{



private int id;

private int runerarity;

private int itemrating;

private int[][] offerproperty;

private int probability;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setRunerarity(int runerarity) {

this.runerarity = runerarity;

}

public int getRunerarity() {

return this.runerarity;

}


public void setItemrating(int itemrating) {

this.itemrating = itemrating;

}

public int getItemrating() {

return this.itemrating;

}


public void setOfferproperty(int[][] offerproperty) {

this.offerproperty = offerproperty;

}

public int[][] getOfferproperty() {

return this.offerproperty;

}


public void setProbability(int probability) {

this.probability = probability;

}

public int getProbability() {

return this.probability;

}




}

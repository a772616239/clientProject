package cfg;
import model.base.baseConfigObject;
public class  PetRarityConfigObject implements baseConfigObject{



private int key;

private int rarity;

private int propertymodel;

private int petfactor;

private int[][] petfixproperties;

private int maxlvl;




public void setKey(int key) {

this.key = key;

}

public int getKey() {

return this.key;

}


public void setRarity(int rarity) {

this.rarity = rarity;

}

public int getRarity() {

return this.rarity;

}


public void setPropertymodel(int propertymodel) {

this.propertymodel = propertymodel;

}

public int getPropertymodel() {

return this.propertymodel;

}


public void setPetfactor(int petfactor) {

this.petfactor = petfactor;

}

public int getPetfactor() {

return this.petfactor;

}


public void setPetfixproperties(int[][] petfixproperties) {

this.petfixproperties = petfixproperties;

}

public int[][] getPetfixproperties() {

return this.petfixproperties;

}


public void setMaxlvl(int maxlvl) {

this.maxlvl = maxlvl;

}

public int getMaxlvl() {

return this.maxlvl;

}




}

package cfg;
import model.base.baseConfigObject;
public class  PetBackConfigObject implements baseConfigObject{



private int id;

private int rarity;

private int petclass;

private int[][] resources;

private int petcount;




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


public void setPetclass(int petclass) {

this.petclass = petclass;

}

public int getPetclass() {

return this.petclass;

}


public void setResources(int[][] resources) {

this.resources = resources;

}

public int[][] getResources() {

return this.resources;

}


public void setPetcount(int petcount) {

this.petcount = petcount;

}

public int getPetcount() {

return this.petcount;

}




}

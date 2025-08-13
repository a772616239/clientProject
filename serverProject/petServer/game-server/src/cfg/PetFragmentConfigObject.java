package cfg;
import model.base.baseConfigObject;
public class  PetFragmentConfigObject implements baseConfigObject{



private int id;

private int debrisrarity;

private int amount;

private int debristype;

private boolean petcore;

private int petid;

private int probability;

private int probabilitybyclass;

private String name;

private int namelanguage;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setDebrisrarity(int debrisrarity) {

this.debrisrarity = debrisrarity;

}

public int getDebrisrarity() {

return this.debrisrarity;

}


public void setAmount(int amount) {

this.amount = amount;

}

public int getAmount() {

return this.amount;

}


public void setDebristype(int debristype) {

this.debristype = debristype;

}

public int getDebristype() {

return this.debristype;

}


public void setPetcore(boolean petcore) {

this.petcore = petcore;

}

public boolean getPetcore() {

return this.petcore;

}


public void setPetid(int petid) {

this.petid = petid;

}

public int getPetid() {

return this.petid;

}


public void setProbability(int probability) {

this.probability = probability;

}

public int getProbability() {

return this.probability;

}


public void setProbabilitybyclass(int probabilitybyclass) {

this.probabilitybyclass = probabilitybyclass;

}

public int getProbabilitybyclass() {

return this.probabilitybyclass;

}


public void setName(String name) {

this.name = name;

}

public String getName() {

return this.name;

}


public void setNamelanguage(int namelanguage) {

this.namelanguage = namelanguage;

}

public int getNamelanguage() {

return this.namelanguage;

}




}

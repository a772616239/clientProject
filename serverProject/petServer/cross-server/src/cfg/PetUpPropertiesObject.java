package cfg;

import model.base.baseConfigObject;

public class  PetUpPropertiesObject implements baseConfigObject{



private int id;

private String type;

private int lvl;

private int petbookid;

private int[][] property;

private int ability;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setType(String type) {

this.type = type;

}

public String getType() {

return this.type;

}


public void setLvl(int lvl) {

this.lvl = lvl;

}

public int getLvl() {

return this.lvl;

}


public void setPetbookid(int petbookid) {

this.petbookid = petbookid;

}

public int getPetbookid() {

return this.petbookid;

}


public void setProperty(int[][] property) {

this.property = property;

}

public int[][] getProperty() {

return this.property;

}


public void setAbility(int ability) {

this.ability = ability;

}

public int getAbility() {

return this.ability;

}




}

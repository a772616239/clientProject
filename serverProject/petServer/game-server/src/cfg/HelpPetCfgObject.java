package cfg;
import model.base.baseConfigObject;
public class  HelpPetCfgObject implements baseConfigObject{



private String id;

private int petcfgid;

private int rarity;

private int level;

private int mainlinenode;




public void setId(String id) {

this.id = id;

}

public String getId() {

return this.id;

}


public void setPetcfgid(int petcfgid) {

this.petcfgid = petcfgid;

}

public int getPetcfgid() {

return this.petcfgid;

}


public void setRarity(int rarity) {

this.rarity = rarity;

}

public int getRarity() {

return this.rarity;

}


public void setLevel(int level) {

this.level = level;

}

public int getLevel() {

return this.level;

}


public void setMainlinenode(int mainlinenode) {

this.mainlinenode = mainlinenode;

}

public int getMainlinenode() {

return this.mainlinenode;

}




}

package cfg;
import model.base.baseConfigObject;
public class  AdvancedSoulConfigObject implements baseConfigObject{



private int itemid;

private int rarity;

private int petclass;




public void setItemid(int itemid) {

this.itemid = itemid;

}

public int getItemid() {

return this.itemid;

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




}

package cfg;
import model.base.baseConfigObject;
public class  ItemPetAwakeExpConfigObject implements baseConfigObject{



private int itemid;

private int propertytype;

private int[] property;

private int exp;




public void setItemid(int itemid) {

this.itemid = itemid;

}

public int getItemid() {

return this.itemid;

}


public void setPropertytype(int propertytype) {

this.propertytype = propertytype;

}

public int getPropertytype() {

return this.propertytype;

}


public void setProperty(int[] property) {

this.property = property;

}

public int[] getProperty() {

return this.property;

}


public void setExp(int exp) {

this.exp = exp;

}

public int getExp() {

return this.exp;

}




}

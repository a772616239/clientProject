package cfg;
import model.base.baseConfigObject;
public class  PetAwakenConfigObject implements baseConfigObject{



private int id;

private int awaketype;

private int orientation;

private int uplvl;

private int petlvl;

private int[][] properties;

private int needexp;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setAwaketype(int awaketype) {

this.awaketype = awaketype;

}

public int getAwaketype() {

return this.awaketype;

}


public void setOrientation(int orientation) {

this.orientation = orientation;

}

public int getOrientation() {

return this.orientation;

}


public void setUplvl(int uplvl) {

this.uplvl = uplvl;

}

public int getUplvl() {

return this.uplvl;

}


public void setPetlvl(int petlvl) {

this.petlvl = petlvl;

}

public int getPetlvl() {

return this.petlvl;

}


public void setProperties(int[][] properties) {

this.properties = properties;

}

public int[][] getProperties() {

return this.properties;

}


public void setNeedexp(int needexp) {

this.needexp = needexp;

}

public int getNeedexp() {

return this.needexp;

}




}

package cfg;
import model.base.baseConfigObject;
public class  PetRuneBlessRatingCfgObject implements baseConfigObject{



private int id;

private int runerarity;

private int blesslevel;

private int[][] ratingthreshold;

private int offerproperty;




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


public void setBlesslevel(int blesslevel) {

this.blesslevel = blesslevel;

}

public int getBlesslevel() {

return this.blesslevel;

}


public void setRatingthreshold(int[][] ratingthreshold) {

this.ratingthreshold = ratingthreshold;

}

public int[][] getRatingthreshold() {

return this.ratingthreshold;

}


public void setOfferproperty(int offerproperty) {

this.offerproperty = offerproperty;

}

public int getOfferproperty() {

return this.offerproperty;

}




}

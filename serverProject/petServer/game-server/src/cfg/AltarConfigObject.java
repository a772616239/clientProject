package cfg;
import model.base.baseConfigObject;
public class  AltarConfigObject implements baseConfigObject{



private int id;

private int[] price;

private int[][] qualityweight;

private int mustgetdrawtimes;

private int mustgetquality;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setPrice(int[] price) {

this.price = price;

}

public int[] getPrice() {

return this.price;

}


public void setQualityweight(int[][] qualityweight) {

this.qualityweight = qualityweight;

}

public int[][] getQualityweight() {

return this.qualityweight;

}


public void setMustgetdrawtimes(int mustgetdrawtimes) {

this.mustgetdrawtimes = mustgetdrawtimes;

}

public int getMustgetdrawtimes() {

return this.mustgetdrawtimes;

}


public void setMustgetquality(int mustgetquality) {

this.mustgetquality = mustgetquality;

}

public int getMustgetquality() {

return this.mustgetquality;

}




}

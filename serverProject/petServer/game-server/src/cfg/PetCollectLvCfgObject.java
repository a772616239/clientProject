package cfg;
import model.base.baseConfigObject;
public class  PetCollectLvCfgObject implements baseConfigObject{



private int lv;

private int upexp;

private int[][] offerproperty;




public void setLv(int lv) {

this.lv = lv;

}

public int getLv() {

return this.lv;

}


public void setUpexp(int upexp) {

this.upexp = upexp;

}

public int getUpexp() {

return this.upexp;

}


public void setOfferproperty(int[][] offerproperty) {

this.offerproperty = offerproperty;

}

public int[][] getOfferproperty() {

return this.offerproperty;

}




}

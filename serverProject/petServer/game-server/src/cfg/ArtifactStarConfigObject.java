package cfg;
import model.base.baseConfigObject;
public class  ArtifactStarConfigObject implements baseConfigObject{



private int key;

private int[] upconsume;

private int[][] increaseproperty;

private int[][] cumuproperty;




public void setKey(int key) {

this.key = key;

}

public int getKey() {

return this.key;

}


public void setUpconsume(int[] upconsume) {

this.upconsume = upconsume;

}

public int[] getUpconsume() {

return this.upconsume;

}


public void setIncreaseproperty(int[][] increaseproperty) {

this.increaseproperty = increaseproperty;

}

public int[][] getIncreaseproperty() {

return this.increaseproperty;

}


public void setCumuproperty(int[][] cumuproperty) {

this.cumuproperty = cumuproperty;

}

public int[][] getCumuproperty() {

return this.cumuproperty;

}




}

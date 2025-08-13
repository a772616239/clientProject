package cfg;
import model.base.baseConfigObject;
public class  PetRuneRairtyUpCfgObject implements baseConfigObject{



private int id;

private int runekind;

private int rairty;

private int[][] consumes;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setRunekind(int runekind) {

this.runekind = runekind;

}

public int getRunekind() {

return this.runekind;

}


public void setRairty(int rairty) {

this.rairty = rairty;

}

public int getRairty() {

return this.rairty;

}


public void setConsumes(int[][] consumes) {

this.consumes = consumes;

}

public int[][] getConsumes() {

return this.consumes;

}




}

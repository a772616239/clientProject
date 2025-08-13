package cfg;
import model.base.baseConfigObject;
public class  StoneRiftPetObject implements baseConfigObject{



private int pettype;

private int function;

private int[][] ritygain;




public void setPettype(int pettype) {

this.pettype = pettype;

}

public int getPettype() {

return this.pettype;

}


public void setFunction(int function) {

this.function = function;

}

public int getFunction() {

return this.function;

}


public void setRitygain(int[][] ritygain) {

this.ritygain = ritygain;

}

public int[][] getRitygain() {

return this.ritygain;

}




}

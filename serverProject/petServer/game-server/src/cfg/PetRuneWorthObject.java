package cfg;
import model.base.baseConfigObject;
public class  PetRuneWorthObject implements baseConfigObject{



private int id;

private int runerarity;

private int runelvl;

private int[][] runesale;




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


public void setRunelvl(int runelvl) {

this.runelvl = runelvl;

}

public int getRunelvl() {

return this.runelvl;

}


public void setRunesale(int[][] runesale) {

this.runesale = runesale;

}

public int[][] getRunesale() {

return this.runesale;

}




}

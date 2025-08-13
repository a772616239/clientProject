package cfg;
import model.base.baseConfigObject;
public class  PetEvolveCfgObject implements baseConfigObject{



private int id;

private int[][] upconsume;

private int[][] upreward;

private int addability;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setUpconsume(int[][] upconsume) {

this.upconsume = upconsume;

}

public int[][] getUpconsume() {

return this.upconsume;

}


public void setUpreward(int[][] upreward) {

this.upreward = upreward;

}

public int[][] getUpreward() {

return this.upreward;

}


public void setAddability(int addability) {

this.addability = addability;

}

public int getAddability() {

return this.addability;

}




}

package cfg;
import model.base.baseConfigObject;
public class  MistDropObjConfigObject implements baseConfigObject{



private int id;

private int dropobjtype;

private int[][] dropobjprop;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setDropobjtype(int dropobjtype) {

this.dropobjtype = dropobjtype;

}

public int getDropobjtype() {

return this.dropobjtype;

}


public void setDropobjprop(int[][] dropobjprop) {

this.dropobjprop = dropobjprop;

}

public int[][] getDropobjprop() {

return this.dropobjprop;

}




}

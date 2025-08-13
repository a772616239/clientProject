package cfg;
import model.base.baseConfigObject;
public class  MistExplodeConfigObject implements baseConfigObject{



private int id;

private int objtype;

private int[][] initprop;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setObjtype(int objtype) {

this.objtype = objtype;

}

public int getObjtype() {

return this.objtype;

}


public void setInitprop(int[][] initprop) {

this.initprop = initprop;

}

public int[][] getInitprop() {

return this.initprop;

}




}

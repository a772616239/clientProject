package cfg;
import model.base.baseConfigObject;
public class  MistNewObjConfigObject implements baseConfigObject{



private int id;

private int objtype;

private int[] initpos;

private int[][] initprop;

private int[] randprop;

private boolean initrand;

private int[][] randposdata;




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


public void setInitpos(int[] initpos) {

this.initpos = initpos;

}

public int[] getInitpos() {

return this.initpos;

}


public void setInitprop(int[][] initprop) {

this.initprop = initprop;

}

public int[][] getInitprop() {

return this.initprop;

}


public void setRandprop(int[] randprop) {

this.randprop = randprop;

}

public int[] getRandprop() {

return this.randprop;

}


public void setInitrand(boolean initrand) {

this.initrand = initrand;

}

public boolean getInitrand() {

return this.initrand;

}


public void setRandposdata(int[][] randposdata) {

this.randposdata = randposdata;

}

public int[][] getRandposdata() {

return this.randposdata;

}




}

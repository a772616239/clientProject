package cfg;
import model.base.baseConfigObject;
public class  MistScheduleObjConfigObject implements baseConfigObject{



private int id;

private int objtype;

private int refreshinterval;

private int initcount;

private int maxcount;

private int[][] initprop;

private int[] randprop;

private boolean removewhenscheduleend;

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


public void setRefreshinterval(int refreshinterval) {

this.refreshinterval = refreshinterval;

}

public int getRefreshinterval() {

return this.refreshinterval;

}


public void setInitcount(int initcount) {

this.initcount = initcount;

}

public int getInitcount() {

return this.initcount;

}


public void setMaxcount(int maxcount) {

this.maxcount = maxcount;

}

public int getMaxcount() {

return this.maxcount;

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


public void setRemovewhenscheduleend(boolean removewhenscheduleend) {

this.removewhenscheduleend = removewhenscheduleend;

}

public boolean getRemovewhenscheduleend() {

return this.removewhenscheduleend;

}


public void setRandposdata(int[][] randposdata) {

this.randposdata = randposdata;

}

public int[][] getRandposdata() {

return this.randposdata;

}




}

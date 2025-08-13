package cfg;
import model.base.baseConfigObject;
public class  TheWarMonsterRefreshConfigObject implements baseConfigObject{



private int id;

private int refreshinterval;

private int refreshnum;

private int maxrefreshnum;

private int gorlrate;

private int dprate;

private int maxproducttime;

private int fithmakeid;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setRefreshinterval(int refreshinterval) {

this.refreshinterval = refreshinterval;

}

public int getRefreshinterval() {

return this.refreshinterval;

}


public void setRefreshnum(int refreshnum) {

this.refreshnum = refreshnum;

}

public int getRefreshnum() {

return this.refreshnum;

}


public void setMaxrefreshnum(int maxrefreshnum) {

this.maxrefreshnum = maxrefreshnum;

}

public int getMaxrefreshnum() {

return this.maxrefreshnum;

}


public void setGorlrate(int gorlrate) {

this.gorlrate = gorlrate;

}

public int getGorlrate() {

return this.gorlrate;

}


public void setDprate(int dprate) {

this.dprate = dprate;

}

public int getDprate() {

return this.dprate;

}


public void setMaxproducttime(int maxproducttime) {

this.maxproducttime = maxproducttime;

}

public int getMaxproducttime() {

return this.maxproducttime;

}


public void setFithmakeid(int fithmakeid) {

this.fithmakeid = fithmakeid;

}

public int getFithmakeid() {

return this.fithmakeid;

}




}

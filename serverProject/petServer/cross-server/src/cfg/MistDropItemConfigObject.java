package cfg;
import model.base.baseConfigObject;
public class  MistDropItemConfigObject implements baseConfigObject{



private int dropindex;

private int dropnum;

private int pkdropodds;

private int normaldropodds;




public void setDropindex(int dropindex) {

this.dropindex = dropindex;

}

public int getDropindex() {

return this.dropindex;

}


public void setDropnum(int dropnum) {

this.dropnum = dropnum;

}

public int getDropnum() {

return this.dropnum;

}


public void setPkdropodds(int pkdropodds) {

this.pkdropodds = pkdropodds;

}

public int getPkdropodds() {

return this.pkdropodds;

}


public void setNormaldropodds(int normaldropodds) {

this.normaldropodds = normaldropodds;

}

public int getNormaldropodds() {

return this.normaldropodds;

}




}

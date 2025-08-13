package cfg;
import model.base.baseConfigObject;
public class  PatrolMapObject implements baseConfigObject{



private int mapid;

private int main;

private int[] branch;

private int path;




public void setMapid(int mapid) {

this.mapid = mapid;

}

public int getMapid() {

return this.mapid;

}


public void setMain(int main) {

this.main = main;

}

public int getMain() {

return this.main;

}


public void setBranch(int[] branch) {

this.branch = branch;

}

public int[] getBranch() {

return this.branch;

}


public void setPath(int path) {

this.path = path;

}

public int getPath() {

return this.path;

}




}

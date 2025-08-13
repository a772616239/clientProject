package cfg;

import model.base.baseConfigObject;
public class  TeamPositionObject implements baseConfigObject{



private int positionid;

private int unlocklv;




public void setPositionid(int positionid) {

this.positionid = positionid;

}

public int getPositionid() {

return this.positionid;

}


public void setUnlocklv(int unlocklv) {

this.unlocklv = unlocklv;

}

public int getUnlocklv() {

return this.unlocklv;

}




}

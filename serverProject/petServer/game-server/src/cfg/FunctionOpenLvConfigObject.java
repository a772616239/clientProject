package cfg;
import model.base.baseConfigObject;
public class  FunctionOpenLvConfigObject implements baseConfigObject{



private int id;

private int unlocktype;

private int unlockneed;

private int needchapter;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setUnlocktype(int unlocktype) {

this.unlocktype = unlocktype;

}

public int getUnlocktype() {

return this.unlocktype;

}


public void setUnlockneed(int unlockneed) {

this.unlockneed = unlockneed;

}

public int getUnlockneed() {

return this.unlockneed;

}


public void setNeedchapter(int needchapter) {

this.needchapter = needchapter;

}

public int getNeedchapter() {

return this.needchapter;

}




}

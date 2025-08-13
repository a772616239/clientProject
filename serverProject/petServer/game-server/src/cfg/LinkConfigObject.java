package cfg;
import model.base.baseConfigObject;
public class  LinkConfigObject implements baseConfigObject{



private int id;

private int[] needpet;

private int[] bufflist;

private int[] fixfight;

private int[] lvlfightfactor;

private int exp;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setNeedpet(int[] needpet) {

this.needpet = needpet;

}

public int[] getNeedpet() {

return this.needpet;

}


public void setBufflist(int[] bufflist) {

this.bufflist = bufflist;

}

public int[] getBufflist() {

return this.bufflist;

}


public void setFixfight(int[] fixfight) {

this.fixfight = fixfight;

}

public int[] getFixfight() {

return this.fixfight;

}


public void setLvlfightfactor(int[] lvlfightfactor) {

this.lvlfightfactor = lvlfightfactor;

}

public int[] getLvlfightfactor() {

return this.lvlfightfactor;

}


public void setExp(int exp) {

this.exp = exp;

}

public int getExp() {

return this.exp;

}




}

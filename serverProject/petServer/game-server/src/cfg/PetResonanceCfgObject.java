package cfg;
import model.base.baseConfigObject;
public class  PetResonanceCfgObject implements baseConfigObject{



private int id;

private int level;

private int groupid;

private int[][] needpet;

private int[] bufflist;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setLevel(int level) {

this.level = level;

}

public int getLevel() {

return this.level;

}


public void setGroupid(int groupid) {

this.groupid = groupid;

}

public int getGroupid() {

return this.groupid;

}


public void setNeedpet(int[][] needpet) {

this.needpet = needpet;

}

public int[][] getNeedpet() {

return this.needpet;

}


public void setBufflist(int[] bufflist) {

this.bufflist = bufflist;

}

public int[] getBufflist() {

return this.bufflist;

}




}

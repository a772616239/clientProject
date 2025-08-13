package cfg;
import model.base.baseConfigObject;
public class  MainLineCheckPointObject implements baseConfigObject{



private int id;

private int type;

private int subtype;

private int unlocklv;

private int mainlinemissionid;

private boolean isthelastpoint;

private int beforecheckpoint;

private int aftercheckpoint;

private int[] nodelist;

private int[] correctorder;

private int unlockmistlv;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setType(int type) {

this.type = type;

}

public int getType() {

return this.type;

}


public void setSubtype(int subtype) {

this.subtype = subtype;

}

public int getSubtype() {

return this.subtype;

}


public void setUnlocklv(int unlocklv) {

this.unlocklv = unlocklv;

}

public int getUnlocklv() {

return this.unlocklv;

}


public void setMainlinemissionid(int mainlinemissionid) {

this.mainlinemissionid = mainlinemissionid;

}

public int getMainlinemissionid() {

return this.mainlinemissionid;

}


public void setIsthelastpoint(boolean isthelastpoint) {

this.isthelastpoint = isthelastpoint;

}

public boolean getIsthelastpoint() {

return this.isthelastpoint;

}


public void setBeforecheckpoint(int beforecheckpoint) {

this.beforecheckpoint = beforecheckpoint;

}

public int getBeforecheckpoint() {

return this.beforecheckpoint;

}


public void setAftercheckpoint(int aftercheckpoint) {

this.aftercheckpoint = aftercheckpoint;

}

public int getAftercheckpoint() {

return this.aftercheckpoint;

}


public void setNodelist(int[] nodelist) {

this.nodelist = nodelist;

}

public int[] getNodelist() {

return this.nodelist;

}


public void setCorrectorder(int[] correctorder) {

this.correctorder = correctorder;

}

public int[] getCorrectorder() {

return this.correctorder;

}


public void setUnlockmistlv(int unlockmistlv) {

this.unlockmistlv = unlockmistlv;

}

public int getUnlockmistlv() {

return this.unlockmistlv;

}




}

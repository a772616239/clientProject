package cfg;
import model.base.baseConfigObject;
public class  ResourceCopyObject implements baseConfigObject{



private int id;

private int type;

private int typepassindex;

private int unlocklv;

private int fightmakeid;

private int afterid;




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


public void setTypepassindex(int typepassindex) {

this.typepassindex = typepassindex;

}

public int getTypepassindex() {

return this.typepassindex;

}


public void setUnlocklv(int unlocklv) {

this.unlocklv = unlocklv;

}

public int getUnlocklv() {

return this.unlocklv;

}


public void setFightmakeid(int fightmakeid) {

this.fightmakeid = fightmakeid;

}

public int getFightmakeid() {

return this.fightmakeid;

}


public void setAfterid(int afterid) {

this.afterid = afterid;

}

public int getAfterid() {

return this.afterid;

}




}

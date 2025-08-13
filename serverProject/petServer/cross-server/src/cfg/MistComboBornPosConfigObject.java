package cfg;
import model.base.baseConfigObject;
public class  MistComboBornPosConfigObject implements baseConfigObject{



private int id;

private int level;

private int objtype;

private int[] masterobjpos;

private int[][] slaveobjposlist;

private int[][] playerrebornposlist;




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


public void setObjtype(int objtype) {

this.objtype = objtype;

}

public int getObjtype() {

return this.objtype;

}


public void setMasterobjpos(int[] masterobjpos) {

this.masterobjpos = masterobjpos;

}

public int[] getMasterobjpos() {

return this.masterobjpos;

}


public void setSlaveobjposlist(int[][] slaveobjposlist) {

this.slaveobjposlist = slaveobjposlist;

}

public int[][] getSlaveobjposlist() {

return this.slaveobjposlist;

}


public void setPlayerrebornposlist(int[][] playerrebornposlist) {

this.playerrebornposlist = playerrebornposlist;

}

public int[][] getPlayerrebornposlist() {

return this.playerrebornposlist;

}




}

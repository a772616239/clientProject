package cfg;
import model.base.baseConfigObject;
public class  MistMagicCycleConfigObject implements baseConfigObject{



private int id;

private int lightnum;

private int[] cannotlightflag;

private int[][] operatedata;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setLightnum(int lightnum) {

this.lightnum = lightnum;

}

public int getLightnum() {

return this.lightnum;

}


public void setCannotlightflag(int[] cannotlightflag) {

this.cannotlightflag = cannotlightflag;

}

public int[] getCannotlightflag() {

return this.cannotlightflag;

}


public void setOperatedata(int[][] operatedata) {

this.operatedata = operatedata;

}

public int[][] getOperatedata() {

return this.operatedata;

}




}

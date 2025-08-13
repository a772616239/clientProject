package cfg;
import model.base.baseConfigObject;
public class  MistNpcTaskConfigObject implements baseConfigObject{



private int id;

private int misttasktype;

private int targetcount;

private int extparam;

private int duration;

private int[][] finishrewrad;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setMisttasktype(int misttasktype) {

this.misttasktype = misttasktype;

}

public int getMisttasktype() {

return this.misttasktype;

}


public void setTargetcount(int targetcount) {

this.targetcount = targetcount;

}

public int getTargetcount() {

return this.targetcount;

}


public void setExtparam(int extparam) {

this.extparam = extparam;

}

public int getExtparam() {

return this.extparam;

}


public void setDuration(int duration) {

this.duration = duration;

}

public int getDuration() {

return this.duration;

}


public void setFinishrewrad(int[][] finishrewrad) {

this.finishrewrad = finishrewrad;

}

public int[][] getFinishrewrad() {

return this.finishrewrad;

}




}

package cfg;
import model.base.baseConfigObject;
public class  PetEscapeBulletInvokeCfgObject implements baseConfigObject{



private int id;

private int[] rangetime;

private int invokeinterval;

private int invokepointnum;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setRangetime(int[] rangetime) {

this.rangetime = rangetime;

}

public int[] getRangetime() {

return this.rangetime;

}


public void setInvokeinterval(int invokeinterval) {

this.invokeinterval = invokeinterval;

}

public int getInvokeinterval() {

return this.invokeinterval;

}


public void setInvokepointnum(int invokepointnum) {

this.invokepointnum = invokepointnum;

}

public int getInvokepointnum() {

return this.invokepointnum;

}




}

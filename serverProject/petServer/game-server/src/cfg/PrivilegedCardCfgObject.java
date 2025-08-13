package cfg;
import model.base.baseConfigObject;
public class  PrivilegedCardCfgObject implements baseConfigObject{



private int id;

private int expiredays;

private int[][] instantreward;

private int[][] addcount;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setExpiredays(int expiredays) {

this.expiredays = expiredays;

}

public int getExpiredays() {

return this.expiredays;

}


public void setInstantreward(int[][] instantreward) {

this.instantreward = instantreward;

}

public int[][] getInstantreward() {

return this.instantreward;

}


public void setAddcount(int[][] addcount) {

this.addcount = addcount;

}

public int[][] getAddcount() {

return this.addcount;

}




}

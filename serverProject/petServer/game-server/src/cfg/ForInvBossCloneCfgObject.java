package cfg;
import model.base.baseConfigObject;
public class  ForInvBossCloneCfgObject implements baseConfigObject{



private int id;

private int[][] properties;

private int integraladdition;

private int appearrate;

private int boosaddition;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setProperties(int[][] properties) {

this.properties = properties;

}

public int[][] getProperties() {

return this.properties;

}


public void setIntegraladdition(int integraladdition) {

this.integraladdition = integraladdition;

}

public int getIntegraladdition() {

return this.integraladdition;

}


public void setAppearrate(int appearrate) {

this.appearrate = appearrate;

}

public int getAppearrate() {

return this.appearrate;

}


public void setBoosaddition(int boosaddition) {

this.boosaddition = boosaddition;

}

public int getBoosaddition() {

return this.boosaddition;

}




}

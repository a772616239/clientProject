package cfg;
import model.base.baseConfigObject;
public class  PopupMissionObject implements baseConfigObject{



private int id;

private int missiontype;

private int target;

private int addition;

private int viplv;

private int productid;

private int[][] reward;

private int limittime;

private int limitbuy;

private int dailytriggerlimit;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setMissiontype(int missiontype) {

this.missiontype = missiontype;

}

public int getMissiontype() {

return this.missiontype;

}


public void setTarget(int target) {

this.target = target;

}

public int getTarget() {

return this.target;

}


public void setAddition(int addition) {

this.addition = addition;

}

public int getAddition() {

return this.addition;

}


public void setViplv(int viplv) {

this.viplv = viplv;

}

public int getViplv() {

return this.viplv;

}


public void setProductid(int productid) {

this.productid = productid;

}

public int getProductid() {

return this.productid;

}


public void setReward(int[][] reward) {

this.reward = reward;

}

public int[][] getReward() {

return this.reward;

}


public void setLimittime(int limittime) {

this.limittime = limittime;

}

public int getLimittime() {

return this.limittime;

}


public void setLimitbuy(int limitbuy) {

this.limitbuy = limitbuy;

}

public int getLimitbuy() {

return this.limitbuy;

}


public void setDailytriggerlimit(int dailytriggerlimit) {

this.dailytriggerlimit = dailytriggerlimit;

}

public int getDailytriggerlimit() {

return this.dailytriggerlimit;

}




}

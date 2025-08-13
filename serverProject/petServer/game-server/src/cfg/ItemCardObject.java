package cfg;
import model.base.baseConfigObject;
public class  ItemCardObject implements baseConfigObject{



private int id;

private int limitday;

private int rechargeproductid;

private int buyreward;

private int reward;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setLimitday(int limitday) {

this.limitday = limitday;

}

public int getLimitday() {

return this.limitday;

}


public void setRechargeproductid(int rechargeproductid) {

this.rechargeproductid = rechargeproductid;

}

public int getRechargeproductid() {

return this.rechargeproductid;

}


public void setBuyreward(int buyreward) {

this.buyreward = buyreward;

}

public int getBuyreward() {

return this.buyreward;

}


public void setReward(int reward) {

this.reward = reward;

}

public int getReward() {

return this.reward;

}




}

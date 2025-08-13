package cfg;
import model.base.baseConfigObject;
public class  BuyTaskConfigObject implements baseConfigObject{



private int id;

private int activityid;

private int title;

private int limitbuy;

private int[] price;

private int reward;

private int discount;

private int specialtype;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setActivityid(int activityid) {

this.activityid = activityid;

}

public int getActivityid() {

return this.activityid;

}


public void setTitle(int title) {

this.title = title;

}

public int getTitle() {

return this.title;

}


public void setLimitbuy(int limitbuy) {

this.limitbuy = limitbuy;

}

public int getLimitbuy() {

return this.limitbuy;

}


public void setPrice(int[] price) {

this.price = price;

}

public int[] getPrice() {

return this.price;

}


public void setReward(int reward) {

this.reward = reward;

}

public int getReward() {

return this.reward;

}


public void setDiscount(int discount) {

this.discount = discount;

}

public int getDiscount() {

return this.discount;

}


public void setSpecialtype(int specialtype) {

this.specialtype = specialtype;

}

public int getSpecialtype() {

return this.specialtype;

}




}

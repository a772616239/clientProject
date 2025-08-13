package cfg;
import model.base.baseConfigObject;
public class  ShopRefreshSpendObject implements baseConfigObject{



private int id;

private int shoptype;

private int refreshtimes;

private int[] spend;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setShoptype(int shoptype) {

this.shoptype = shoptype;

}

public int getShoptype() {

return this.shoptype;

}


public void setRefreshtimes(int refreshtimes) {

this.refreshtimes = refreshtimes;

}

public int getRefreshtimes() {

return this.refreshtimes;

}


public void setSpend(int[] spend) {

this.spend = spend;

}

public int[] getSpend() {

return this.spend;

}




}

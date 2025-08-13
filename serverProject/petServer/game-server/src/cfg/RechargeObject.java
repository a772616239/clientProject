package cfg;
import model.base.baseConfigObject;
public class  RechargeObject implements baseConfigObject{



private int id;

private int rechargeamount2;

private int numberofdiamonds;

private int givevipexp;

private int productid;

private int giftdiamonds;

private boolean recommend;

private boolean purchaselimit;

private int rechargetype;

private boolean firstrechargetype;

private int begintime;

private int endtime;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setRechargeamount2(int rechargeamount2) {

this.rechargeamount2 = rechargeamount2;

}

public int getRechargeamount2() {

return this.rechargeamount2;

}


public void setNumberofdiamonds(int numberofdiamonds) {

this.numberofdiamonds = numberofdiamonds;

}

public int getNumberofdiamonds() {

return this.numberofdiamonds;

}


public void setGivevipexp(int givevipexp) {

this.givevipexp = givevipexp;

}

public int getGivevipexp() {

return this.givevipexp;

}


public void setProductid(int productid) {

this.productid = productid;

}

public int getProductid() {

return this.productid;

}


public void setGiftdiamonds(int giftdiamonds) {

this.giftdiamonds = giftdiamonds;

}

public int getGiftdiamonds() {

return this.giftdiamonds;

}


public void setRecommend(boolean recommend) {

this.recommend = recommend;

}

public boolean getRecommend() {

return this.recommend;

}


public void setPurchaselimit(boolean purchaselimit) {

this.purchaselimit = purchaselimit;

}

public boolean getPurchaselimit() {

return this.purchaselimit;

}


public void setRechargetype(int rechargetype) {

this.rechargetype = rechargetype;

}

public int getRechargetype() {

return this.rechargetype;

}


public void setFirstrechargetype(boolean firstrechargetype) {

this.firstrechargetype = firstrechargetype;

}

public boolean getFirstrechargetype() {

return this.firstrechargetype;

}


public void setBegintime(int begintime) {

this.begintime = begintime;

}

public int getBegintime() {

return this.begintime;

}


public void setEndtime(int endtime) {

this.endtime = endtime;

}

public int getEndtime() {

return this.endtime;

}




}

package cfg;
import model.base.baseConfigObject;
public class  RechargeProductObject implements baseConfigObject{



private int id;

private String iosproductid;

private String googleproductid;

private String hyzproductid;

private int rechargescore;

private int producttype;

private int subtype;

private int[][] reward;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setIosproductid(String iosproductid) {

this.iosproductid = iosproductid;

}

public String getIosproductid() {

return this.iosproductid;

}


public void setGoogleproductid(String googleproductid) {

this.googleproductid = googleproductid;

}

public String getGoogleproductid() {

return this.googleproductid;

}


public void setHyzproductid(String hyzproductid) {

this.hyzproductid = hyzproductid;

}

public String getHyzproductid() {

return this.hyzproductid;

}


public void setRechargescore(int rechargescore) {

this.rechargescore = rechargescore;

}

public int getRechargescore() {

return this.rechargescore;

}


public void setProducttype(int producttype) {

this.producttype = producttype;

}

public int getProducttype() {

return this.producttype;

}


public void setSubtype(int subtype) {

this.subtype = subtype;

}

public int getSubtype() {

return this.subtype;

}


public void setReward(int[][] reward) {

this.reward = reward;

}

public int[][] getReward() {

return this.reward;

}




}

package cfg;
import model.base.baseConfigObject;
public class  ShopSellObject implements baseConfigObject{



private int id;

private int shopid;

private String servername;

private int[] cargo;

private int[] price;

private int buylimit;

private int vipexp;

private int sellgroup;

private int appearrate;

private int specialtype;

private int[] specialparam;

private boolean selling;

private int unlockcondtion;

private int discounttype;

private int honrlv;

private int showviplv;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setShopid(int shopid) {

this.shopid = shopid;

}

public int getShopid() {

return this.shopid;

}


public void setServername(String servername) {

this.servername = servername;

}

public String getServername() {

return this.servername;

}


public void setCargo(int[] cargo) {

this.cargo = cargo;

}

public int[] getCargo() {

return this.cargo;

}


public void setPrice(int[] price) {

this.price = price;

}

public int[] getPrice() {

return this.price;

}


public void setBuylimit(int buylimit) {

this.buylimit = buylimit;

}

public int getBuylimit() {

return this.buylimit;

}


public void setVipexp(int vipexp) {

this.vipexp = vipexp;

}

public int getVipexp() {

return this.vipexp;

}


public void setSellgroup(int sellgroup) {

this.sellgroup = sellgroup;

}

public int getSellgroup() {

return this.sellgroup;

}


public void setAppearrate(int appearrate) {

this.appearrate = appearrate;

}

public int getAppearrate() {

return this.appearrate;

}


public void setSpecialtype(int specialtype) {

this.specialtype = specialtype;

}

public int getSpecialtype() {

return this.specialtype;

}


public void setSpecialparam(int[] specialparam) {

this.specialparam = specialparam;

}

public int[] getSpecialparam() {

return this.specialparam;

}


public void setSelling(boolean selling) {

this.selling = selling;

}

public boolean getSelling() {

return this.selling;

}


public void setUnlockcondtion(int unlockcondtion) {

this.unlockcondtion = unlockcondtion;

}

public int getUnlockcondtion() {

return this.unlockcondtion;

}


public void setDiscounttype(int discounttype) {

this.discounttype = discounttype;

}

public int getDiscounttype() {

return this.discounttype;

}


public void setHonrlv(int honrlv) {

this.honrlv = honrlv;

}

public int getHonrlv() {

return this.honrlv;

}


public void setShowviplv(int showviplv) {

this.showviplv = showviplv;

}

public int getShowviplv() {

return this.showviplv;

}




}

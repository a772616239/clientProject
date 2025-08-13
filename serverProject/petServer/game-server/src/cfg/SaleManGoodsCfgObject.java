package cfg;
import model.base.baseConfigObject;
public class  SaleManGoodsCfgObject implements baseConfigObject{



private int id;

private int shopshellid;

private int[][] discount;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setShopshellid(int shopshellid) {

this.shopshellid = shopshellid;

}

public int getShopshellid() {

return this.shopshellid;

}


public void setDiscount(int[][] discount) {

this.discount = discount;

}

public int[][] getDiscount() {

return this.discount;

}




}

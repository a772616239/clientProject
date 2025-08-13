package cfg;
import model.base.baseConfigObject;
public class  TrainingShopObject implements baseConfigObject{



private int id;

private int group;

private int level;

private int itemid;

private int[][] price;

private int limit;

private int[][] discount;

private int free;

private int priceadd;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setGroup(int group) {

this.group = group;

}

public int getGroup() {

return this.group;

}


public void setLevel(int level) {

this.level = level;

}

public int getLevel() {

return this.level;

}


public void setItemid(int itemid) {

this.itemid = itemid;

}

public int getItemid() {

return this.itemid;

}


public void setPrice(int[][] price) {

this.price = price;

}

public int[][] getPrice() {

return this.price;

}


public void setLimit(int limit) {

this.limit = limit;

}

public int getLimit() {

return this.limit;

}


public void setDiscount(int[][] discount) {

this.discount = discount;

}

public int[][] getDiscount() {

return this.discount;

}


public void setFree(int free) {

this.free = free;

}

public int getFree() {

return this.free;

}


public void setPriceadd(int priceadd) {

this.priceadd = priceadd;

}

public int getPriceadd() {

return this.priceadd;

}




}

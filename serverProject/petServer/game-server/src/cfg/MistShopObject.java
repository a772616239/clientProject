package cfg;
import model.base.baseConfigObject;
public class  MistShopObject implements baseConfigObject{



private int id;

private int[][] goodslist;




public void setId(int id) {

this.id = id;

}

public int getId() {

return this.id;

}


public void setGoodslist(int[][] goodslist) {

this.goodslist = goodslist;

}

public int[][] getGoodslist() {

return this.goodslist;

}




}

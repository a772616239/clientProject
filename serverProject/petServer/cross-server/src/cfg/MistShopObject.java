package cfg;

import model.base.baseConfigObject;

public class MistShopObject implements baseConfigObject {


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

    public boolean checkSellingGoods(int goodsType, int goodsId) {
        if (goodslist == null) {
            return false;
        }
        for (int i = 0; i < goodslist.length; i++) {
            if (goodslist[i][0] == goodsType && goodslist[i][1] == goodsId) {
                return true;
            }
        }
        return false;
    }
}

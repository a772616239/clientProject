package model.mistforest.map.grid;

public class ShopGrid extends Grid {
    private int shopId;

    public ShopGrid(int gridType) {
        super(gridType);
    }

    public int getShopId() {
        return shopId;
    }

    public void setShopId(int shopId) {
        this.shopId = shopId;
    }
}

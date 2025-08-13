package model.shop.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import common.entity.DBEntity;

/**
 * 商店数据库存储对象
 */
public class ShopDB extends DBEntity implements Serializable {

    private static final long serialVersionUID = 2328889873480113414L;

    /**
     * <商店类型,商店信息总和></>
     */
    Map<Integer, PlayerShopInfo> shopInfo = new HashMap<>();

    public void putShopInfo(int shopType, PlayerShopInfo shopInfo) {
        this.shopInfo.put(shopType, shopInfo);
    }

    public Map<Integer, PlayerShopInfo> getShopInfo() {
        return shopInfo;
    }

    public void setShopInfo(Map<Integer, PlayerShopInfo> shopInfo) {
        this.shopInfo = shopInfo;
    }
}

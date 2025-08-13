/**
 * created by tool DAOGenerate
 */
package model.shop.entity;

import cfg.ShopConfig;
import cfg.ShopConfigObject;
import common.tick.GlobalTick;
import model.obj.BaseObj;
import model.player.util.PlayerUtil;
import model.shop.StoreManager;
import model.shop.dbCache.shopCache;
import protocol.Shop.GoodsInfo;
import protocol.Shop.ShopInfo;
import protocol.Shop.ShopTypeEnum;
import util.GameUtil;
import util.LogUtil;
import util.TimeUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * created by tool
 */
@SuppressWarnings("serial")
public class shopEntity extends BaseObj {

    public String getClassType() {
        return "shopEntity";
    }

    /**
     *
     */
    private String playeridx;

    /**
     *
     */
    private byte[] shopinfo;


    /**
     * 获得
     */
    public String getPlayeridx() {
        return playeridx;
    }

    /**
     * 设置
     */
    public void setPlayeridx(String playeridx) {
        this.playeridx = playeridx;
    }

    /**
     * 获得
     */
    public byte[] getShopinfo() {
        return shopinfo;
    }

    /**
     * 设置
     */
    public void setShopinfo(byte[] shopinfo) {
        this.shopinfo = shopinfo;
    }

    public String getBaseIdx() {
        // TODO Auto-generated method stub
        return playeridx;
    }

    private shopEntity() {
    }

    public shopEntity(String playerIdx) {
        this.playeridx = playerIdx;
    }

    private ShopDB dbBuilder;

    public ShopDB getDbBuilder() {
        if (dbBuilder == null) {
            dbBuilder = getDBInfo();
        }
        return dbBuilder;
    }

    private ShopDB getDBInfo() {
        try {
            if (this.shopinfo != null) {
                return (ShopDB) ShopDB.parseFrom(shopinfo);
            } else {
                return new ShopDB();
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            LogUtil.error("parse db shopInfo failed, return new builder, playerIdx:" + getPlayeridx());
            return new ShopDB();
        }
    }

    @Override
    public void putToCache() {
        shopCache.put(this);
    }

    @Override
    public void transformDBData() {
        this.shopinfo = getDbBuilder().toByteArray();
    }

    /**
     * 如对该方法返回的对象进行修改,亲调用以下方法使更改生效
     *
     * @param shopType
     * @return
     * @see shopEntity#putPlayerShopInfo
     */
    public PlayerShopInfo getPlayerShopInfo(ShopTypeEnum shopType) {
        if (shopType == null || shopType == ShopTypeEnum.STE_null) {
            LogUtil.error("shopEntity.getPlayerShopInfoBuilder, error params, shopType=" + shopType);
            return null;
        }
        PlayerShopInfo playerStoreInfo = getDbBuilder().getShopInfo().get(shopType.getNumber());
        if (playerStoreInfo == null || playerStoreInfo.getNextRefreshTime() < GlobalTick.getInstance().getCurrentTime()) {
            PlayerShopInfo shopInfo = StoreManager.getInstance().initPlayerShopInfo(getPlayeridx(), shopType);
            if (shopInfo == null) {
                shopInfo = new PlayerShopInfo();
            }
            shopInfo.setNextRefreshTime(calculateNextUpdateTime(shopType, playerStoreInfo == null ? 0 : playerStoreInfo.getAutoRefreshTimes()));
            putPlayerShopInfo(shopType, shopInfo);
            return shopInfo;
        }
        return playerStoreInfo;
    }

    public void putPlayerShopInfo(ShopTypeEnum shopType, PlayerShopInfo shopInfo) {
        if (shopType == null || shopType == ShopTypeEnum.STE_null || shopInfo == null) {
            return;
        }
        getDbBuilder().putShopInfo(shopType.getNumber(), shopInfo);
    }

    /**
     * 如对该方法返回的对象进行修改,亲调用以下方法使更改生效
     *
     * @param shopType
     * @param buyGoodsId
     * @return
     * @see shopEntity#putGoodsInfo
     */
    public GoodsInfo getGoodsInfo(ShopTypeEnum shopType, int buyGoodsId) {
        PlayerShopInfo shopInfo = getPlayerShopInfo(shopType);
        if (shopInfo == null) {
            return null;
        }
        return shopInfo.getBuyRecord().get(buyGoodsId);
    }

    public void putGoodsInfo(ShopTypeEnum shopType, GoodsInfo.Builder goodsInfo) {
        if (goodsInfo == null) {
            return;
        }

        PlayerShopInfo shopInfoBuilder = getPlayerShopInfo(shopType);
        if (shopInfoBuilder == null) {
            shopInfoBuilder =new PlayerShopInfo();
        }
        shopInfoBuilder.putBuyRecord(goodsInfo.getGoodsCfgId(), goodsInfo.build());
        putPlayerShopInfo(shopType, shopInfoBuilder);
    }

    public int getAlreadyBuyTimes(ShopTypeEnum shopType, int buyGoodsId) {
        GoodsInfo info = getGoodsInfo(shopType, buyGoodsId);
        if (info == null) {
            return 0;
        }
        return info.getAlreadBuyTimes();
    }

    public void addBuyTimes(ShopTypeEnum shopType, int buyGoodsId, int addBuyCount) {
        if (addBuyCount <= 0) {
            return;
        }

        int newCount = addBuyCount;
        GoodsInfo goodsInfo = getGoodsInfo(shopType, buyGoodsId);
        if (goodsInfo != null) {
            newCount += goodsInfo.getAlreadBuyTimes();
        }

        GoodsInfo.Builder builder = GoodsInfo.newBuilder().setGoodsCfgId(buyGoodsId).setAlreadBuyTimes(newCount);
        putGoodsInfo(shopType, builder);
    }

    public int getManualRefreshTimes(ShopTypeEnum shopType) {
        PlayerShopInfo shopInfo = getPlayerShopInfo(shopType);
        if (shopInfo == null) {
            return 0;
        }
        return shopInfo.getManualRefreshTimes();
    }

    public ShopInfo buildPlayerShowShopInfo(ShopTypeEnum shopType) {
        PlayerShopInfo playerShopInfo = getPlayerShopInfo(shopType);
        if (playerShopInfo == null) {
            return null;
        }

        ShopInfo.Builder result = ShopInfo.newBuilder();
        result.setShopType(shopType);
        result.setManualRefreshTimes(playerShopInfo.getManualRefreshTimes());
        result.setNextRefreshTime(playerShopInfo.getNextRefreshTime());
        result.addAllGoodsInfos(convertShowShopInfo(playerShopInfo,shopType));
        return result.build();
    }

    private Collection<GoodsInfo> convertShowShopInfo(PlayerShopInfo playerShopInfo, ShopTypeEnum shopType) {
        if (ShopTypeEnum.STE_VipShop == shopType) {
            List<Integer> showSells = StoreManager.getInstance().getVipShopShopSells(PlayerUtil.queryPlayerVipLv(playeridx));
            return playerShopInfo.getBuyRecord().values().stream().filter(e -> showSells.contains(e.getGoodsCfgId())).collect(Collectors.toList());
        }
        return playerShopInfo.getBuyRecord().values();
    }

    /**
     * 手动刷新商店并添加刷新次数
     */
    public void manualRefreshAndAddTimes(ShopTypeEnum shopType) {
        manualRefresh(shopType);
        addManualRefreshTimes(shopType);
    }

    /**
     * 手动刷新
     *
     * @param shopType
     */
    public void manualRefresh(ShopTypeEnum shopType) {
        PlayerShopInfo builder = getPlayerShopInfo(shopType);
        if (builder == null) {
            return;
        }

        List<GoodsInfo> goodsInfos = StoreManager.getInstance().manualRefresh(getPlayeridx(), shopType, builder.getBuyRecord().values());
        PlayerShopInfo shopInfo = getPlayerShopInfo(shopType);
        if (GameUtil.collectionIsEmpty(goodsInfos) || shopInfo == null) {
            return;
        }

        shopInfo.clearBuyRecord();
        for (GoodsInfo goodsInfo : goodsInfos) {
            shopInfo.putBuyRecord(goodsInfo.getGoodsCfgId(), goodsInfo);
        }

        putPlayerShopInfo(shopType, shopInfo);
    }

    /**
     * 增加手动刷新次数
     */
    public void addManualRefreshTimes(ShopTypeEnum shopType) {
        PlayerShopInfo builder = getPlayerShopInfo(shopType);
        if (builder == null) {
            return;
        }

        builder.setManualRefreshTimes(builder.getManualRefreshTimes() + 1);

        putPlayerShopInfo(shopType, builder);
    }

    /**
     * 更新每日数据
     */
    public void updateDailyData() {
        ShopDB builder = getDbBuilder();
        Map<ShopTypeEnum, PlayerShopInfo> needUpdate = new HashMap<>();
        for (Entry<Integer, PlayerShopInfo> entry : builder.getShopInfo().entrySet()) {
            ShopTypeEnum typeEnum = ShopTypeEnum.forNumber(entry.getKey());
            ShopConfigObject shopConfig = ShopConfig.getById(entry.getKey());
            if (shopConfig == null || shopConfig.getAutorefresh() == 0) {
                continue;
            }
            PlayerShopInfo shopInfo = entry.getValue();
            if (GlobalTick.getInstance().getCurrentTime() >= shopInfo.getNextRefreshTime()) {
                autoRefresh(typeEnum);
            } else {
                needUpdate.put(typeEnum, shopInfo.clearManualRefreshTimes());
            }
        }

        if (!needUpdate.isEmpty()) {
            needUpdate.forEach(this::putPlayerShopInfo);
        }
    }

    /**
     * 商店自动刷新
     *
     * @param shopType
     */
    public void autoRefresh(ShopTypeEnum shopType) {
        PlayerShopInfo builder = getPlayerShopInfo(shopType);
        if (builder == null) {
            return;
        }

        //清空购买记录
        builder.clearBuyRecord();
        List<GoodsInfo> newList = StoreManager.getInstance().autoRefresh(getPlayeridx(), shopType, builder.getAutoRefreshTimes());
        if (!GameUtil.collectionIsEmpty(newList)) {
            newList.forEach(e -> builder.putBuyRecord(e.getGoodsCfgId(), e));
        }
        builder.setNextRefreshTime(calculateNextUpdateTime(shopType, builder.getAutoRefreshTimes()));
        builder.setAutoRefreshTimes(builder.getAutoRefreshTimes() + 1);
        putPlayerShopInfo(shopType, builder);
    }

    private long calculateNextUpdateTime(ShopTypeEnum shopType, int autoRefreshTimes) {
        for (ShopConfigObject config : ShopConfig._ix_id.values()) {
            if (shopType == ShopTypeEnum.forNumber(config.getId())) {
                int nextDay = autoRefreshTimes <= 0 ? config.getInitcycle() : config.getGeneralcycle();
                return TimeUtil.getNextDaysResetTime(GlobalTick.getInstance().getCurrentTime(), nextDay);
            }
        }
        LogUtil.error("can`t calculateNextUpdateTime by shopType :{},playerId:{}", shopType, getPlayeridx());
        return Long.MAX_VALUE;
    }
}
package model.shop;

import cfg.Mission;
import cfg.MissionObject;
import cfg.ShopConfig;
import cfg.ShopConfigObject;
import cfg.ShopSell;
import cfg.ShopSellObject;
import cfg.VIPConfig;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.Getter;
import model.gameplay.GamePlayerUpdate;
import model.gameplay.dbCache.gameplayCache;
import model.gameplay.entity.gameplayEntity;
import model.player.util.PlayerUtil;
import model.shop.entity.PlayerShopInfo;
import protocol.Common.EnumFunction;
import protocol.Common.RewardSourceEnum;
import protocol.GameplayDB.DB_StoreInfo;
import protocol.GameplayDB.GameplayTypeEnum;
import protocol.Shop.GoodsInfo;
import protocol.Shop.ShopTypeEnum;
import util.LogUtil;

/**
 * @author huhan
 * @date 2020/04/03
 */
public class StoreManager implements GamePlayerUpdate {
    private static StoreManager instance;

    public static StoreManager getInstance() {
        if (instance == null) {
            synchronized (StoreManager.class) {
                if (instance == null) {
                    instance = new StoreManager();
                }
            }
        }
        return instance;
    }

    private static final String UPDATE_IDX = String.valueOf(GameplayTypeEnum.GTE_Stores_VALUE);

    private StoreManager() {
    }

    private Map<ShopTypeEnum, Store> storeMap = new ConcurrentHashMap<>();

    //<任务类型,该类型下任务>
    @Getter
    private Map<Integer, List<MissionObject>> storeMission = new HashMap<>();

    public boolean init() {
        initShopMission();
        DB_StoreInfo.Builder storeInfo = getDbStoreInfo();
        for (ShopConfigObject value : ShopConfig._ix_id.values()) {
            ShopTypeEnum typeEnum = ShopTypeEnum.forNumber(value.getId());
            if (null == typeEnum || ShopTypeEnum.STE_null == typeEnum) {
                continue;
            }
            Store store = new Store(value);
            Long aLong = storeInfo.getUpdateTimeMap().get(value.getId());
            if (aLong != null) {
                store.setNextUpdateTime(aLong);
            }
            if (storeMap.containsKey(typeEnum)) {
                LogUtil.error("ShopConfig exist the same type config, shopType id =" + value.getId());
                return false;
            }
            this.storeMap.put(typeEnum, store);

            if (!initAutoCycleGoods(store, value.getGoodscycle())) {
                return false;
            }

        }

        //添加商品
        for (ShopSellObject value : ShopSell._ix_id.values()) {
            //跳过商店类型配置为0 或者不在售卖中
            if (value.getShopid() == 0 || !value.getSelling()) {
                continue;
            }
            ShopTypeEnum shopTypeEnum = ShopTypeEnum.forNumber(value.getShopid());
            if (shopTypeEnum == null) {
                LogUtil.error("model.shop.StoreManager.init, shop goods link shop is not exist, shop id:" + value.getShopid());
                return false;
            }
            //流浪商人商品不放商店
            if (ShopTypeEnum.STE_ScaleMan == shopTypeEnum) {
                continue;
            }
            Store store = this.storeMap.get(shopTypeEnum);
            if (null == store) {
                LogUtil.error("shop sell config, sell id " + value.getId()
                        + " shop type is not exist, shop type =" + value.getShopid());
                continue;
            }
            store.addGoods(value);
        }

        initVipShopShells();

        for (Store value : storeMap.values()) {
            if (!value.check()) {
                return false;
            }
            if (!value.afterGoodsInit()){
                return false;
            }
        }
        return gameplayCache.getInstance().addToUpdateSet(this);
    }

    Map<Integer, List<Integer>> vipLvSells = new HashMap<>();


    public List<Integer> getVipShopShopSells(int vipLv) {
        return vipLvSells.getOrDefault(vipLv, Collections.emptyList());
    }

    private void initVipShopShells() {
        for (Integer vipLv : VIPConfig._ix_id.keySet()) {
            vipLvSells.put(vipLv, ShopSell._ix_id.values().stream().filter(e -> e.getShowviplv() <= vipLv&&e.getShopid()==ShopTypeEnum.STE_VipShop_VALUE).map(ShopSellObject::getId).collect(Collectors.toList()));
        }
    }

    private void initShopMission() {
        Set<MissionObject> missions = new HashSet<>();
        for (ShopSellObject cfg : ShopSell._ix_id.values()) {
            if (cfg.getUnlockcondtion() > 0) {
                MissionObject mission = Mission.getById(cfg.getUnlockcondtion());
                missions.add(mission);
            }
        }
        storeMission = missions.stream().collect(Collectors.groupingBy(MissionObject::getMissiontype));
    }

    private boolean initAutoCycleGoods(Store store, int[][] autoRefreshGoodsCycle) {
        if (autoRefreshGoodsCycle.length > 0) {
            List<List<ShopSellObject>> autoCycleGoods = new ArrayList<>();
            for (int[] sellIds : autoRefreshGoodsCycle) {
                List<ShopSellObject> shopSellList = new ArrayList<>();
                for (int sellId : sellIds) {
                    ShopSellObject sell = ShopSell.getById(sellId);
                    if (sell == null) {
                        LogUtil.error("ShopConfig goodsCycle config not exist shopCell by shopId:{} ,sellId:{}", store.getShopTypeValue(), sellId);
                        return false;
                    }
                    if (!sell.getSelling()) {
                        LogUtil.warn("ShopConfig goodsCycle contain un selling goods, id:" + sell.getId());
                        continue;
                    }
                    shopSellList.add(sell);
                }
                autoCycleGoods.add(shopSellList);
            }
            store.setAutoCycleGoods(autoCycleGoods);
        }
        return true;
    }

/*    @Override
    public void onTick() {
        for (Store store : storeMap.values()) {
            store.tick();
        }
    }*/

    /**
     * 初始化商店信息
     *
     * @param shopType
     */
    public PlayerShopInfo initPlayerShopInfo(String playerIdx, ShopTypeEnum shopType) {
        if (shopType == null) {
            return null;
        }
        Store store = this.storeMap.get(shopType);
        if (store == null) {
            LogUtil.error("model.shop.StoreManager.initPlayerStoreInfo, shop type store is not exist, shopType:" + shopType);
            return null;
        }
        return store.createPlayerShopInfoBuilder(playerIdx);
    }

    public DB_StoreInfo.Builder getDbStoreInfo() {
        gameplayEntity entity = gameplayCache.getByIdx(UPDATE_IDX);
        if (entity == null) {
            LogUtil.debug("model.shop.StoreManager.getDBMallInfo, entity is null");
            return DB_StoreInfo.newBuilder();
        }

        try {
            return DB_StoreInfo.parseFrom(entity.getGameplayinfo()).toBuilder();
        } catch (InvalidProtocolBufferException e) {
            LogUtil.printStackTrace(e);
            LogUtil.error("parse mall info failed, return new DB_MallInfo.builder");
            return DB_StoreInfo.newBuilder();
        }
    }


    @Override
    public void update() {
        gameplayEntity entity = gameplayCache.getByIdx(UPDATE_IDX);
        if (entity == null) {
            entity = new gameplayEntity();
            entity.setIdx(UPDATE_IDX);
        }

        DB_StoreInfo.Builder builder = DB_StoreInfo.newBuilder();
        for (Store value : storeMap.values()) {
            builder.putUpdateTime(value.getShopTypeValue(), value.getNextUpdateTime());
        }

        entity.setGameplayinfo(builder.build().toByteArray());
        entity.putToCache();
    }

    /**
     * 判断玩家商店功能是否已经开启
     *
     * @param playerIdx
     * @param shopType
     * @return
     */
    public static boolean storeIsUnlock(String playerIdx, ShopTypeEnum shopType) {
        if (shopType == ShopTypeEnum.STE_BlackMarket
                && !PlayerUtil.queryFunctionUnlock(playerIdx, EnumFunction.BlackMarket)) {
            return false;
        }

        return true;
    }

    public boolean supportManualRefresh(ShopTypeEnum shopType) {
        Store store = this.storeMap.get(shopType);
        if (store == null) {
            return false;
        }
        return store.supportManual();
    }

    /**
     * 手动刷新并添加手动刷新次数
     *
     * @param shopType
     * @param curShopInfoList 玩家当前的商品列表
     * @return
     */
    public List<GoodsInfo> manualRefresh(String playerIdx, ShopTypeEnum shopType, Collection<GoodsInfo> curShopInfoList) {
        Store store = this.storeMap.get(shopType);
        if (store == null) {
            return null;
        }

        return store.refreshByManual(playerIdx, curShopInfoList);
    }

    public static RewardSourceEnum getRewardSourceTypeByShopType(ShopTypeEnum shopType) {
        if (ShopTypeEnum.STE_BlackMarket == shopType) {
            return RewardSourceEnum.RSE_BlackMarket;
        } else if (ShopTypeEnum.STE_CourageTrial == shopType) {
            return RewardSourceEnum.RSE_BraveChallengeStore;
        } else if (ShopTypeEnum.STE_Pet == shopType) {
            return RewardSourceEnum.RSE_PetStore;
        } else if (ShopTypeEnum.STE_Vagrant == shopType) {
            return RewardSourceEnum.RSE_Vagrant;
        } else if (shopType == ShopTypeEnum.STE_Arena) {
            return RewardSourceEnum.RSE_ArenaStore;
        }
        return RewardSourceEnum.RSE_Null;
    }

    public long getNextUpdateTime(ShopTypeEnum shopType) {
        if (shopType == null) {
            return 0;
        }
        Store store = this.storeMap.get(shopType);
        if (store == null) {
            LogUtil.error("model.shop.StoreManager.getNextUpdateTime, shop type store is not exist, shopType:" + shopType);
            return 0;
        }
        return store.getNextUpdateTime();
    }

    public List<GoodsInfo> autoRefresh(String playerIdx, ShopTypeEnum shopType, int autoRefreshTimes) {
        if (shopType == null) {
            return null;
        }
        Store store = this.storeMap.get(shopType);
        if (store == null) {
            return null;
        }
        return store.refreshByAuto(playerIdx, autoRefreshTimes);
    }
}
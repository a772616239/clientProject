/*CREATED BY TOOL*/

package model.shop.dbCache;

import annotation.annationInit;
import common.SyncExecuteFunction;
import datatool.StringHelper;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;
import entity.UpdateDailyData;
import java.util.Map;
import model.base.cache.IbaseCache;
import model.base.cache.baseCache;
import model.base.cache.baseUpdateCache;
import model.player.util.PlayerUtil;
import model.shop.cache.shopUpdateCache;
import model.shop.entity.shopEntity;
import protocol.Shop.ShopTypeEnum;
import util.LogUtil;

@annationInit(value = "shopCache", methodname = "load")
public class shopCache extends baseCache<shopCache> implements IbaseCache, UpdateDailyData {

    /******************* MUST HAVE ********************************/

    private static shopCache instance = null;

    public static shopCache getInstance() {

        if (instance == null) {
            instance = new shopCache();
        }
        return instance;

    }


    public String getDaoName() {

        return "shopDAO";
    }

    public BaseDAO getDao() {

        return AppContext.getBean("shopDAO");
    }

    public void load(baseCache o) {

        if (instance == null) {
            instance = (shopCache) o;
        }
        super.loadAllFromDb();

    }

    public static void put(shopEntity v) {

        getInstance().putBaseEntity(v);

    }

    public static shopEntity getByIdx(String idx) {
        if (StringHelper.isNull(idx))
            return null;


        BaseEntity v = getInstance().getBaseEntityByIdx(idx);
        if (v == null)
            return null;

        return (shopEntity) v;

    }

    public Map<String, BaseEntity> getAll() {

        return getInstance()._ix_id;

    }

    public baseUpdateCache getUpdateCache() {

        return shopUpdateCache.getInstance();

    }

    public static void remove(String idx) {

        getInstance().BaseRemove(idx);

    }


    public void putToMem(BaseEntity v) {

        shopEntity t = (shopEntity) v;

    }

    /*******************MUST HAVE END ********************************/

    public shopEntity getEntityByPlayerIdx(String playerIdx) {
        if (!PlayerUtil.playerIsExist(playerIdx)) {
            return null;
        }
        shopEntity entity = getByIdx(playerIdx);
        if (entity == null && PlayerUtil.playerIsExist(playerIdx)) {
            entity = new shopEntity(playerIdx);
            entity.putToCache();
        }
        return entity;
    }

    @Override
    public void updateDailyData() {
        for (BaseEntity value : _ix_id.values()) {
            if (!(value instanceof shopEntity)) {
                continue;
            }
            shopEntity entity = (shopEntity) value;
            try {
                SyncExecuteFunction.executeConsumer(entity, e -> entity.updateDailyData());
            }catch (Exception ex){
                LogUtil.error("shopCache.updateDailyData error by playerId:[{}]", entity.getPlayeridx());
                LogUtil.printStackTrace(ex);
            }
        }
    }

    public void autoRefresh(ShopTypeEnum shopType) {
        if (shopType == null) {
            return;
        }

        for (BaseEntity value : _ix_id.values()) {
            if (!(value instanceof shopEntity)) {
                continue;
            }
            shopEntity entity = (shopEntity) value;
            SyncExecuteFunction.executeConsumer(entity, e -> entity.autoRefresh(shopType));
        }
    }
}

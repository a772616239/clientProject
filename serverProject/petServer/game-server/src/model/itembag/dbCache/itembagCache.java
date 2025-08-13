/*CREATED BY TOOL*/

package model.itembag.dbCache;

import annotation.annationInit;
import datatool.StringHelper;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;
import model.base.cache.IbaseCache;
import model.base.cache.baseCache;
import model.base.cache.baseUpdateCache;
import model.itembag.cache.itembagUpdateCache;
import model.itembag.entity.itembagEntity;
import model.player.util.PlayerUtil;
import protocol.ItemBagDB.DB_ItemBag;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@annationInit(value = "itembagCache", methodname = "load")
public class itembagCache extends baseCache<itembagCache> implements IbaseCache {

    /******************* MUST HAVE ********************************/

    private static itembagCache instance = null;

    public static itembagCache getInstance() {

        if (instance == null) {
            instance = new itembagCache();
        }
        return instance;

    }


    public String getDaoName() {

        return "itembagDAO";
    }

    public BaseDAO getDao() {

        return AppContext.getBean("itembagDAO");
    }

    public void load(baseCache o) {

        if (instance == null) {
            instance = (itembagCache) o;
        }
        super.loadAllFromDb();

    }

    public static void put(itembagEntity v) {
        getInstance().putBaseEntity(v);

    }

    public static itembagEntity getByIdx(String idx) {
        if (StringHelper.isNull(idx))
            return null;


        BaseEntity v = getInstance().getBaseEntityByIdx(idx);
        if (v == null)
            return null;

        return (itembagEntity) v;

    }

    public Map<String, BaseEntity> getAll() {

        return getInstance()._ix_id;

    }

    public baseUpdateCache getUpdateCache() {

        return itembagUpdateCache.getInstance();

    }

    public static void remove(String idx) {

        getInstance().BaseRemove(idx);

    }


    public void putToMem(BaseEntity v) {

        itembagEntity t = (itembagEntity) v;
        if (t != null) {
            String linkPlayerIdx = t.getLinkplayeridx();
            if (linkPlayerIdx != null) {
                itemMap.put(linkPlayerIdx, t);
            }
        }
    }

    /*******************MUST HAVE END*******************************/

    private static Map<String, itembagEntity> itemMap = new ConcurrentHashMap<>();

    public itembagEntity getItemBagByPlayerIdx(String playerIdx) {
        if (playerIdx == null) {
            return null;
        }

        itembagEntity entity = itemMap.get(playerIdx);
        if (entity == null && PlayerUtil.playerIsExist(playerIdx)) {
            entity = new itembagEntity(playerIdx);
            DB_ItemBag.Builder itemInfo = entity.getDb_data();
            itemInfo.setBagCapacityLimit(Integer.MAX_VALUE);
            put(entity);
        }

        return entity;
    }

    public long getPlayerItemCount(String playerIdx, int itemCfgId) {
        itembagEntity entity = getItemBagByPlayerIdx(playerIdx);
        if (entity == null) {
            return 0;
        }
        return entity.getItemCount(itemCfgId);
    }

    public void onPlayerLogin(String playerIdx) {
        itembagEntity entity = getItemBagByPlayerIdx(playerIdx);
        if (entity == null) {
            return;
        }
        entity.sendDailyLimitItemUse();
    }
}

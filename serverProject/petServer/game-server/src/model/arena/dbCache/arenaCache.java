/*CREATED BY TOOL*/

package model.arena.dbCache;

import annotation.annationInit;
import common.SyncExecuteFunction;
import datatool.StringHelper;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;
import entity.UpdateDailyData;
import java.util.Map;
import model.arena.cache.arenaUpdateCache;
import model.arena.entity.arenaEntity;
import model.base.cache.IbaseCache;
import model.base.cache.baseCache;
import model.base.cache.baseUpdateCache;
import model.player.util.PlayerUtil;

@annationInit(value = "arenaCache", methodname = "load")
public class arenaCache extends baseCache<arenaCache> implements IbaseCache, UpdateDailyData {

    /******************* MUST HAVE ********************************/

    private static arenaCache instance = null;

    public static arenaCache getInstance() {

        if (instance == null) {
            instance = new arenaCache();
        }
        return instance;

    }


    public String getDaoName() {

        return "arenaDAO";
    }

    public BaseDAO getDao() {

        return AppContext.getBean("arenaDAO");
    }

    public void load(baseCache o) {

        if (instance == null) {
            instance = (arenaCache) o;
        }
        super.loadAllFromDb();

    }

    public static void put(arenaEntity v) {

        getInstance().putBaseEntity(v);

    }

    public static arenaEntity getByIdx(String idx) {
        if (StringHelper.isNull(idx))
            return null;


        BaseEntity v = getInstance().getBaseEntityByIdx(idx);
        if (v == null)
            return null;

        return (arenaEntity) v;

    }

    public Map<String, BaseEntity> getAll() {

        return getInstance()._ix_id;

    }

    public baseUpdateCache getUpdateCache() {

        return arenaUpdateCache.getInstance();

    }

    public static void remove(String idx) {

        getInstance().BaseRemove(idx);

    }


    public void putToMem(BaseEntity v) {

        arenaEntity t = (arenaEntity) v;

    }

    /*******************MUST HAVE END ********************************/

    public arenaEntity getEntity(String playerIdx) {
        if (StringHelper.isNull(playerIdx)) {
            return null;
        }

        arenaEntity entity = getByIdx(playerIdx);
        if (entity == null && PlayerUtil.playerIsExist(playerIdx)) {
            entity = new arenaEntity(playerIdx);
            entity.putToCache();
        }
        return entity;
    }

    @Override
    public void updateDailyData() {
        for (BaseEntity value : _ix_id.values()) {
            if (!(value instanceof arenaEntity)) {
                return;
            }
            arenaEntity entity = (arenaEntity) value;
            SyncExecuteFunction.executeConsumer(entity, e -> entity.updateDailyData());
        }
    }

    public int getPlayerDan(String playerIdx) {
        arenaEntity entity = getEntity(playerIdx);
        if (entity == null) {
            return 0;
        }
        return entity.getDbBuilder().getDan();
    }

}

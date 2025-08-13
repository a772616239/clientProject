/*CREATED BY TOOL*/

package model.bosstower.dbCache;

import annotation.annationInit;
import common.GlobalData;
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
import model.bosstower.cache.bosstowerUpdateCache;
import model.bosstower.entity.bosstowerEntity;
import model.player.util.PlayerUtil;

@annationInit(value = "bosstowerCache", methodname = "load")
public class bosstowerCache extends baseCache<bosstowerCache> implements IbaseCache, UpdateDailyData {

    /******************* MUST HAVE ********************************/

    private static bosstowerCache instance = null;

    public static bosstowerCache getInstance() {

        if (instance == null) {
            instance = new bosstowerCache();
        }
        return instance;

    }


    public String getDaoName() {

        return "bosstowerDAO";
    }

    public BaseDAO getDao() {

        return AppContext.getBean("bosstowerDAO");
    }

    public void load(baseCache o) {

        if (instance == null) {
            instance = (bosstowerCache) o;
        }
        super.loadAllFromDb();

    }

    public static void put(bosstowerEntity v) {

        getInstance().putBaseEntity(v);

    }

    public static bosstowerEntity getByIdx(String idx) {
        if (StringHelper.isNull(idx))
            return null;


        BaseEntity v = getInstance().getBaseEntityByIdx(idx);
        if (v == null)
            return null;

        return (bosstowerEntity) v;

    }

    public Map<String, BaseEntity> getAll() {

        return getInstance()._ix_id;

    }

    public baseUpdateCache getUpdateCache() {

        return bosstowerUpdateCache.getInstance();

    }

    public static void remove(String idx) {

        getInstance().BaseRemove(idx);

    }


    public void putToMem(BaseEntity v) {

        bosstowerEntity t = (bosstowerEntity) v;

    }

    /*******************MUST HAVE END ********************************/

    public bosstowerEntity getEntity(String playerIdx) {
        bosstowerEntity entity = getByIdx(playerIdx);
        if (entity == null && PlayerUtil.playerIsExist(playerIdx)) {
            entity = new bosstowerEntity(playerIdx);
            entity.putToCache();
        }
        return entity;
    }

    @Override
    public void updateDailyData() {
        for (BaseEntity value : _ix_id.values()) {
            if (!(value instanceof bosstowerEntity)) {
                continue;
            }

            bosstowerEntity entity = (bosstowerEntity) value;
            SyncExecuteFunction.executeConsumer(entity,
                    v -> entity.updateDailyData(GlobalData.getInstance().checkPlayerOnline(entity.getPlayeridx())));
        }
    }

}

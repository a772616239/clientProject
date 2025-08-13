/*CREATED BY TOOL*/

package model.patrol.dbCache;

import annotation.annationInit;
import com.google.protobuf.InvalidProtocolBufferException;
import datatool.StringHelper;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;
import model.base.cache.IbaseCache;
import model.base.cache.baseCache;
import model.base.cache.baseUpdateCache;
import model.patrol.cache.patrolUpdateCache;
import model.patrol.entity.patrolEntity;
import util.LogUtil;

import java.util.Map;

@annationInit(value = "patrolCache", methodname = "load")
public class patrolCache extends baseCache<patrolCache> implements IbaseCache {
    /******************* MUST HAVE ********************************/

    private static patrolCache instance = null;

    public static patrolCache getInstance() {

        if (instance == null) {
            instance = new patrolCache();
        }
        return instance;

    }

    @Override
    public String getDaoName() {
        return "patrolDAO";
    }

    @Override
    public BaseDAO getDao() {
        return AppContext.getBean("patrolDAO");
    }

    public void load(baseCache o) {

        if (instance == null) {
            instance = (patrolCache) o;
        }
        super.loadAllFromDb();

    }

    public static void put(patrolEntity v) {

        getInstance().putBaseEntity(v);

    }

    public static patrolEntity getByIdx(String idx) {
        if (StringHelper.isNull(idx))
            return null;


        BaseEntity v = getInstance().getBaseEntityByIdx(idx);
        if (v == null)
            return null;

        return (patrolEntity) v;

    }

    @Override
    public Map<String, BaseEntity> getAll() {
        return getInstance()._ix_id;
    }

    @Override
    public baseUpdateCache getUpdateCache() {
        return patrolUpdateCache.getInstance();
    }

    public static void remove(String idx) {

        getInstance().BaseRemove(idx);

    }

    @Override
    public void putToMem(BaseEntity v) {
        patrolEntity entity = (patrolEntity) v;
        try {
            entity.toBuilder();
        } catch (InvalidProtocolBufferException e) {
            LogUtil.error(e.toString());
        }
    }

    /***************************分割**********************************/
    public patrolEntity getCacheByPlayer(String playerId) {
        for (BaseEntity value : _ix_id.values()) {
            patrolEntity entity = (patrolEntity) value;
            if (entity.getPlayeridx().equals(playerId)) {
                return entity;
            }
        }
        return null;
    }

    public void flush(patrolEntity entity) {
        entity.refresh();
        put(entity);
    }

/*******************MUST HAVE END ********************************/
}

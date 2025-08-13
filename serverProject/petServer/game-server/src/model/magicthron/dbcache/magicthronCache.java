/*CREATED BY TOOL*/

package model.magicthron.dbcache;

import java.util.Map;

import com.google.protobuf.InvalidProtocolBufferException;

import annotation.annationInit;
import common.SyncExecuteFunction;
import datatool.StringHelper;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;
import model.base.cache.IbaseCache;
import model.base.cache.baseCache;
import model.base.cache.baseUpdateCache;
import model.magicthron.cache.magicUpdateCache;
import model.magicthron.entity.magicthronEntity;
import util.LogUtil;

@annationInit(value = "magicthronDAO", methodname = "load")
public class magicthronCache extends baseCache<magicthronCache> implements IbaseCache {

    /******************* MUST HAVE ********************************/

    private static magicthronCache instance = null;

    public static magicthronCache getInstance() {

        if (instance == null) {
            instance = new magicthronCache();
        }
        return instance;

    }


    public String getDaoName() {

        return "magicthronDAO";
    }

    public BaseDAO getDao() {

        return AppContext.getBean("magicthronDAO");
    }

    public void load(baseCache o) {

        if (instance == null) {
            instance = (magicthronCache) o;
        }
        super.loadAllFromDb();

    }

    public static void put(magicthronEntity v) {

        getInstance().putBaseEntity(v);
    }

    public static magicthronEntity getByIdx(String idx) {
        if (StringHelper.isNull(idx))
            return null;


        BaseEntity v = getInstance().getBaseEntityByIdx(idx);
        if (v == null)
            return null;

        return (magicthronEntity) v;

    }

    public Map<String, BaseEntity> getAll() {

        return getInstance()._ix_id;

    }

    public baseUpdateCache getUpdateCache() {

        return magicUpdateCache.getInstance();

    }

    public static void remove(String idx) {

        getInstance().BaseRemove(idx);

    }


    public void putToMem(BaseEntity v) {
    	magicthronEntity entity = (magicthronEntity) v;
        _ix_id.put(entity.getIdx(),entity);
    }


    public void flush(magicthronEntity entity) {
        entity.refresh();
        put(entity);
    }
    
    /*******************MUST HAVE END ********************************/
    public void updateDailyData() {
    	Map<String, BaseEntity> all = getAll();
        for (BaseEntity entity : all.values()) {
            if (!(entity instanceof magicthronEntity)) {
                return;
            }
            try {
            	magicthronEntity magicEntity = (magicthronEntity) entity;
                SyncExecuteFunction.executeConsumer(magicEntity, e -> magicEntity.updateDailyData());
            } catch (Exception e) {
                LogUtil.error("magicthroncache.updateDailyData error by Id:[{}]", ((magicthronEntity) entity).getIdx());
                LogUtil.printStackTrace(e);
            }
        }
    }


    public void updateWeeklyData() {

        Map<String, BaseEntity> all = getAll();
        for (BaseEntity entity : all.values()) {
            if (!(entity instanceof magicthronEntity)) {
                return;
            }
            try {
                magicthronEntity magicEntity = (magicthronEntity) entity;
                SyncExecuteFunction.executeConsumer(magicEntity, e -> magicEntity.updateWeeklyData());
            } catch (Exception e) {
                LogUtil.error("magicthroncache.updateWeeklyData error by Id:[{}]", ((magicthronEntity) entity).getIdx());
                LogUtil.printStackTrace(e);
            }
        }
    }
}

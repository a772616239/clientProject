/*CREATED BY TOOL*/

package model.recentpassed.dbCache;

import annotation.annationInit;
import common.SyncExecuteFunction;
import datatool.StringHelper;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;
import java.util.Map;
import model.base.cache.IbaseCache;
import model.base.cache.baseCache;
import model.base.cache.baseUpdateCache;
import model.recentpassed.RecentPassedUtil;
import model.recentpassed.cache.recentpassedUpdateCache;
import model.recentpassed.entity.recentpassedEntity;
import org.apache.commons.lang.StringUtils;
import protocol.Common.EnumFunction;

@annationInit(value = "recentpassedCache", methodname = "load")
public class recentpassedCache extends baseCache<recentpassedCache> implements IbaseCache {

    /******************* MUST HAVE ********************************/

    private static recentpassedCache instance = null;

    public static recentpassedCache getInstance() {

        if (instance == null) {
            instance = new recentpassedCache();
        }
        return instance;

    }


    public String getDaoName() {

        return "recentpassedDAO";
    }

    public BaseDAO getDao() {

        return AppContext.getBean("recentpassedDAO");
    }

    public void load(baseCache o) {

        if (instance == null) {
            instance = (recentpassedCache) o;
        }
        super.loadAllFromDb();

    }

    public static void put(recentpassedEntity v) {

        getInstance().putBaseEntity(v);

    }

    public static recentpassedEntity getByIdx(String idx) {
        if (StringHelper.isNull(idx))
            return null;


        BaseEntity v = getInstance().getBaseEntityByIdx(idx);
        if (v == null)
            return null;

        return (recentpassedEntity) v;

    }

    public Map<String, BaseEntity> getAll() {

        return getInstance()._ix_id;

    }

    public baseUpdateCache getUpdateCache() {

        return recentpassedUpdateCache.getInstance();

    }

    public static void remove(String idx) {

        getInstance().BaseRemove(idx);

    }


    public void putToMem(BaseEntity v) {

        recentpassedEntity t = (recentpassedEntity) v;

    }

/*******************MUST HAVE END ********************************/

    public recentpassedEntity getEntity(EnumFunction function, int param) {
        String idx = RecentPassedUtil.buildIdx(function, param);
        if (idx == null) {
            return null;
        }

        recentpassedEntity entity = getByIdx(idx);
        if (entity == null) {
            entity = new recentpassedEntity();
            entity.setIdx(idx);
            put(entity);
        }
        return entity;
    }

    public void addRecentPassed(String playerIdx, EnumFunction function, int param) {
        if (StringUtils.isBlank(playerIdx)) {
            return;
        }

        recentpassedEntity entity = getEntity(function, param);
        if (entity == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            entity.addRecentPassed(playerIdx, function);
        });
    }
}

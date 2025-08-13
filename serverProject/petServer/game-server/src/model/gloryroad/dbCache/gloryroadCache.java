/*CREATED BY TOOL*/

package model.gloryroad.dbCache;

import common.SyncExecuteFunction;
import java.util.Map;
import model.base.cache.IbaseCache;
import model.base.cache.baseCache;
import model.base.cache.baseUpdateCache;
import model.gloryroad.cache.gloryroadUpdateCache;
import model.gloryroad.entity.gloryroadEntity;
import annotation.annationInit;
import datatool.StringHelper;
import db.entity.BaseEntity;
import db.core.BaseDAO;
import db.config.AppContext;
import model.player.util.PlayerUtil;
import util.LogUtil;

@annationInit(value = "gloryroadCache", methodname = "load")
public class gloryroadCache extends baseCache<gloryroadCache> implements IbaseCache {

    /******************* MUST HAVE ********************************/

    private static gloryroadCache instance = null;

    public static gloryroadCache getInstance() {

        if (instance == null) {
            instance = new gloryroadCache();
        }
        return instance;

    }


    public String getDaoName() {

        return "gloryroadDAO";
    }

    public BaseDAO getDao() {

        return AppContext.getBean("gloryroadDAO");
    }

    public void load(baseCache o) {

        if (instance == null) {
            instance = (gloryroadCache) o;
        }
        super.loadAllFromDb();

    }

    public static void put(gloryroadEntity v) {

        getInstance().putBaseEntity(v);

    }

    public static gloryroadEntity getByIdx(String idx) {
        if (StringHelper.isNull(idx))
            return null;


        BaseEntity v = getInstance().getBaseEntityByIdx(idx);
        if (v == null)
            return null;

        return (gloryroadEntity) v;

    }

    public Map<String, BaseEntity> getAll() {

        return getInstance()._ix_id;

    }

    public baseUpdateCache getUpdateCache() {

        return gloryroadUpdateCache.getInstance();

    }

    public static void remove(String idx) {

        getInstance().BaseRemove(idx);

    }


    public void putToMem(BaseEntity v) {

        gloryroadEntity t = (gloryroadEntity) v;

    }


    /*******************MUST HAVE END ********************************/

    public gloryroadEntity getEntity(String playerIdx) {
        gloryroadEntity entity = getByIdx(playerIdx);
        if (entity == null && PlayerUtil.playerIsExist(playerIdx)) {
            entity = new gloryroadEntity();
            entity.setPlayeridx(playerIdx);
            put(entity);
        }
        return entity;
    }

    public void clear() {
        for (BaseEntity value : _ix_id.values()) {
            if (!(value instanceof gloryroadEntity)) {
                continue;
            }

            gloryroadEntity entity = (gloryroadEntity) value;
            SyncExecuteFunction.executeConsumer(entity, e -> entity.clear());
        }
        LogUtil.info("gloryroadCache.clearAllRecord, finished clear all player records");
    }

    public int getPlayerJoinArenaRank(String playerIdx) {
        gloryroadEntity entity = getEntity(playerIdx);
        return entity == null ? 0 : entity.getDbBuilder().getJoinArenaRank();
    }

}

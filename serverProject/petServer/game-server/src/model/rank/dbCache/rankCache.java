/*CREATED BY TOOL*/

package model.rank.dbCache;

import annotation.annationInit;
import common.IdGenerator;
import datatool.StringHelper;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import model.base.cache.IbaseCache;
import model.base.cache.baseCache;
import model.base.cache.baseUpdateCache;
import model.rank.cache.rankUpdateCache;
import model.rank.entity.rankEntity;

@annationInit(value = "rankCache", methodname = "load")
public class rankCache extends baseCache<rankCache> implements IbaseCache {

    /******************* MUST HAVE ********************************/

    private static rankCache instance = null;

    public static rankCache getInstance() {

        if (instance == null) {
            instance = new rankCache();
        }
        return instance;

    }


    public String getDaoName() {

        return "rankDAO";
    }

    public BaseDAO getDao() {

        return AppContext.getBean("rankDAO");
    }

    public void load(baseCache o) {

        if (instance == null) {
            instance = (rankCache) o;
        }
        super.loadAllFromDb();

    }

    public static void put(rankEntity v) {
        getInstance().putBaseEntity(v);
    }

    public static rankEntity getByIdx(String idx) {
        if (StringHelper.isNull(idx))
            return null;


        BaseEntity v = getInstance().getBaseEntityByIdx(idx);
        if (v != null) {
            return (rankEntity) v;
        }


        rankEntity entity = new rankEntity(idx);
        entity.transformDBData();
        entity.putToCache();

        return entity;

    }

    public Map<String, BaseEntity> getAll() {

        return getInstance()._ix_id;

    }

    public baseUpdateCache getUpdateCache() {

        return rankUpdateCache.getInstance();

    }

    public static void remove(String idx) {

        getInstance().BaseRemove(idx);

    }


    public void putToMem(BaseEntity v) {
        if (v instanceof rankEntity) {
            rankEntity rankEntity = (rankEntity) v;
            rankMap.put(rankEntity.getDb_data().getRankId(), rankEntity);
        }
    }


    /*******************MUST HAVE END ********************************/
    private final Map<Integer, rankEntity> rankMap = new ConcurrentHashMap<>();


    public rankEntity getByRankTypeNumber(int rankTypeId) {
        rankEntity entity = rankMap.get(rankTypeId);

        if (entity != null) {
            return entity;
        }

        synchronized (this) {
            entity = rankMap.get(rankTypeId);

            if (entity != null) {
                return entity;
            }
            entity = new rankEntity();
            entity.setIdx(IdGenerator.getInstance().generateId());
            entity.getDb_data().setRankId(rankTypeId);
            entity.transformDBData();
            entity.putToCache();
        }
        return entity;
    }


}

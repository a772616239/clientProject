/*CREATED BY TOOL*/

package model.magicthron.cache;

import java.util.Map;

import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;
import model.cacheprocess.baseUapteCacheL;
import model.magicthron.dbcache.magicthronCache;

public class magicUpdateCacheL extends baseUapteCacheL {

    private static magicUpdateCacheL instance = null;

    public static magicUpdateCacheL getInstance() {

        if (instance == null) {
            instance = new magicUpdateCacheL();
        }
        return instance;

    }


    public Map<String, Boolean> getInsert() {

        return magicUpdateCache.getInstance().getInsert();

    }

    public Map<String, Boolean> getUpdate() {

        return magicUpdateCache.getInstance().getUpdate();

    }

    public Map<String, Boolean> getDel() {

        return magicUpdateCache.getInstance().getDel();

    }

    public BaseEntity getBaseEntity(String idx) {
        return magicthronCache.getByIdx(idx);

    }

    public BaseDAO getDao() {

        return AppContext.getBean("magicthronDAO");
    }

}

/*CREATED BY TOOL*/

package model.itembag.cache;

import java.util.Map;
import model.cacheprocess.baseUapteCacheL;
import model.itembag.dbCache.itembagCache;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;

public class itembagUpdateCacheL extends baseUapteCacheL {

    private static itembagUpdateCacheL instance = null;

    public static itembagUpdateCacheL getInstance() {

        if (instance == null) {
            instance = new itembagUpdateCacheL();
        }
        return instance;

    }


    public Map<String, Boolean> getInsert() {

        return itembagUpdateCache.getInstance().getInsert();

    }

    public Map<String, Boolean> getUpdate() {

        return itembagUpdateCache.getInstance().getUpdate();

    }

    public Map<String, Boolean> getDel() {

        return itembagUpdateCache.getInstance().getDel();

    }

    public BaseEntity getBaseEntity(String idx) {
        return itembagCache.getByIdx(idx);

    }

    public BaseDAO getDao() {

        return AppContext.getBean("itembagDAO");
    }

}

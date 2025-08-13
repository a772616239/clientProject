/*CREATED BY TOOL*/

package model.patrol.cache;

import java.util.Map;
import model.cacheprocess.baseUapteCacheL;
import model.patrol.dbCache.patrolCache;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;

public class patrolUpdateCacheL extends baseUapteCacheL {

    private static patrolUpdateCacheL instance = null;

    public static patrolUpdateCacheL getInstance() {

        if (instance == null) {
            instance = new patrolUpdateCacheL();
        }
        return instance;

    }


    public Map<String, Boolean> getInsert() {

        return patrolUpdateCache.getInstance().getInsert();

    }

    public Map<String, Boolean> getUpdate() {

        return patrolUpdateCache.getInstance().getUpdate();

    }

    public Map<String, Boolean> getDel() {

        return patrolUpdateCache.getInstance().getDel();

    }

    public BaseEntity getBaseEntity(String idx) {
        return patrolCache.getByIdx(idx);

    }

    public BaseDAO getDao() {

        return AppContext.getBean("patrolDAO");
    }

}

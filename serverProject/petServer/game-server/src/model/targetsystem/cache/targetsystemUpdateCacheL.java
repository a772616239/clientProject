/*CREATED BY TOOL*/

package model.targetsystem.cache;

import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;
import java.util.Map;
import model.cacheprocess.baseUapteCacheL;
import model.targetsystem.dbCache.targetsystemCache;

public class targetsystemUpdateCacheL extends baseUapteCacheL {

    private static targetsystemUpdateCacheL instance = null;

    public static targetsystemUpdateCacheL getInstance() {

        if (instance == null) {
            instance = new targetsystemUpdateCacheL();
        }
        return instance;

    }


    public Map<String, Boolean> getInsert() {

        return targetsystemUpdateCache.getInstance().getInsert();

    }

    public Map<String, Boolean> getUpdate() {

        return targetsystemUpdateCache.getInstance().getUpdate();

    }

    public Map<String, Boolean> getDel() {

        return targetsystemUpdateCache.getInstance().getDel();

    }

    public BaseEntity getBaseEntity(String idx) {
        return targetsystemCache.getByIdx(idx);

    }

    public BaseDAO getDao() {

        return AppContext.getBean("targetsystemDAO");
    }

}

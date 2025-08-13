/*CREATED BY TOOL*/

package model.mainLine.cache;

import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;
import java.util.Map;
import model.cacheprocess.baseUapteCacheL;
import model.mainLine.dbCache.mainlineCache;


public class mainlineUpdateCacheL extends baseUapteCacheL {

    private static mainlineUpdateCacheL instance = null;

    public static mainlineUpdateCacheL getInstance() {

        if (instance == null) {
            instance = new mainlineUpdateCacheL();
        }
        return instance;

    }


    public Map<String, Boolean> getInsert() {

        return mainlineUpdateCache.getInstance().getInsert();

    }

    public Map<String, Boolean> getUpdate() {

        return mainlineUpdateCache.getInstance().getUpdate();

    }

    public Map<String, Boolean> getDel() {

        return mainlineUpdateCache.getInstance().getDel();

    }

    public BaseEntity getBaseEntity(String idx) {
        return mainlineCache.getByIdx(idx);

    }

    public BaseDAO getDao() {

        return AppContext.getBean("mainlineDAO");
    }

}

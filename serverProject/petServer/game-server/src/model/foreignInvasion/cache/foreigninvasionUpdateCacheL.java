/*CREATED BY TOOL*/

package model.foreignInvasion.cache;

import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;
import java.util.Map;
import model.cacheprocess.baseUapteCacheL;
import model.foreignInvasion.dbCache.foreigninvasionCache;


public class foreigninvasionUpdateCacheL extends baseUapteCacheL {

    private static foreigninvasionUpdateCacheL instance = null;

    public static foreigninvasionUpdateCacheL getInstance() {

        if (instance == null) {
            instance = new foreigninvasionUpdateCacheL();
        }
        return instance;

    }


    public Map<String, Boolean> getInsert() {

        return foreigninvasionUpdateCache.getInstance().getInsert();

    }

    public Map<String, Boolean> getUpdate() {

        return foreigninvasionUpdateCache.getInstance().getUpdate();

    }

    public Map<String, Boolean> getDel() {

        return foreigninvasionUpdateCache.getInstance().getDel();

    }

    public BaseEntity getBaseEntity(String idx) {
        return foreigninvasionCache.getByIdx(idx);

    }

    public BaseDAO getDao() {

        return AppContext.getBean("foreigninvasionDAO");
    }

}

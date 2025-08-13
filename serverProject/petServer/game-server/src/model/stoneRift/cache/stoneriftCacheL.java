/*CREATED BY TOOL*/

package model.stoneRift.cache;

import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;
import java.util.Map;
import model.cacheprocess.baseUapteCacheL;
import model.stoneRift.dbCache.stoneriftCache;

public class stoneriftCacheL extends baseUapteCacheL {

    private static stoneriftCacheL instance = null;

    public static stoneriftCacheL getInstance() {

        if (instance == null) {
            instance = new stoneriftCacheL();
        }
        return instance;

    }


    public Map<String, Boolean> getInsert() {

        return stoneriftUpdateCache.getInstance().getInsert();

    }

    public Map<String, Boolean> getUpdate() {

        return stoneriftUpdateCache.getInstance().getUpdate();

    }

    public Map<String, Boolean> getDel() {

        return stoneriftUpdateCache.getInstance().getDel();

    }

    public BaseEntity getBaseEntity(String idx) {
        return stoneriftCache.getByIdx(idx);

    }

    public BaseDAO getDao() {

        return AppContext.getBean("stoneriftDAO");
    }

}

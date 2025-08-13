/*CREATED BY TOOL*/

package model.farmmine.cache;

import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;
import model.cacheprocess.baseUapteCacheL;
import model.farmmine.dbCache.farmmineCache;

import java.util.Map;

public class farmmineUpdateCacheL extends baseUapteCacheL {

    private static farmmineUpdateCacheL instance = null;

    public static farmmineUpdateCacheL getInstance() {

        if (instance == null) {
            instance = new farmmineUpdateCacheL();
        }
        return instance;

    }


    public Map<String, Boolean> getInsert() {

        return farmmineUpdateCache.getInstance().getInsert();

    }

    public Map<String, Boolean> getUpdate() {

        return farmmineUpdateCache.getInstance().getUpdate();

    }

    public Map<String, Boolean> getDel() {

        return farmmineUpdateCache.getInstance().getDel();

    }

    public BaseEntity getBaseEntity(String idx) {
        return farmmineCache.getByIdx(idx);

    }

    public BaseDAO getDao() {

        return AppContext.getBean("farmmineDAO");
    }

}

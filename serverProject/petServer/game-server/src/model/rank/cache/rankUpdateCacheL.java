/*CREATED BY TOOL*/

package model.rank.cache;

import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;
import model.cacheprocess.baseUapteCacheL;
import model.rank.dbCache.rankCache;

import java.util.Map;


public class rankUpdateCacheL extends baseUapteCacheL {

    private static rankUpdateCacheL instance = null;

    public static rankUpdateCacheL getInstance() {

        if (instance == null) {
            instance = new rankUpdateCacheL();
        }
        return instance;

    }


    public Map<String, Boolean> getInsert() {

        return rankUpdateCache.getInstance().getInsert();

    }

    public Map<String, Boolean> getUpdate() {

        return rankUpdateCache.getInstance().getUpdate();

    }

    public Map<String, Boolean> getDel() {

        return rankUpdateCache.getInstance().getDel();

    }

    public BaseEntity getBaseEntity(String idx) {
        return rankCache.getByIdx(idx);

    }

    public BaseDAO getDao() {

        return AppContext.getBean("rankDAO");
    }

}

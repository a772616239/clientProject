/*CREATED BY TOOL*/

package model.pet.cache;

import java.util.Map;
import model.cacheprocess.baseUapteCacheL;
import model.pet.dbCache.petCache;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;

public class petUpdateCacheL extends baseUapteCacheL {

    private static petUpdateCacheL instance = null;

    public static petUpdateCacheL getInstance() {

        if (instance == null) {
            instance = new petUpdateCacheL();
        }
        return instance;

    }


    public Map<String, Boolean> getInsert() {

        return petUpdateCache.getInstance().getInsert();

    }

    public Map<String, Boolean> getUpdate() {

        return petUpdateCache.getInstance().getUpdate();

    }

    public Map<String, Boolean> getDel() {

        return petUpdateCache.getInstance().getDel();

    }

    public BaseEntity getBaseEntity(String idx) {
        return petCache.getByIdx(idx);

    }

    public BaseDAO getDao() {

        return AppContext.getBean("petDAO");
    }

}

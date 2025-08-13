/*CREATED BY TOOL*/

package model.gameplay.cache;

import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;
import java.util.Map;
import model.cacheprocess.baseUapteCacheL;
import model.gameplay.dbCache.gameplayCache;

public class gameplayUpdateCacheL extends baseUapteCacheL {

    private static gameplayUpdateCacheL instance = null;

    public static gameplayUpdateCacheL getInstance() {

        if (instance == null) {
            instance = new gameplayUpdateCacheL();
        }
        return instance;

    }


    public Map<String, Boolean> getInsert() {

        return gameplayUpdateCache.getInstance().getInsert();

    }

    public Map<String, Boolean> getUpdate() {

        return gameplayUpdateCache.getInstance().getUpdate();

    }

    public Map<String, Boolean> getDel() {

        return gameplayUpdateCache.getInstance().getDel();

    }

    public BaseEntity getBaseEntity(String idx) {
        return gameplayCache.getByIdx(idx);

    }

    public BaseDAO getDao() {

        return AppContext.getBean("gameplayDAO");
    }

}

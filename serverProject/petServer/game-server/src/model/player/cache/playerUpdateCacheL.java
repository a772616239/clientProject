/*CREATED BY TOOL*/

package model.player.cache;

import java.util.Map;
import model.cacheprocess.baseUapteCacheL;
import model.player.dbCache.playerCache;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;


public class playerUpdateCacheL extends baseUapteCacheL {

    private static playerUpdateCacheL instance = null;

    public static playerUpdateCacheL getInstance() {

        if (instance == null) {
            instance = new playerUpdateCacheL();
        }
        return instance;

    }


    public Map<String, Boolean> getInsert() {

        return playerUpdateCache.getInstance().getInsert();

    }

    public Map<String, Boolean> getUpdate() {

        return playerUpdateCache.getInstance().getUpdate();

    }

    public Map<String, Boolean> getDel() {

        return playerUpdateCache.getInstance().getDel();

    }

    public BaseEntity getBaseEntity(String idx) {
        return playerCache.getByIdx(idx);

    }

    public BaseDAO getDao() {

        return AppContext.getBean("playerDAO");
    }

}

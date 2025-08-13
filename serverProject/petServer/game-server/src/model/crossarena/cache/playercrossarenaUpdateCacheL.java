/*CREATED BY TOOL*/

package model.crossarena.cache;

import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;
import model.cacheprocess.baseUapteCacheL;
import model.crossarena.dbCache.playercrossarenaCache;

import java.util.Map;

public class playercrossarenaUpdateCacheL extends baseUapteCacheL {

    private static playercrossarenaUpdateCacheL instance = null;

    public static playercrossarenaUpdateCacheL getInstance() {

        if (instance == null) {
            instance = new playercrossarenaUpdateCacheL();
        }
        return instance;

    }


    public Map<String, Boolean> getInsert() {

        return playercrossarenaUpdateCache.getInstance().getInsert();

    }

    public Map<String, Boolean> getUpdate() {

        return playercrossarenaUpdateCache.getInstance().getUpdate();

    }

    public Map<String, Boolean> getDel() {

        return playercrossarenaUpdateCache.getInstance().getDel();

    }

    public BaseEntity getBaseEntity(String idx) {
        return playercrossarenaCache.getByIdx(idx);

    }

    public BaseDAO getDao() {
        return AppContext.getBean("playercrossarenaDAO");
    }

}

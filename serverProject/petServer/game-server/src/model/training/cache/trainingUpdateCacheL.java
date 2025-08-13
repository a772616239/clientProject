/*CREATED BY TOOL*/

package model.training.cache;

import java.util.Map;

import model.cacheprocess.baseUapteCacheL;
import model.shop.dbCache.shopCache;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;
import model.training.dbCache.trainingCache;

public class trainingUpdateCacheL extends baseUapteCacheL {

    private static trainingUpdateCacheL instance = null;

    public static trainingUpdateCacheL getInstance() {

        if (instance == null) {
            instance = new trainingUpdateCacheL();
        }
        return instance;

    }


    public Map<String, Boolean> getInsert() {

        return trainingUpdateCache.getInstance().getInsert();

    }

    public Map<String, Boolean> getUpdate() {

        return trainingUpdateCache.getInstance().getUpdate();

    }

    public Map<String, Boolean> getDel() {

        return trainingUpdateCache.getInstance().getDel();

    }

    public BaseEntity getBaseEntity(String idx) {
        return trainingCache.getByIdx(idx);

    }

    public BaseDAO getDao() {

        return AppContext.getBean("trainingDAO");
    }

}

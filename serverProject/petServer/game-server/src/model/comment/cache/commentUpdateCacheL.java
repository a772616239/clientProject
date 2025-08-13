/*CREATED BY TOOL*/

package model.comment.cache;

import java.util.Map;
import model.cacheprocess.baseUapteCacheL;
import model.comment.dbCache.commentCache;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;

public class commentUpdateCacheL extends baseUapteCacheL {

    private static commentUpdateCacheL instance = null;

    public static commentUpdateCacheL getInstance() {

        if (instance == null) {
            instance = new commentUpdateCacheL();
        }
        return instance;

    }


    public Map<String, Boolean> getInsert() {

        return commentUpdateCache.getInstance().getInsert();

    }

    public Map<String, Boolean> getUpdate() {

        return commentUpdateCache.getInstance().getUpdate();

    }

    public Map<String, Boolean> getDel() {

        return commentUpdateCache.getInstance().getDel();

    }

    public BaseEntity getBaseEntity(String idx) {
        return commentCache.getByIdx(idx);

    }

    public BaseDAO getDao() {

        return AppContext.getBean("commentDAO");
    }

}

/*CREATED BY TOOL*/

package model.mailbox.cache;

import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;
import java.util.Map;
import model.cacheprocess.baseUapteCacheL;
import model.mailbox.dbCache.mailboxCache;

public class mailboxUpdateCacheL extends baseUapteCacheL {

    private static mailboxUpdateCacheL instance = null;

    public static mailboxUpdateCacheL getInstance() {

        if (instance == null) {
            instance = new mailboxUpdateCacheL();
        }
        return instance;

    }


    public Map<String, Boolean> getInsert() {

        return mailboxUpdateCache.getInstance().getInsert();

    }

    public Map<String, Boolean> getUpdate() {

        return mailboxUpdateCache.getInstance().getUpdate();

    }

    public Map<String, Boolean> getDel() {

        return mailboxUpdateCache.getInstance().getDel();

    }

    public BaseEntity getBaseEntity(String idx) {
        return mailboxCache.getByIdx(idx);

    }

    public BaseDAO getDao() {

        return AppContext.getBean("mailboxDAO");
    }

}

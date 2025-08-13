/*CREATED BY TOOL*/

package model.recentpassed.cache;
import java.util.Map;
import model.cacheprocess.baseUapteCacheL;
import model.recentpassed.dbCache.recentpassedCache;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;

public class recentpassedUpdateCacheL extends baseUapteCacheL{

private static recentpassedUpdateCacheL instance =null;

public static recentpassedUpdateCacheL getInstance() {

if (instance==null) {
instance=new recentpassedUpdateCacheL();
}
return instance;

}


public Map<String, Boolean> getInsert() {

return recentpassedUpdateCache.getInstance().getInsert();

}
public Map<String, Boolean> getUpdate() {

return recentpassedUpdateCache.getInstance().getUpdate();

}
public Map<String, Boolean> getDel() {

return recentpassedUpdateCache.getInstance().getDel();

}

public BaseEntity getBaseEntity(String idx) {
return recentpassedCache.getByIdx(idx);

}
public BaseDAO getDao() {

return AppContext.getBean("recentpassedDAO");
}

}

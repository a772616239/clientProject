/*CREATED BY TOOL*/

package model.bosstower.cache;
import java.util.Map;
import model.cacheprocess.baseUapteCacheL;
import model.bosstower.dbCache.bosstowerCache;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;

public class bosstowerUpdateCacheL extends baseUapteCacheL{

private static bosstowerUpdateCacheL instance =null;

public static bosstowerUpdateCacheL getInstance() {

if (instance==null) {
instance=new bosstowerUpdateCacheL();
}
return instance;

}


public Map<String, Boolean> getInsert() {

return bosstowerUpdateCache.getInstance().getInsert();

}
public Map<String, Boolean> getUpdate() {

return bosstowerUpdateCache.getInstance().getUpdate();

}
public Map<String, Boolean> getDel() {

return bosstowerUpdateCache.getInstance().getDel();

}

public BaseEntity getBaseEntity(String idx) {
return bosstowerCache.getByIdx(idx);

}
public BaseDAO getDao() {

return AppContext.getBean("bosstowerDAO");
}

}

/*CREATED BY TOOL*/

package model.petrune.cache;
import java.util.Map;
import model.cacheprocess.baseUapteCacheL;
import model.petrune.dbCache.petruneCache;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;

public class petruneUpdateCacheL extends baseUapteCacheL{

private static petruneUpdateCacheL instance =null;

public static petruneUpdateCacheL getInstance() {

if (instance==null) {
instance=new petruneUpdateCacheL();
}
return instance;

}


public Map<String, Boolean> getInsert() {

return petruneUpdateCache.getInstance().getInsert();

}
public Map<String, Boolean> getUpdate() {

return petruneUpdateCache.getInstance().getUpdate();

}
public Map<String, Boolean> getDel() {

return petruneUpdateCache.getInstance().getDel();

}

public BaseEntity getBaseEntity(String idx) {
return petruneCache.getByIdx(idx);

}
public BaseDAO getDao() {

return AppContext.getBean("petruneDAO");
}

}

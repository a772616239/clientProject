/*CREATED BY TOOL*/

package model.petfragment.cache;
import java.util.Map;
import model.cacheprocess.baseUapteCacheL;
import model.petfragment.dbCache.petfragmentCache;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;

public class petfragmentUpdateCacheL extends baseUapteCacheL{

private static petfragmentUpdateCacheL instance =null;

public static petfragmentUpdateCacheL getInstance() {

if (instance==null) {
instance=new petfragmentUpdateCacheL();
}
return instance;

}


public Map<String, Boolean> getInsert() {

return petfragmentUpdateCache.getInstance().getInsert();

}
public Map<String, Boolean> getUpdate() {

return petfragmentUpdateCache.getInstance().getUpdate();

}
public Map<String, Boolean> getDel() {

return petfragmentUpdateCache.getInstance().getDel();

}

public BaseEntity getBaseEntity(String idx) {
return petfragmentCache.getByIdx(idx);

}
public BaseDAO getDao() {

return AppContext.getBean("petfragmentDAO");
}

}

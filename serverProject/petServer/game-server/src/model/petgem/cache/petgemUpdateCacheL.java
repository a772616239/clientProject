/*CREATED BY TOOL*/

package model.petgem.cache;

import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;
import model.cacheprocess.baseUapteCacheL;
import model.petgem.dbCache.petgemCache;

import java.util.Map;

public class petgemUpdateCacheL extends baseUapteCacheL{

private static petgemUpdateCacheL instance =null;

public static petgemUpdateCacheL getInstance() {

if (instance==null) {
instance=new petgemUpdateCacheL();
}
return instance;

}


public Map<String, Boolean> getInsert() {

return petgemUpdateCache.getInstance().getInsert();

}
public Map<String, Boolean> getUpdate() {

return petgemUpdateCache.getInstance().getUpdate();

}
public Map<String, Boolean> getDel() {

return petgemUpdateCache.getInstance().getDel();

}

public BaseEntity getBaseEntity(String idx) {
return petgemCache.getByIdx(idx);

}
public BaseDAO getDao() {

return AppContext.getBean("petgemDAO");
}

}

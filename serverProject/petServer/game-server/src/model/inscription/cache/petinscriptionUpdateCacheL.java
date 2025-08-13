/*CREATED BY TOOL*/

package model.inscription.cache;

import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;
import java.util.Map;
import model.cacheprocess.baseUapteCacheL;
import model.inscription.dbCache.petinscriptionCache;

public class petinscriptionUpdateCacheL extends baseUapteCacheL{

private static petinscriptionUpdateCacheL instance =null;

public static petinscriptionUpdateCacheL getInstance() {

if (instance==null) {
instance=new petinscriptionUpdateCacheL();
}
return instance;

}


public Map<String, Boolean> getInsert() {

return petinscriptionUpdateCache.getInstance().getInsert();

}
public Map<String, Boolean> getUpdate() {

return petinscriptionUpdateCache.getInstance().getUpdate();

}
public Map<String, Boolean> getDel() {

return petinscriptionUpdateCache.getInstance().getDel();

}

public BaseEntity getBaseEntity(String idx) {
return petinscriptionCache.getByIdx(idx);

}
public BaseDAO getDao() {

return AppContext.getBean("petinscriptionDAO");
}

}

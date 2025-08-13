/*CREATED BY TOOL*/

package model.petmission.cache;
import java.util.Map;
import model.cacheprocess.baseUapteCacheL;
import model.petmission.dbCache.petmissionCache;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;

public class petmissionUpdateCacheL extends baseUapteCacheL{

private static petmissionUpdateCacheL instance =null;

public static petmissionUpdateCacheL getInstance() {

if (instance==null) {
instance=new petmissionUpdateCacheL();
}
return instance;

}


public Map<String, Boolean> getInsert() {

return petmissionUpdateCache.getInstance().getInsert();

}
public Map<String, Boolean> getUpdate() {

return petmissionUpdateCache.getInstance().getUpdate();

}
public Map<String, Boolean> getDel() {

return petmissionUpdateCache.getInstance().getDel();

}

public BaseEntity getBaseEntity(String idx) {
return petmissionCache.getByIdx(idx);

}
public BaseDAO getDao() {

return AppContext.getBean("petmissionDAO");
}

}

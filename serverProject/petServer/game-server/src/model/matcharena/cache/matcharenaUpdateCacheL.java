/*CREATED BY TOOL*/

package model.matcharena.cache;
import java.util.Map;
import model.cacheprocess.baseUapteCacheL;
import model.matcharena.dbCache.matcharenaCache;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;

public class matcharenaUpdateCacheL extends baseUapteCacheL{

private static matcharenaUpdateCacheL instance =null;

public static matcharenaUpdateCacheL getInstance() {

if (instance==null) {
instance=new matcharenaUpdateCacheL();
}
return instance;

}


public Map<String, Boolean> getInsert() {

return matcharenaUpdateCache.getInstance().getInsert();

}
public Map<String, Boolean> getUpdate() {

return matcharenaUpdateCache.getInstance().getUpdate();

}
public Map<String, Boolean> getDel() {

return matcharenaUpdateCache.getInstance().getDel();

}

public BaseEntity getBaseEntity(String idx) {
return matcharenaCache.getByIdx(idx);

}
public BaseDAO getDao() {

return AppContext.getBean("matcharenaDAO");
}

}

/*CREATED BY TOOL*/

package model.gloryroad.cache;
import java.util.Map;
import model.cacheprocess.baseUapteCacheL;
import model.gloryroad.dbCache.gloryroadCache;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;

public class gloryroadUpdateCacheL extends baseUapteCacheL{

private static gloryroadUpdateCacheL instance =null;

public static gloryroadUpdateCacheL getInstance() {

if (instance==null) {
instance=new gloryroadUpdateCacheL();
}
return instance;

}


public Map<String, Boolean> getInsert() {

return gloryroadUpdateCache.getInstance().getInsert();

}
public Map<String, Boolean> getUpdate() {

return gloryroadUpdateCache.getInstance().getUpdate();

}
public Map<String, Boolean> getDel() {

return gloryroadUpdateCache.getInstance().getDel();

}

public BaseEntity getBaseEntity(String idx) {
return gloryroadCache.getByIdx(idx);

}
public BaseDAO getDao() {

return AppContext.getBean("gloryroadDAO");
}

}

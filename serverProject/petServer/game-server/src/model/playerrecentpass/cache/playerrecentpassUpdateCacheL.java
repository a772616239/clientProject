/*CREATED BY TOOL*/

package model.playerrecentpass.cache;
import java.util.Map;
import model.cacheprocess.baseUapteCacheL;
import model.playerrecentpass.dbCache.playerrecentpassCache;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;

public class playerrecentpassUpdateCacheL extends baseUapteCacheL{

private static playerrecentpassUpdateCacheL instance =null;

public static playerrecentpassUpdateCacheL getInstance() {

if (instance==null) {
instance=new playerrecentpassUpdateCacheL();
}
return instance;

}


public Map<String, Boolean> getInsert() {

return playerrecentpassUpdateCache.getInstance().getInsert();

}
public Map<String, Boolean> getUpdate() {

return playerrecentpassUpdateCache.getInstance().getUpdate();

}
public Map<String, Boolean> getDel() {

return playerrecentpassUpdateCache.getInstance().getDel();

}

public BaseEntity getBaseEntity(String idx) {
return playerrecentpassCache.getByIdx(idx);

}
public BaseDAO getDao() {

return AppContext.getBean("playerrecentpassDAO");
}

}

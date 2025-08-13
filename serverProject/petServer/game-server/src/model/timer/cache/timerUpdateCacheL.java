/*CREATED BY TOOL*/

package model.timer.cache;
import java.util.Map;
import model.cacheprocess.baseUapteCacheL;
import model.timer.dbCache.timerCache;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;

public class timerUpdateCacheL extends baseUapteCacheL{

private static timerUpdateCacheL instance =null;

public static timerUpdateCacheL getInstance() {

if (instance==null) {
instance=new timerUpdateCacheL();
}
return instance;

}


public Map<String, Boolean> getInsert() {

return timerUpdateCache.getInstance().getInsert();

}
public Map<String, Boolean> getUpdate() {

return timerUpdateCache.getInstance().getUpdate();

}
public Map<String, Boolean> getDel() {

return timerUpdateCache.getInstance().getDel();

}

public BaseEntity getBaseEntity(String idx) {
return timerCache.getByIdx(idx);

}
public BaseDAO getDao() {

return AppContext.getBean("timerDAO");
}

}

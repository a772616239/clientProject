/*CREATED BY TOOL*/

package model.arena.cache;
import java.util.Map;
import model.cacheprocess.baseUapteCacheL;
import model.arena.dbCache.arenaCache;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;

public class arenaUpdateCacheL extends baseUapteCacheL{

private static arenaUpdateCacheL instance =null;

public static arenaUpdateCacheL getInstance() {

if (instance==null) {
instance=new arenaUpdateCacheL();
}
return instance;

}


public Map<String, Boolean> getInsert() {

return arenaUpdateCache.getInstance().getInsert();

}
public Map<String, Boolean> getUpdate() {

return arenaUpdateCache.getInstance().getUpdate();

}
public Map<String, Boolean> getDel() {

return arenaUpdateCache.getInstance().getDel();

}

public BaseEntity getBaseEntity(String idx) {
return arenaCache.getByIdx(idx);

}
public BaseDAO getDao() {

return AppContext.getBean("arenaDAO");
}

}

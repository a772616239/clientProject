/*CREATED BY TOOL*/

package model.team.cache;
import java.util.Map;
import model.cacheprocess.baseUapteCacheL;
import model.team.dbCache.teamCache;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;

public class teamUpdateCacheL extends baseUapteCacheL{

private static teamUpdateCacheL instance =null;

public static teamUpdateCacheL getInstance() {

if (instance==null) {
instance=new teamUpdateCacheL();
}
return instance;

}


public Map<String, Boolean> getInsert() {

return teamUpdateCache.getInstance().getInsert();

}
public Map<String, Boolean> getUpdate() {

return teamUpdateCache.getInstance().getUpdate();

}
public Map<String, Boolean> getDel() {

return teamUpdateCache.getInstance().getDel();

}

public BaseEntity getBaseEntity(String idx) {
return teamCache.getByIdx(idx);

}
public BaseDAO getDao() {

return AppContext.getBean("teamDAO");
}

}

/*CREATED BY TOOL*/

package model.bravechallenge.cache;
import java.util.Map;
import model.cacheprocess.baseUapteCacheL;
import model.bravechallenge.dbCache.bravechallengeCache;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;

public class bravechallengeUpdateCacheL extends baseUapteCacheL{

private static bravechallengeUpdateCacheL instance =null;

public static bravechallengeUpdateCacheL getInstance() {

if (instance==null) {
instance=new bravechallengeUpdateCacheL();
}
return instance;

}


public Map<String, Boolean> getInsert() {

return bravechallengeUpdateCache.getInstance().getInsert();

}
public Map<String, Boolean> getUpdate() {

return bravechallengeUpdateCache.getInstance().getUpdate();

}
public Map<String, Boolean> getDel() {

return bravechallengeUpdateCache.getInstance().getDel();

}

public BaseEntity getBaseEntity(String idx) {
return bravechallengeCache.getByIdx(idx);

}
public BaseDAO getDao() {

return AppContext.getBean("bravechallengeDAO");
}

}

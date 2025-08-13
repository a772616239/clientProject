/*CREATED BY TOOL*/

package model.battlerecord.cache;
import java.util.Map;
import model.cacheprocess.baseUapteCacheL;
import model.battlerecord.dbCache.battlerecordCache;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;

public class battlerecordUpdateCacheL extends baseUapteCacheL{

private static battlerecordUpdateCacheL instance =null;

public static battlerecordUpdateCacheL getInstance() {

if (instance==null) {
instance=new battlerecordUpdateCacheL();
}
return instance;

}


public Map<String, Boolean> getInsert() {

return battlerecordUpdateCache.getInstance().getInsert();

}
public Map<String, Boolean> getUpdate() {

return battlerecordUpdateCache.getInstance().getUpdate();

}
public Map<String, Boolean> getDel() {

return battlerecordUpdateCache.getInstance().getDel();

}

public BaseEntity getBaseEntity(String idx) {
return battlerecordCache.getByIdx(idx);

}
public BaseDAO getDao() {

return AppContext.getBean("battlerecordDAO");
}

}

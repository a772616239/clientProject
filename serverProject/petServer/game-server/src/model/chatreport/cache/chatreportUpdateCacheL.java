/*CREATED BY TOOL*/

package model.chatreport.cache;
import java.util.Map;
import model.cacheprocess.baseUapteCacheL;
import model.chatreport.dbCache.chatreportCache;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;

public class chatreportUpdateCacheL extends baseUapteCacheL{

private static chatreportUpdateCacheL instance =null;

public static chatreportUpdateCacheL getInstance() {

if (instance==null) {
instance=new chatreportUpdateCacheL();
}
return instance;

}


public Map<String, Boolean> getInsert() {

return chatreportUpdateCache.getInstance().getInsert();

}
public Map<String, Boolean> getUpdate() {

return chatreportUpdateCache.getInstance().getUpdate();

}
public Map<String, Boolean> getDel() {

return chatreportUpdateCache.getInstance().getDel();

}

public BaseEntity getBaseEntity(String idx) {
return chatreportCache.getByIdx(idx);

}
public BaseDAO getDao() {

return AppContext.getBean("chatreportDAO");
}

}

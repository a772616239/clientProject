/*CREATED BY TOOL*/

package model.exchangehistory.cache;
import java.util.Map;
import model.cacheprocess.baseUapteCacheL;
import model.exchangehistory.dbCache.exchangehistoryCache;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;

public class exchangehistoryUpdateCacheL extends baseUapteCacheL{

private static exchangehistoryUpdateCacheL instance =null;

public static exchangehistoryUpdateCacheL getInstance() {

if (instance==null) {
instance=new exchangehistoryUpdateCacheL();
}
return instance;

}


public Map<String, Boolean> getInsert() {

return exchangehistoryUpdateCache.getInstance().getInsert();

}
public Map<String, Boolean> getUpdate() {

return exchangehistoryUpdateCache.getInstance().getUpdate();

}
public Map<String, Boolean> getDel() {

return exchangehistoryUpdateCache.getInstance().getDel();

}

public BaseEntity getBaseEntity(String idx) {
return exchangehistoryCache.getByIdx(idx);

}
public BaseDAO getDao() {

return AppContext.getBean("exchangehistoryDAO");
}

}

/*CREATED BY TOOL*/

package model.shop.cache;
import java.util.Map;
import model.cacheprocess.baseUapteCacheL;
import model.shop.dbCache.shopCache;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;

public class shopUpdateCacheL extends baseUapteCacheL{

private static shopUpdateCacheL instance =null;

public static shopUpdateCacheL getInstance() {

if (instance==null) {
instance=new shopUpdateCacheL();
}
return instance;

}


public Map<String, Boolean> getInsert() {

return shopUpdateCache.getInstance().getInsert();

}
public Map<String, Boolean> getUpdate() {

return shopUpdateCache.getInstance().getUpdate();

}
public Map<String, Boolean> getDel() {

return shopUpdateCache.getInstance().getDel();

}

public BaseEntity getBaseEntity(String idx) {
return shopCache.getByIdx(idx);

}
public BaseDAO getDao() {

return AppContext.getBean("shopDAO");
}

}

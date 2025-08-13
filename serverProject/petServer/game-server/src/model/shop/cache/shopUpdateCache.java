/*CREATED BY TOOL*/

package model.shop.cache;
import model.base.cache.baseUpdateCache;

public class shopUpdateCache extends baseUpdateCache<shopUpdateCache>{

private static shopUpdateCache instance =null;

public static shopUpdateCache getInstance() {

if (instance==null) {
instance=new shopUpdateCache();
}
return instance;

}


}

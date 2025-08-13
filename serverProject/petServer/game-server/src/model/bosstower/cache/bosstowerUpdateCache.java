/*CREATED BY TOOL*/

package model.bosstower.cache;
import model.base.cache.baseUpdateCache;

public class bosstowerUpdateCache extends baseUpdateCache<bosstowerUpdateCache>{

private static bosstowerUpdateCache instance =null;

public static bosstowerUpdateCache getInstance() {

if (instance==null) {
instance=new bosstowerUpdateCache();
}
return instance;

}


}

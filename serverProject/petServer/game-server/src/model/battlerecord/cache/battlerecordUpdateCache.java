/*CREATED BY TOOL*/

package model.battlerecord.cache;
import model.base.cache.baseUpdateCache;

public class battlerecordUpdateCache extends baseUpdateCache<battlerecordUpdateCache>{

private static battlerecordUpdateCache instance =null;

public static battlerecordUpdateCache getInstance() {

if (instance==null) {
instance=new battlerecordUpdateCache();
}
return instance;

}


}

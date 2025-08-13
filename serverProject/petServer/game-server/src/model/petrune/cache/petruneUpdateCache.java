/*CREATED BY TOOL*/

package model.petrune.cache;
import model.base.cache.baseUpdateCache;

public class petruneUpdateCache extends baseUpdateCache<petruneUpdateCache>{

private static petruneUpdateCache instance =null;

public static petruneUpdateCache getInstance() {

if (instance==null) {
instance=new petruneUpdateCache();
}
return instance;

}


}

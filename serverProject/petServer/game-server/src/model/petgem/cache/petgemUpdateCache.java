/*CREATED BY TOOL*/

package model.petgem.cache;
import model.base.cache.baseUpdateCache;

public class petgemUpdateCache extends baseUpdateCache<petgemUpdateCache>{

private static petgemUpdateCache instance =null;

public static petgemUpdateCache getInstance() {

if (instance==null) {
instance=new petgemUpdateCache();
}
return instance;

}


}

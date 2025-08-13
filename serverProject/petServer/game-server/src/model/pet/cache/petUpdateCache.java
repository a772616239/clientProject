/*CREATED BY TOOL*/

package model.pet.cache;
import model.base.cache.baseUpdateCache;

public class petUpdateCache extends baseUpdateCache<petUpdateCache>{

private static petUpdateCache instance =null;

public static petUpdateCache getInstance() {

if (instance==null) {
instance=new petUpdateCache();
}
return instance;

}


}

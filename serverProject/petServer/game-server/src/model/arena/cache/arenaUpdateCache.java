/*CREATED BY TOOL*/

package model.arena.cache;
import model.base.cache.baseUpdateCache;

public class arenaUpdateCache extends baseUpdateCache<arenaUpdateCache>{

private static arenaUpdateCache instance =null;

public static arenaUpdateCache getInstance() {

if (instance==null) {
instance=new arenaUpdateCache();
}
return instance;

}


}

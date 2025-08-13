/*CREATED BY TOOL*/

package model.recentpassed.cache;
import model.base.cache.baseUpdateCache;

public class recentpassedUpdateCache extends baseUpdateCache<recentpassedUpdateCache>{

private static recentpassedUpdateCache instance =null;

public static recentpassedUpdateCache getInstance() {

if (instance==null) {
instance=new recentpassedUpdateCache();
}
return instance;

}


}

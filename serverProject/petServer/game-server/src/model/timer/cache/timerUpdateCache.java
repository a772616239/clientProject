/*CREATED BY TOOL*/

package model.timer.cache;
import model.base.cache.baseUpdateCache;

public class timerUpdateCache extends baseUpdateCache<timerUpdateCache>{

private static timerUpdateCache instance =null;

public static timerUpdateCache getInstance() {

if (instance==null) {
instance=new timerUpdateCache();
}
return instance;

}


}

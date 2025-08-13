/*CREATED BY TOOL*/

package model.chatreport.cache;
import model.base.cache.baseUpdateCache;

public class chatreportUpdateCache extends baseUpdateCache<chatreportUpdateCache>{

private static chatreportUpdateCache instance =null;

public static chatreportUpdateCache getInstance() {

if (instance==null) {
instance=new chatreportUpdateCache();
}
return instance;

}


}

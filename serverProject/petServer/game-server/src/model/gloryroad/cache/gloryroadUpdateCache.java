/*CREATED BY TOOL*/

package model.gloryroad.cache;
import model.base.cache.baseUpdateCache;

public class gloryroadUpdateCache extends baseUpdateCache<gloryroadUpdateCache>{

private static gloryroadUpdateCache instance =null;

public static gloryroadUpdateCache getInstance() {

if (instance==null) {
instance=new gloryroadUpdateCache();
}
return instance;

}


}

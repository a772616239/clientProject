/*CREATED BY TOOL*/

package model.stoneRift.cache;
import model.base.cache.baseUpdateCache;

public class stoneriftUpdateCache extends baseUpdateCache<stoneriftUpdateCache>{

private static stoneriftUpdateCache instance =null;

public static stoneriftUpdateCache getInstance() {

if (instance==null) {
instance=new stoneriftUpdateCache();
}
return instance;

}


}

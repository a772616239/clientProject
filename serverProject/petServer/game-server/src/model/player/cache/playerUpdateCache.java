/*CREATED BY TOOL*/

package model.player.cache;
import model.base.cache.baseUpdateCache;

public class playerUpdateCache extends baseUpdateCache<playerUpdateCache>{

private static playerUpdateCache instance =null;

public static playerUpdateCache getInstance() {

if (instance==null) {
instance=new playerUpdateCache();
}
return instance;

}


}

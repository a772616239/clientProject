/*CREATED BY TOOL*/

package model.playerrecentpass.cache;
import model.base.cache.baseUpdateCache;

public class playerrecentpassUpdateCache extends baseUpdateCache<playerrecentpassUpdateCache>{

private static playerrecentpassUpdateCache instance =null;

public static playerrecentpassUpdateCache getInstance() {

if (instance==null) {
instance=new playerrecentpassUpdateCache();
}
return instance;

}


}

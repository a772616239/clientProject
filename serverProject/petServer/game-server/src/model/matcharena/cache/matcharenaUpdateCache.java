/*CREATED BY TOOL*/

package model.matcharena.cache;
import model.base.cache.baseUpdateCache;

public class matcharenaUpdateCache extends baseUpdateCache<matcharenaUpdateCache>{

private static matcharenaUpdateCache instance =null;

public static matcharenaUpdateCache getInstance() {

if (instance==null) {
instance=new matcharenaUpdateCache();
}
return instance;

}


}

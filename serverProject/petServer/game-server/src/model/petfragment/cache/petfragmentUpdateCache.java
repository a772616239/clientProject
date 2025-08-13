/*CREATED BY TOOL*/

package model.petfragment.cache;
import model.base.cache.baseUpdateCache;

public class petfragmentUpdateCache extends baseUpdateCache<petfragmentUpdateCache>{

private static petfragmentUpdateCache instance =null;

public static petfragmentUpdateCache getInstance() {

if (instance==null) {
instance=new petfragmentUpdateCache();
}
return instance;

}


}

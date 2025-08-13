/*CREATED BY TOOL*/

package model.inscription.cache;
import model.base.cache.baseUpdateCache;

public class petinscriptionUpdateCache extends baseUpdateCache<petinscriptionUpdateCache>{

private static petinscriptionUpdateCache instance =null;

public static petinscriptionUpdateCache getInstance() {

if (instance==null) {
instance=new petinscriptionUpdateCache();
}
return instance;

}


}

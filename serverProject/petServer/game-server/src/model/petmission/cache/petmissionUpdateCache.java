/*CREATED BY TOOL*/

package model.petmission.cache;
import model.base.cache.baseUpdateCache;

public class petmissionUpdateCache extends baseUpdateCache<petmissionUpdateCache>{

private static petmissionUpdateCache instance =null;

public static petmissionUpdateCache getInstance() {

if (instance==null) {
instance=new petmissionUpdateCache();
}
return instance;

}


}

/*CREATED BY TOOL*/

package model.team.cache;
import model.base.cache.baseUpdateCache;

public class teamUpdateCache extends baseUpdateCache<teamUpdateCache>{

private static teamUpdateCache instance =null;

public static teamUpdateCache getInstance() {

if (instance==null) {
instance=new teamUpdateCache();
}
return instance;

}


}

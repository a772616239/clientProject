/*CREATED BY TOOL*/

package model.bravechallenge.cache;
import model.base.cache.baseUpdateCache;

public class bravechallengeUpdateCache extends baseUpdateCache<bravechallengeUpdateCache>{

private static bravechallengeUpdateCache instance =null;

public static bravechallengeUpdateCache getInstance() {

if (instance==null) {
instance=new bravechallengeUpdateCache();
}
return instance;

}


}

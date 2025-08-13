/*CREATED BY TOOL*/

package model.mailbox.cache;
import model.base.cache.baseUpdateCache;

public class mailboxUpdateCache extends baseUpdateCache<mailboxUpdateCache>{

private static mailboxUpdateCache instance =null;

public static mailboxUpdateCache getInstance() {

if (instance==null) {
instance=new mailboxUpdateCache();
}
return instance;

}


}

/*CREATED BY TOOL*/

package model.exchangehistory.cache;
import model.base.cache.baseUpdateCache;

public class exchangehistoryUpdateCache extends baseUpdateCache<exchangehistoryUpdateCache>{

private static exchangehistoryUpdateCache instance =null;

public static exchangehistoryUpdateCache getInstance() {

if (instance==null) {
instance=new exchangehistoryUpdateCache();
}
return instance;

}


}

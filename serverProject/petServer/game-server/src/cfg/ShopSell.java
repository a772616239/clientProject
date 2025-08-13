/*CREATED BY TOOL*/

package cfg;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import model.base.baseConfig;
import datatool.MapHelper;
import annotation.annationInit;
import common.load.ServerConfig;
import JsonTool.readJsonFile;

@annationInit(value ="ShopSell", methodname = "initConfig")
public class ShopSell extends baseConfig<ShopSellObject>{


private static ShopSell instance = null;

public static ShopSell getInstance() {

if (instance == null)
instance = new ShopSell();
return instance;

}


public static Map<Integer, ShopSellObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (ShopSell) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"ShopSell");

for(Map e:ret)
{
put(e);
}

}

public static ShopSellObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, ShopSellObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setShopid(MapHelper.getInt(e, "shopId"));

config.setServername(MapHelper.getStr(e, "serverName"));

config.setCargo(MapHelper.getInts(e, "cargo"));

config.setPrice(MapHelper.getInts(e, "price"));

config.setBuylimit(MapHelper.getInt(e, "buyLimit"));

config.setVipexp(MapHelper.getInt(e, "vipExp"));

config.setSellgroup(MapHelper.getInt(e, "sellGroup"));

config.setAppearrate(MapHelper.getInt(e, "appearRate"));

config.setSpecialtype(MapHelper.getInt(e, "specialType"));

config.setSpecialparam(MapHelper.getInts(e, "specialParam"));

config.setSelling(MapHelper.getBoolean(e, "selling"));

config.setUnlockcondtion(MapHelper.getInt(e, "unlockCondtion"));

config.setDiscounttype(MapHelper.getInt(e, "discountType"));

config.setHonrlv(MapHelper.getInt(e, "honrLv"));

config.setShowviplv(MapHelper.getInt(e, "showVipLv"));


_ix_id.put(config.getId(),config);



}
}

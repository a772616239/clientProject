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

@annationInit(value ="TrainingShop", methodname = "initConfig")
public class TrainingShop extends baseConfig<TrainingShopObject>{


private static TrainingShop instance = null;

public static TrainingShop getInstance() {

if (instance == null)
instance = new TrainingShop();
return instance;

}


public static Map<Integer, TrainingShopObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (TrainingShop) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"TrainingShop");

for(Map e:ret)
{
put(e);
}

}

public static TrainingShopObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, TrainingShopObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setGroup(MapHelper.getInt(e, "group"));

config.setLevel(MapHelper.getInt(e, "level"));

config.setItemid(MapHelper.getInt(e, "itemId"));

config.setPrice(MapHelper.getIntArray(e, "price"));

config.setLimit(MapHelper.getInt(e, "limit"));

config.setDiscount(MapHelper.getIntArray(e, "discount"));

config.setFree(MapHelper.getInt(e, "free"));

config.setPriceadd(MapHelper.getInt(e, "priceAdd"));


_ix_id.put(config.getId(),config);



}
}

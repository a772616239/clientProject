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

@annationInit(value ="ItemCard", methodname = "initConfig")
public class ItemCard extends baseConfig<ItemCardObject>{


private static ItemCard instance = null;

public static ItemCard getInstance() {

if (instance == null)
instance = new ItemCard();
return instance;

}


public static Map<Integer, ItemCardObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (ItemCard) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"ItemCard");

for(Map e:ret)
{
put(e);
}

}

public static ItemCardObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, ItemCardObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setLimitday(MapHelper.getInt(e, "limitDay"));

config.setRechargeproductid(MapHelper.getInt(e, "rechargeproductid"));

config.setBuyreward(MapHelper.getInt(e, "buyReward"));

config.setReward(MapHelper.getInt(e, "reward"));


_ix_id.put(config.getId(),config);



}
}

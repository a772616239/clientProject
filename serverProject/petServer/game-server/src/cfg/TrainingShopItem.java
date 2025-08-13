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

@annationInit(value ="TrainingShopItem", methodname = "initConfig")
public class TrainingShopItem extends baseConfig<TrainingShopItemObject>{


private static TrainingShopItem instance = null;

public static TrainingShopItem getInstance() {

if (instance == null)
instance = new TrainingShopItem();
return instance;

}


public static Map<Integer, TrainingShopItemObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (TrainingShopItem) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"TrainingShopItem");

for(Map e:ret)
{
put(e);
}

}

public static TrainingShopItemObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, TrainingShopItemObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setBuff(MapHelper.getInt(e, "buff"));

config.setIsfight(MapHelper.getInts(e, "isFight"));


_ix_id.put(config.getId(),config);



}
}

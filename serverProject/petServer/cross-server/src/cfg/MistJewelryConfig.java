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

@annationInit(value ="MistJewelryConfig", methodname = "initConfig")
public class MistJewelryConfig extends baseConfig<MistJewelryConfigObject>{


private static MistJewelryConfig instance = null;

public static MistJewelryConfig getInstance() {

if (instance == null)
instance = new MistJewelryConfig();
return instance;

}


public static Map<Integer, MistJewelryConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MistJewelryConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MistJewelryConfig");

for(Map e:ret)
{
put(e);
}

}

public static MistJewelryConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MistJewelryConfigObject config){

config.setId(MapHelper.getInt(e, "ID"));

config.setNeedstamina(MapHelper.getInt(e, "NeedStamina"));

config.setDuration(MapHelper.getInt(e, "Duration"));

config.setReward(MapHelper.getIntArray(e, "Reward"));


_ix_id.put(config.getId(),config);



}
}

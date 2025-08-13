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

@annationInit(value ="NewBeeGiftCfg", methodname = "initConfig")
public class NewBeeGiftCfg extends baseConfig<NewBeeGiftCfgObject>{


private static NewBeeGiftCfg instance = null;

public static NewBeeGiftCfg getInstance() {

if (instance == null)
instance = new NewBeeGiftCfg();
return instance;

}


public static Map<Integer, NewBeeGiftCfgObject> _ix_id = new HashMap<Integer, NewBeeGiftCfgObject>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (NewBeeGiftCfg) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"NewBeeGiftCfg");

for(Map e:ret)
{
put(e);
}

}

public static NewBeeGiftCfgObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, NewBeeGiftCfgObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setType(MapHelper.getInt(e, "type"));

config.setConsume(MapHelper.getInts(e, "consume"));

config.setReward(MapHelper.getIntArray(e, "reward"));

config.setLimit(MapHelper.getInt(e, "limit"));


_ix_id.put(config.getId(),config);



}
}

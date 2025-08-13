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

@annationInit(value ="MistFateDoorConfig", methodname = "initConfig")
public class MistFateDoorConfig extends baseConfig<MistFateDoorConfigObject>{


private static MistFateDoorConfig instance = null;

public static MistFateDoorConfig getInstance() {

if (instance == null)
instance = new MistFateDoorConfig();
return instance;

}


public static Map<Integer, MistFateDoorConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MistFateDoorConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MistFateDoorConfig");

for(Map e:ret)
{
put(e);
}

}

public static MistFateDoorConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MistFateDoorConfigObject config){

config.setId(MapHelper.getInt(e, "Id"));

config.setRewardobjlist(MapHelper.getIntArray(e, "rewardObjList"));


_ix_id.put(config.getId(),config);



}
}

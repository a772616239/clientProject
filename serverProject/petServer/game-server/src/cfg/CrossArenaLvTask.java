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

@annationInit(value ="CrossArenaLvTask", methodname = "initConfig")
public class CrossArenaLvTask extends baseConfig<CrossArenaLvTaskObject>{


private static CrossArenaLvTask instance = null;

public static CrossArenaLvTask getInstance() {

if (instance == null)
instance = new CrossArenaLvTask();
return instance;

}


public static Map<Integer, CrossArenaLvTaskObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (CrossArenaLvTask) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"CrossArenaLvTask");

for(Map e:ret)
{
put(e);
}

}

public static CrossArenaLvTaskObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, CrossArenaLvTaskObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setType(MapHelper.getInt(e, "type"));

config.setValue(MapHelper.getInt(e, "value"));

config.setReward(MapHelper.getIntArray(e, "reward"));


_ix_id.put(config.getId(),config);



}
}

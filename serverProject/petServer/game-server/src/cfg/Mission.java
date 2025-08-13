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

@annationInit(value ="Mission", methodname = "initConfig")
public class Mission extends baseConfig<MissionObject>{


private static Mission instance = null;

public static Mission getInstance() {

if (instance == null)
instance = new Mission();
return instance;

}


public static Map<Integer, MissionObject> _ix_id = new HashMap<Integer, MissionObject>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (Mission) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"Mission");

for(Map e:ret)
{
put(e);
}

}

public static MissionObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MissionObject config){

config.setId(MapHelper.getInt(e, "Id"));

config.setMissiontype(MapHelper.getInt(e, "missionType"));

config.setAddtion(MapHelper.getInt(e, "addtion"));

config.setTargetcount(MapHelper.getInt(e, "targetCount"));

config.setFinishreward(MapHelper.getIntArray(e, "finishReward"));


_ix_id.put(config.getId(),config);



}
}

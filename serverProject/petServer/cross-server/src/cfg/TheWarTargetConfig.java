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

@annationInit(value ="TheWarTargetConfig", methodname = "initConfig")
public class TheWarTargetConfig extends baseConfig<TheWarTargetConfigObject>{


private static TheWarTargetConfig instance = null;

public static TheWarTargetConfig getInstance() {

if (instance == null)
instance = new TheWarTargetConfig();
return instance;

}


public static Map<Integer, TheWarTargetConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (TheWarTargetConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"TheWarTargetConfig");

for(Map e:ret)
{
put(e);
}

}

public static TheWarTargetConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, TheWarTargetConfigObject config){

config.setId(MapHelper.getInt(e, "Id"));

config.setMissiontype(MapHelper.getInt(e, "missionType"));

config.setAddtion(MapHelper.getInt(e, "addtion"));

config.setTargetcount(MapHelper.getInt(e, "targetCount"));

config.setFinishnormalreward(MapHelper.getIntArray(e, "finishNormalReward"));

config.setFinishwarreward(MapHelper.getIntArray(e, "finishWarReward"));


_ix_id.put(config.getId(),config);



}
}

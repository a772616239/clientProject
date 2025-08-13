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

@annationInit(value ="TheWarJobTileConfig", methodname = "initConfig")
public class TheWarJobTileConfig extends baseConfig<TheWarJobTileConfigObject>{


private static TheWarJobTileConfig instance = null;

public static TheWarJobTileConfig getInstance() {

if (instance == null)
instance = new TheWarJobTileConfig();
return instance;

}


public static Map<Integer, TheWarJobTileConfigObject> _ix_id = new HashMap<Integer, TheWarJobTileConfigObject>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (TheWarJobTileConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"TheWarJobTileConfig");

for(Map e:ret)
{
put(e);
}

}

public static TheWarJobTileConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, TheWarJobTileConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setMaxpetcount(MapHelper.getInt(e, "maxPetCount"));

config.setTeammaxpetcount(MapHelper.getInt(e, "teamMaxPetCount"));

config.setMaxoccupygirdcount(MapHelper.getInt(e, "maxOccupyGirdcount"));

config.setMaxtechlevel(MapHelper.getInt(e, "maxTechLevel"));

config.setAchievecondition(MapHelper.getInts(e, "achieveCondition"));

config.setJobtilereward(MapHelper.getIntArray(e, "JobTileReward"));


_ix_id.put(config.getId(),config);



}
}

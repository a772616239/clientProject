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

@annationInit(value ="StoneRiftAchievement", methodname = "initConfig")
public class StoneRiftAchievement extends baseConfig<StoneRiftAchievementObject>{


private static StoneRiftAchievement instance = null;

public static StoneRiftAchievement getInstance() {

if (instance == null)
instance = new StoneRiftAchievement();
return instance;

}


public static Map<Integer, StoneRiftAchievementObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (StoneRiftAchievement) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"StoneRiftAchievement");

for(Map e:ret)
{
put(e);
}

}

public static StoneRiftAchievementObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, StoneRiftAchievementObject config){

config.setId(MapHelper.getInt(e, "Id"));

config.setMissionid(MapHelper.getInt(e, "missionId"));

config.setReward(MapHelper.getIntArray(e, "reward"));


_ix_id.put(config.getId(),config);



}
}

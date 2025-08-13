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

@annationInit(value ="ShuraArenaPlayerScoreTen", methodname = "initConfig")
public class ShuraArenaPlayerScoreTen extends baseConfig<ShuraArenaPlayerScoreTenObject>{


private static ShuraArenaPlayerScoreTen instance = null;

public static ShuraArenaPlayerScoreTen getInstance() {

if (instance == null)
instance = new ShuraArenaPlayerScoreTen();
return instance;

}


public static Map<Integer, ShuraArenaPlayerScoreTenObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (ShuraArenaPlayerScoreTen) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"ShuraArenaPlayerScoreTen");

for(Map e:ret)
{
put(e);
}

}

public static ShuraArenaPlayerScoreTenObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, ShuraArenaPlayerScoreTenObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setAreaid(MapHelper.getInt(e, "areaId"));

config.setSection(MapHelper.getInts(e, "section"));

config.setDailyreward(MapHelper.getIntArray(e, "dailyReward"));

config.setWeeklyreward(MapHelper.getIntArray(e, "weeklyReward"));


_ix_id.put(config.getId(),config);



}
}

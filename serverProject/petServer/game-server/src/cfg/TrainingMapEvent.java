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

@annationInit(value ="TrainingMapEvent", methodname = "initConfig")
public class TrainingMapEvent extends baseConfig<TrainingMapEventObject>{


private static TrainingMapEvent instance = null;

public static TrainingMapEvent getInstance() {

if (instance == null)
instance = new TrainingMapEvent();
return instance;

}


public static Map<Integer, TrainingMapEventObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (TrainingMapEvent) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"TrainingMapEvent");

for(Map e:ret)
{
put(e);
}

}

public static TrainingMapEventObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, TrainingMapEventObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setReward(MapHelper.getIntArray(e, "reward"));

config.setEvents(MapHelper.getInts(e, "events"));


_ix_id.put(config.getId(),config);



}
}

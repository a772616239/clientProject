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

@annationInit(value ="TrainingLuckHideBuff", methodname = "initConfig")
public class TrainingLuckHideBuff extends baseConfig<TrainingLuckHideBuffObject>{


private static TrainingLuckHideBuff instance = null;

public static TrainingLuckHideBuff getInstance() {

if (instance == null)
instance = new TrainingLuckHideBuff();
return instance;

}


public static Map<Integer, TrainingLuckHideBuffObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (TrainingLuckHideBuff) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"TrainingLuckHideBuff");

for(Map e:ret)
{
put(e);
}

}

public static TrainingLuckHideBuffObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, TrainingLuckHideBuffObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setMapid(MapHelper.getInt(e, "mapId"));

config.setType(MapHelper.getInt(e, "type"));

config.setConfigformat(MapHelper.getInts(e, "configformat"));

config.setBuff(MapHelper.getInt(e, "buff"));

config.setWeight(MapHelper.getInt(e, "weight"));


_ix_id.put(config.getId(),config);



}
}

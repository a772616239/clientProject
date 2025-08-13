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

@annationInit(value ="TrainingLuckPool", methodname = "initConfig")
public class TrainingLuckPool extends baseConfig<TrainingLuckPoolObject>{


private static TrainingLuckPool instance = null;

public static TrainingLuckPool getInstance() {

if (instance == null)
instance = new TrainingLuckPool();
return instance;

}


public static Map<Integer, TrainingLuckPoolObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (TrainingLuckPool) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"TrainingLuckPool");

for(Map e:ret)
{
put(e);
}

}

public static TrainingLuckPoolObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, TrainingLuckPoolObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setCards(MapHelper.getIntArray(e, "cards"));


_ix_id.put(config.getId(),config);



}
}

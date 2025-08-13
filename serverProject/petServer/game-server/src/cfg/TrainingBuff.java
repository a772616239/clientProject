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

@annationInit(value ="TrainingBuff", methodname = "initConfig")
public class TrainingBuff extends baseConfig<TrainingBuffObject>{


private static TrainingBuff instance = null;

public static TrainingBuff getInstance() {

if (instance == null)
instance = new TrainingBuff();
return instance;

}


public static Map<Integer, TrainingBuffObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (TrainingBuff) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"TrainingBuff");

for(Map e:ret)
{
put(e);
}

}

public static TrainingBuffObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, TrainingBuffObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setBuffcamp(MapHelper.getInt(e, "buffCamp"));


_ix_id.put(config.getId(),config);



}
}

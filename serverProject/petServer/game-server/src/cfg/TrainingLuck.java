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

@annationInit(value ="TrainingLuck", methodname = "initConfig")
public class TrainingLuck extends baseConfig<TrainingLuckObject>{


private static TrainingLuck instance = null;

public static TrainingLuck getInstance() {

if (instance == null)
instance = new TrainingLuck();
return instance;

}


public static Map<Integer, TrainingLuckObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (TrainingLuck) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"TrainingLuck");

for(Map e:ret)
{
put(e);
}

}

public static TrainingLuckObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, TrainingLuckObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setType(MapHelper.getInt(e, "type"));

config.setConfigformat(MapHelper.getIntArray(e, "configformat"));

config.setBuff(MapHelper.getInt(e, "buff"));

config.setWeight(MapHelper.getInt(e, "weight"));

config.setLevel(MapHelper.getInt(e, "level"));

config.setBuffflag(MapHelper.getInt(e, "buffflag"));

config.setGrade(MapHelper.getInt(e, "grade"));

config.setPrice(MapHelper.getIntArray(e, "price"));

config.setDiscount(MapHelper.getIntArray(e, "discount"));


_ix_id.put(config.getId(),config);



}
}

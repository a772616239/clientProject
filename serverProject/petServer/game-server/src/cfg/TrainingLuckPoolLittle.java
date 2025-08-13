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

@annationInit(value ="TrainingLuckPoolLittle", methodname = "initConfig")
public class TrainingLuckPoolLittle extends baseConfig<TrainingLuckPoolLittleObject>{


private static TrainingLuckPoolLittle instance = null;

public static TrainingLuckPoolLittle getInstance() {

if (instance == null)
instance = new TrainingLuckPoolLittle();
return instance;

}


public static Map<Integer, TrainingLuckPoolLittleObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (TrainingLuckPoolLittle) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"TrainingLuckPoolLittle");

for(Map e:ret)
{
put(e);
}

}

public static TrainingLuckPoolLittleObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, TrainingLuckPoolLittleObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setGrade(MapHelper.getInt(e, "grade"));

config.setCards(MapHelper.getIntArray(e, "cards"));


_ix_id.put(config.getId(),config);



}
}

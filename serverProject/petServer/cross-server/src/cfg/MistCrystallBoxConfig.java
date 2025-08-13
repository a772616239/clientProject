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

@annationInit(value ="MistCrystallBoxConfig", methodname = "initConfig")
public class MistCrystallBoxConfig extends baseConfig<MistCrystallBoxConfigObject>{


private static MistCrystallBoxConfig instance = null;

public static MistCrystallBoxConfig getInstance() {

if (instance == null)
instance = new MistCrystallBoxConfig();
return instance;

}


public static Map<Integer, MistCrystallBoxConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MistCrystallBoxConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MistCrystallBoxConfig");

for(Map e:ret)
{
put(e);
}

}

public static MistCrystallBoxConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MistCrystallBoxConfigObject config){

config.setId(MapHelper.getInt(e, "Id"));

config.setOptionallist(MapHelper.getInts(e, "OptionalList"));


_ix_id.put(config.getId(),config);



}
}

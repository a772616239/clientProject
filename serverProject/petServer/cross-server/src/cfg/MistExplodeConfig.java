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

@annationInit(value ="MistExplodeConfig", methodname = "initConfig")
public class MistExplodeConfig extends baseConfig<MistExplodeConfigObject>{


private static MistExplodeConfig instance = null;

public static MistExplodeConfig getInstance() {

if (instance == null)
instance = new MistExplodeConfig();
return instance;

}


public static Map<Integer, MistExplodeConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MistExplodeConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MistExplodeConfig");

for(Map e:ret)
{
put(e);
}

}

public static MistExplodeConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MistExplodeConfigObject config){

config.setId(MapHelper.getInt(e, "Id"));

config.setObjtype(MapHelper.getInt(e, "objType"));

config.setInitprop(MapHelper.getIntArray(e, "InitProp"));


_ix_id.put(config.getId(),config);



}
}

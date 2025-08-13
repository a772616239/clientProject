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

@annationInit(value ="MistStrangeGrassConfig", methodname = "initConfig")
public class MistStrangeGrassConfig extends baseConfig<MistStrangeGrassConfigObject>{


private static MistStrangeGrassConfig instance = null;

public static MistStrangeGrassConfig getInstance() {

if (instance == null)
instance = new MistStrangeGrassConfig();
return instance;

}


public static Map<Integer, MistStrangeGrassConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MistStrangeGrassConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MistStrangeGrassConfig");

for(Map e:ret)
{
put(e);
}

}

public static MistStrangeGrassConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MistStrangeGrassConfigObject config){

config.setId(MapHelper.getInt(e, "Id"));

config.setGenerateobjtype(MapHelper.getInt(e, "GenerateObjType"));

config.setGenerateobjprop(MapHelper.getIntArray(e, "GenerateObjProp"));


_ix_id.put(config.getId(),config);



}
}

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

@annationInit(value ="MistDropObjConfig", methodname = "initConfig")
public class MistDropObjConfig extends baseConfig<MistDropObjConfigObject>{


private static MistDropObjConfig instance = null;

public static MistDropObjConfig getInstance() {

if (instance == null)
instance = new MistDropObjConfig();
return instance;

}


public static Map<Integer, MistDropObjConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MistDropObjConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MistDropObjConfig");

for(Map e:ret)
{
put(e);
}

}

public static MistDropObjConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MistDropObjConfigObject config){

config.setId(MapHelper.getInt(e, "Id"));

config.setDropobjtype(MapHelper.getInt(e, "DropObjType"));

config.setDropobjprop(MapHelper.getIntArray(e, "DropObjProp"));


_ix_id.put(config.getId(),config);



}
}

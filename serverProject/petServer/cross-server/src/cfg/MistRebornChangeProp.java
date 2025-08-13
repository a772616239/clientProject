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

@annationInit(value ="MistRebornChangeProp", methodname = "initConfig")
public class MistRebornChangeProp extends baseConfig<MistRebornChangePropObject>{


private static MistRebornChangeProp instance = null;

public static MistRebornChangeProp getInstance() {

if (instance == null)
instance = new MistRebornChangeProp();
return instance;

}


public static Map<Integer, MistRebornChangePropObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MistRebornChangeProp) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MistRebornChangeProp");

for(Map e:ret)
{
put(e);
}

}

public static MistRebornChangePropObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MistRebornChangePropObject config){

config.setId(MapHelper.getInt(e, "Id"));

config.setProptype(MapHelper.getInts(e, "PropType"));

config.setPropchange(MapHelper.getIntArray(e, "PropChange"));


_ix_id.put(config.getId(),config);



}
}

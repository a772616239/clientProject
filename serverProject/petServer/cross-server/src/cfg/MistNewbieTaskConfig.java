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

@annationInit(value ="MistNewbieTaskConfig", methodname = "initConfig")
public class MistNewbieTaskConfig extends baseConfig<MistNewbieTaskConfigObject>{


private static MistNewbieTaskConfig instance = null;

public static MistNewbieTaskConfig getInstance() {

if (instance == null)
instance = new MistNewbieTaskConfig();
return instance;

}


public static Map<Integer, MistNewbieTaskConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MistNewbieTaskConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MistNewbieTaskConfig");

for(Map e:ret)
{
put(e);
}

}

public static MistNewbieTaskConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MistNewbieTaskConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setMissionid(MapHelper.getInt(e, "missionId"));

config.setUnittype(MapHelper.getInt(e, "unitType"));

config.setUnitpos(MapHelper.getInts(e, "unitPos"));

config.setUnitprop(MapHelper.getIntArray(e, "unitProp"));


_ix_id.put(config.getId(),config);



}
}

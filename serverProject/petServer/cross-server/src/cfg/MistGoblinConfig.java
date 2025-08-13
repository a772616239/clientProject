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

@annationInit(value ="MistGoblinConfig", methodname = "initConfig")
public class MistGoblinConfig extends baseConfig<MistGoblinConfigObject>{


private static MistGoblinConfig instance = null;

public static MistGoblinConfig getInstance() {

if (instance == null)
instance = new MistGoblinConfig();
return instance;

}


public static Map<Integer, MistGoblinConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MistGoblinConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MistGoblinConfig");

for(Map e:ret)
{
put(e);
}

}

public static MistGoblinConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MistGoblinConfigObject config){

config.setId(MapHelper.getInt(e, "Id"));

config.setGeneraterewardidlist(MapHelper.getIntArray(e, "GenerateRewardIdList"));


_ix_id.put(config.getId(),config);



}
}

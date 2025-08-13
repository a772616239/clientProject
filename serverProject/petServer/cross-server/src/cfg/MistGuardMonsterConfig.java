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

@annationInit(value ="MistGuardMonsterConfig", methodname = "initConfig")
public class MistGuardMonsterConfig extends baseConfig<MistGuardMonsterConfigObject>{


private static MistGuardMonsterConfig instance = null;

public static MistGuardMonsterConfig getInstance() {

if (instance == null)
instance = new MistGuardMonsterConfig();
return instance;

}


public static Map<Integer, MistGuardMonsterConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MistGuardMonsterConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MistGuardMonsterConfig");

for(Map e:ret)
{
put(e);
}

}

public static MistGuardMonsterConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MistGuardMonsterConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setBoxinitpos(MapHelper.getInts(e, "boxInitPos"));

config.setGuardmonsterposlist(MapHelper.getIntArray(e, "guardMonsterPosList"));


_ix_id.put(config.getId(),config);



}
}

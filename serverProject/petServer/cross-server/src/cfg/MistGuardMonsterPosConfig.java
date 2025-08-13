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

@annationInit(value ="MistGuardMonsterPosConfig", methodname = "initConfig")
public class MistGuardMonsterPosConfig extends baseConfig<MistGuardMonsterPosConfigObject>{


private static MistGuardMonsterPosConfig instance = null;

public static MistGuardMonsterPosConfig getInstance() {

if (instance == null)
instance = new MistGuardMonsterPosConfig();
return instance;

}


public static Map<Integer, MistGuardMonsterPosConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MistGuardMonsterPosConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MistGuardMonsterPosConfig");

for(Map e:ret)
{
put(e);
}

}

public static MistGuardMonsterPosConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MistGuardMonsterPosConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setPatrolposlist(MapHelper.getIntArray(e, "patrolPosList"));


_ix_id.put(config.getId(),config);



}
}

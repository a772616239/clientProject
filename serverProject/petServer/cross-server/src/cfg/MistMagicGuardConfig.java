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

@annationInit(value ="MistMagicGuardConfig", methodname = "initConfig")
public class MistMagicGuardConfig extends baseConfig<MistMagicGuardConfigObject>{


private static MistMagicGuardConfig instance = null;

public static MistMagicGuardConfig getInstance() {

if (instance == null)
instance = new MistMagicGuardConfig();
return instance;

}


public static Map<Integer, MistMagicGuardConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MistMagicGuardConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MistMagicGuardConfig");

for(Map e:ret)
{
put(e);
}

}

public static MistMagicGuardConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MistMagicGuardConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setRewardconfigid(MapHelper.getInt(e, "rewardConfigId"));

config.setLifetime(MapHelper.getInt(e, "lifeTime"));

config.setExtbufflist(MapHelper.getIntArray(e, "extBuffList"));


_ix_id.put(config.getId(),config);



}
}

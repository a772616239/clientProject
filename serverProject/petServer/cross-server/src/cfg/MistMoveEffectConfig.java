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

@annationInit(value ="MistMoveEffectConfig", methodname = "initConfig")
public class MistMoveEffectConfig extends baseConfig<MistMoveEffectConfigObject>{


private static MistMoveEffectConfig instance = null;

public static MistMoveEffectConfig getInstance() {

if (instance == null)
instance = new MistMoveEffectConfig();
return instance;

}


public static Map<Integer, MistMoveEffectConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MistMoveEffectConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MistMoveEffectConfig");

for(Map e:ret)
{
put(e);
}

}

public static MistMoveEffectConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MistMoveEffectConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setExpiretime(MapHelper.getInt(e, "expireTime"));

config.setExtendbufflist(MapHelper.getInts(e, "extendBuffList"));


_ix_id.put(config.getId(),config);



}
}

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

@annationInit(value ="ForInvBossCloneCfg", methodname = "initConfig")
public class ForInvBossCloneCfg extends baseConfig<ForInvBossCloneCfgObject>{


private static ForInvBossCloneCfg instance = null;

public static ForInvBossCloneCfg getInstance() {

if (instance == null)
instance = new ForInvBossCloneCfg();
return instance;

}


public static Map<Integer, ForInvBossCloneCfgObject> _ix_id = new HashMap<Integer, ForInvBossCloneCfgObject>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (ForInvBossCloneCfg) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"ForInvBossCloneCfg");

for(Map e:ret)
{
put(e);
}

}

public static ForInvBossCloneCfgObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, ForInvBossCloneCfgObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setProperties(MapHelper.getIntArray(e, "properties"));

config.setIntegraladdition(MapHelper.getInt(e, "integralAddition"));

config.setAppearrate(MapHelper.getInt(e, "appearRate"));

config.setBoosaddition(MapHelper.getInt(e, "boosAddition"));


_ix_id.put(config.getId(),config);



}
}

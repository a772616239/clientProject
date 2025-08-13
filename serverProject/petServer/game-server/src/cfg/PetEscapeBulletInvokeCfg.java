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

@annationInit(value ="PetEscapeBulletInvokeCfg", methodname = "initConfig")
public class PetEscapeBulletInvokeCfg extends baseConfig<PetEscapeBulletInvokeCfgObject>{


private static PetEscapeBulletInvokeCfg instance = null;

public static PetEscapeBulletInvokeCfg getInstance() {

if (instance == null)
instance = new PetEscapeBulletInvokeCfg();
return instance;

}


public static Map<Integer, PetEscapeBulletInvokeCfgObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (PetEscapeBulletInvokeCfg) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"PetEscapeBulletInvokeCfg");

for(Map e:ret)
{
put(e);
}

}

public static PetEscapeBulletInvokeCfgObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, PetEscapeBulletInvokeCfgObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setRangetime(MapHelper.getInts(e, "rangeTime"));

config.setInvokeinterval(MapHelper.getInt(e, "invokeInterval"));

config.setInvokepointnum(MapHelper.getInt(e, "invokePointNum"));


_ix_id.put(config.getId(),config);



}
}

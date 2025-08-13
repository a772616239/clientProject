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

@annationInit(value ="MistScheduleObjConfig", methodname = "initConfig")
public class MistScheduleObjConfig extends baseConfig<MistScheduleObjConfigObject>{


private static MistScheduleObjConfig instance = null;

public static MistScheduleObjConfig getInstance() {

if (instance == null)
instance = new MistScheduleObjConfig();
return instance;

}


public static Map<Integer, MistScheduleObjConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MistScheduleObjConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MistScheduleObjConfig");

for(Map e:ret)
{
put(e);
}

}

public static MistScheduleObjConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MistScheduleObjConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setObjtype(MapHelper.getInt(e, "objType"));

config.setRefreshinterval(MapHelper.getInt(e, "refreshInterval"));

config.setInitcount(MapHelper.getInt(e, "initCount"));

config.setMaxcount(MapHelper.getInt(e, "maxCount"));

config.setInitprop(MapHelper.getIntArray(e, "initProp"));

config.setRandprop(MapHelper.getInts(e, "randProp"));

config.setRemovewhenscheduleend(MapHelper.getBoolean(e, "removeWhenScheduleEnd"));

config.setRandposdata(MapHelper.getIntArray(e, "randPosData"));


_ix_id.put(config.getId(),config);



}
}

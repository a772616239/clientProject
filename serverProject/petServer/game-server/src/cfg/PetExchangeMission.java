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

@annationInit(value ="PetExchangeMission", methodname = "initConfig")
public class PetExchangeMission extends baseConfig<PetExchangeMissionObject>{


private static PetExchangeMission instance = null;

public static PetExchangeMission getInstance() {

if (instance == null)
instance = new PetExchangeMission();
return instance;

}


public static Map<Integer, PetExchangeMissionObject> _ix_index = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (PetExchangeMission) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"PetExchangeMission");

for(Map e:ret)
{
put(e);
}

}

public static PetExchangeMissionObject getByIndex(int index){

return _ix_index.get(index);

}



public  void putToMem(Map e, PetExchangeMissionObject config){

config.setIndex(MapHelper.getInt(e, "index"));

config.setName(MapHelper.getInt(e, "name"));

config.setDesc(MapHelper.getInt(e, "desc"));

config.setLimit(MapHelper.getInt(e, "limit"));

config.setEndtime(MapHelper.getInt(e, "endTime"));

config.setRewards(MapHelper.getIntArray(e, "rewards"));

config.setApposeaddition(MapHelper.getIntArray(e, "apposeAddition"));


_ix_index.put(config.getIndex(),config);



}
}

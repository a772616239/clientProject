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

@annationInit(value ="Plot", methodname = "initConfig")
public class Plot extends baseConfig<PlotObject>{


private static Plot instance = null;

public static Plot getInstance() {

if (instance == null)
instance = new Plot();
return instance;

}


public static Map<Integer, PlotObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (Plot) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"Plot");

for(Map e:ret)
{
put(e);
}

}

public static PlotObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, PlotObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setHelppetid(MapHelper.getStr(e, "helpPetId"));

config.setRewardlist(MapHelper.getInts(e, "RewardList"));


_ix_id.put(config.getId(),config);



}
}

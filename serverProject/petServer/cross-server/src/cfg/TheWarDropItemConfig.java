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

@annationInit(value ="TheWarDropItemConfig", methodname = "initConfig")
public class TheWarDropItemConfig extends baseConfig<TheWarDropItemConfigObject>{


private static TheWarDropItemConfig instance = null;

public static TheWarDropItemConfig getInstance() {

if (instance == null)
instance = new TheWarDropItemConfig();
return instance;

}


public static Map<Integer, TheWarDropItemConfigObject> _ix_id = new HashMap<Integer, TheWarDropItemConfigObject>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (TheWarDropItemConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"TheWarDropItemConfig");

for(Map e:ret)
{
put(e);
}

}

public static TheWarDropItemConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, TheWarDropItemConfigObject config){

config.setId(MapHelper.getInt(e, "Id"));

config.setBaseafktime(MapHelper.getInt(e, "baseAfkTime"));

config.setItemdropodds(MapHelper.getIntArray(e, "itemDropOdds"));


_ix_id.put(config.getId(),config);



}
}

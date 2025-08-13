/*CREATED BY TOOL*/

package cfg;
import JsonTool.readJsonFile;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import model.base.baseConfig;
import datatool.MapHelper;
import annotation.annationInit;
import common.load.ServerConfig;

@annationInit(value ="ResourceCopyConfig", methodname = "initConfig")
public class ResourceCopyConfig extends baseConfig<ResourceCopyConfigObject>{


private static ResourceCopyConfig instance = null;

public static ResourceCopyConfig getInstance() {

if (instance == null)
instance = new ResourceCopyConfig();
return instance;

}


public static Map<Integer, ResourceCopyConfigObject> _ix_id = new HashMap<Integer, ResourceCopyConfigObject>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (ResourceCopyConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "ResourceCopyConfig");

for(Map e:ret)
{
put(e);
}

}

public static ResourceCopyConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, ResourceCopyConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setChallengetimes(MapHelper.getInt(e, "challengeTimes"));

config.setBuytimesconsume(MapHelper.getInts(e, "buyTimesConsume"));


_ix_id.put(config.getId(),config);



}
}

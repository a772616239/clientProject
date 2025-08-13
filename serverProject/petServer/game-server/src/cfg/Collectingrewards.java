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

@annationInit(value ="Collectingrewards", methodname = "initConfig")
public class Collectingrewards extends baseConfig<CollectingrewardsObject>{


private static Collectingrewards instance = null;

public static Collectingrewards getInstance() {

if (instance == null)
instance = new Collectingrewards();
return instance;

}


public static Map<Integer, CollectingrewardsObject> _ix_id = new HashMap<Integer, CollectingrewardsObject>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (Collectingrewards) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"Collectingrewards");

for(Map e:ret)
{
put(e);
}

}

public static CollectingrewardsObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, CollectingrewardsObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setCount(MapHelper.getInt(e, "count"));

config.setAwards(MapHelper.getIntArray(e, "awards"));


_ix_id.put(config.getId(),config);



}
}

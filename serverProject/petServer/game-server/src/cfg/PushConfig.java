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

@annationInit(value ="PushConfig", methodname = "initConfig")
public class PushConfig extends baseConfig<PushConfigObject>{


private static PushConfig instance = null;

public static PushConfig getInstance() {

if (instance == null)
instance = new PushConfig();
return instance;

}


public static Map<Integer, PushConfigObject> _ix_id = new HashMap<Integer, PushConfigObject>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (PushConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"PushConfig");

for(Map e:ret)
{
put(e);
}

}

public static PushConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, PushConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setLeadtime(MapHelper.getInt(e, "leadTime"));

config.setCycle(MapHelper.getInt(e, "cycle"));

config.setTitle(MapHelper.getInt(e, "title"));

config.setContent(MapHelper.getInt(e, "content"));

config.setPushtime(MapHelper.getStrs(e, "pushTime"));


_ix_id.put(config.getId(),config);



}
}

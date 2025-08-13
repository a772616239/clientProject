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

@annationInit(value ="MistTimeLimitActivity", methodname = "initConfig")
public class MistTimeLimitActivity extends baseConfig<MistTimeLimitActivityObject>{


private static MistTimeLimitActivity instance = null;

public static MistTimeLimitActivity getInstance() {

if (instance == null)
instance = new MistTimeLimitActivity();
return instance;

}


public static Map<Integer, MistTimeLimitActivityObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MistTimeLimitActivity) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MistTimeLimitActivity");

for(Map e:ret)
{
put(e);
}

}

public static MistTimeLimitActivityObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MistTimeLimitActivityObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setStarttime(MapHelper.getStr(e, "startTime"));

config.setEndtime(MapHelper.getStr(e, "endTime"));

config.setActivitytype(MapHelper.getInt(e, "ActivityType"));


_ix_id.put(config.getId(),config);



}
}

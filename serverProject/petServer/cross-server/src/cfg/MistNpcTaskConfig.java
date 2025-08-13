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

@annationInit(value ="MistNpcTaskConfig", methodname = "initConfig")
public class MistNpcTaskConfig extends baseConfig<MistNpcTaskConfigObject>{


private static MistNpcTaskConfig instance = null;

public static MistNpcTaskConfig getInstance() {

if (instance == null)
instance = new MistNpcTaskConfig();
return instance;

}


public static Map<Integer, MistNpcTaskConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MistNpcTaskConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MistNpcTaskConfig");

for(Map e:ret)
{
put(e);
}

}

public static MistNpcTaskConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MistNpcTaskConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setMisttasktype(MapHelper.getInt(e, "mistTaskType"));

config.setTargetcount(MapHelper.getInt(e, "targetCount"));

config.setExtparam(MapHelper.getInt(e, "extParam"));

config.setDuration(MapHelper.getInt(e, "duration"));

config.setFinishrewrad(MapHelper.getIntArray(e, "finishRewrad"));


_ix_id.put(config.getId(),config);



}
}

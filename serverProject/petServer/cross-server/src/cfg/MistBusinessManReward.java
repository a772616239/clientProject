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

@annationInit(value ="MistBusinessManReward", methodname = "initConfig")
public class MistBusinessManReward extends baseConfig<MistBusinessManRewardObject>{


private static MistBusinessManReward instance = null;

public static MistBusinessManReward getInstance() {

if (instance == null)
instance = new MistBusinessManReward();
return instance;

}


public static Map<Integer, MistBusinessManRewardObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MistBusinessManReward) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MistBusinessManReward");

for(Map e:ret)
{
put(e);
}

}

public static MistBusinessManRewardObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MistBusinessManRewardObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setFinishreward(MapHelper.getIntArray(e, "finishReward"));

config.setFailedreward(MapHelper.getIntArray(e, "failedReward"));


_ix_id.put(config.getId(),config);



}
}

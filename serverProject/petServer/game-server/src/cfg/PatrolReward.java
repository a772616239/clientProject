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

@annationInit(value ="PatrolReward", methodname = "initConfig")
public class PatrolReward extends baseConfig<PatrolRewardObject>{


private static PatrolReward instance = null;

public static PatrolReward getInstance() {

if (instance == null)
instance = new PatrolReward();
return instance;

}


public static Map<Integer, PatrolRewardObject> _ix_id = new HashMap<Integer, PatrolRewardObject>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (PatrolReward) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PatrolReward");

for(Map e:ret)
{
put(e);
}

}

public static PatrolRewardObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, PatrolRewardObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setRewardtype(MapHelper.getStr(e, "rewardType"));

config.setRewardrange(MapHelper.getInts(e, "rewardRange"));

config.setRandomreward(MapHelper.getIntArray(e, "randomReward"));

config.setFixedreward(MapHelper.getIntArray(e, "fixedReward"));


_ix_id.put(config.getId(),config);



}
}

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

@annationInit(value ="RewardConfig", methodname = "initConfig")
public class RewardConfig extends baseConfig<RewardConfigObject>{


private static RewardConfig instance = null;

public static RewardConfig getInstance() {

if (instance == null)
instance = new RewardConfig();
return instance;

}


public static Map<Integer, RewardConfigObject> _ix_rewardid = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (RewardConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"RewardConfig");

for(Map e:ret)
{
put(e);
}

}

public static RewardConfigObject getByRewardid(int rewardid){

return _ix_rewardid.get(rewardid);

}



public  void putToMem(Map e, RewardConfigObject config){

config.setRewardid(MapHelper.getInt(e, "rewardId"));

config.setMustreward(MapHelper.getIntArray(e, "mustReward"));

config.setRandomreward(MapHelper.getIntArray(e, "randomReward"));

config.setRandomtimes(MapHelper.getInt(e, "randomTimes"));


_ix_rewardid.put(config.getRewardid(),config);



}
}

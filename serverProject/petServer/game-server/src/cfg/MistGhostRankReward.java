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

@annationInit(value ="MistGhostRankReward", methodname = "initConfig")
public class MistGhostRankReward extends baseConfig<MistGhostRankRewardObject>{


private static MistGhostRankReward instance = null;

public static MistGhostRankReward getInstance() {

if (instance == null)
instance = new MistGhostRankReward();
return instance;

}


public static Map<Integer, MistGhostRankRewardObject> _ix_rank = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MistGhostRankReward) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MistGhostRankReward");

for(Map e:ret)
{
put(e);
}

}

public static MistGhostRankRewardObject getByRank(int rank){

return _ix_rank.get(rank);

}



public  void putToMem(Map e, MistGhostRankRewardObject config){

config.setRank(MapHelper.getInt(e, "rank"));

config.setRewardid(MapHelper.getInt(e, "RewardId"));


_ix_rank.put(config.getRank(),config);



}
}

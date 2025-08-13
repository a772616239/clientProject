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

@annationInit(value ="ForeignInvasionRankingReward", methodname = "initConfig")
public class ForeignInvasionRankingReward extends baseConfig<ForeignInvasionRankingRewardObject>{


private static ForeignInvasionRankingReward instance = null;

public static ForeignInvasionRankingReward getInstance() {

if (instance == null)
instance = new ForeignInvasionRankingReward();
return instance;

}


public static Map<Integer, ForeignInvasionRankingRewardObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (ForeignInvasionRankingReward) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"ForeignInvasionRankingReward");

for(Map e:ret)
{
put(e);
}

}

public static ForeignInvasionRankingRewardObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, ForeignInvasionRankingRewardObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setStartranking(MapHelper.getInt(e, "startRanking"));

config.setEndranking(MapHelper.getInt(e, "endRanking"));

config.setRewards(MapHelper.getIntArray(e, "rewards"));


_ix_id.put(config.getId(),config);



}
}

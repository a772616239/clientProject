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

@annationInit(value ="RankConfig", methodname = "initConfig")
public class RankConfig extends baseConfig<RankConfigObject>{


private static RankConfig instance = null;

public static RankConfig getInstance() {

if (instance == null)
instance = new RankConfig();
return instance;

}


public static Map<Integer, RankConfigObject> _ix_rankid = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (RankConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"RankConfig");

for(Map e:ret)
{
put(e);
}

}

public static RankConfigObject getByRankid(int rankid){

return _ix_rankid.get(rankid);

}



public  void putToMem(Map e, RankConfigObject config){

config.setRankid(MapHelper.getInt(e, "rankId"));

config.setSort(MapHelper.getInt(e, "sort"));

config.setRankreward_range(MapHelper.getInts(e, "rankReward_Range"));

config.setRankreward_target(MapHelper.getInts(e, "rankReward_Target"));

config.setRankrefreshtime(MapHelper.getInt(e, "rankRefreshTime"));


_ix_rankid.put(config.getRankid(),config);



}
}

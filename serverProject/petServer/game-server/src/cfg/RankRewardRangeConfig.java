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

@annationInit(value ="RankRewardRangeConfig", methodname = "initConfig")
public class RankRewardRangeConfig extends baseConfig<RankRewardRangeConfigObject>{


private static RankRewardRangeConfig instance = null;

public static RankRewardRangeConfig getInstance() {

if (instance == null)
instance = new RankRewardRangeConfig();
return instance;

}


public static Map<Integer, RankRewardRangeConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (RankRewardRangeConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"RankRewardRangeConfig");

for(Map e:ret)
{
put(e);
}

}

public static RankRewardRangeConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, RankRewardRangeConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setRangemin(MapHelper.getInt(e, "rangeMin"));

config.setRangemax(MapHelper.getInt(e, "rangeMax"));

config.setReward(MapHelper.getIntArray(e, "Reward"));


_ix_id.put(config.getId(),config);



}
}

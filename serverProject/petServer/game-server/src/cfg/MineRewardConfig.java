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

@annationInit(value ="MineRewardConfig", methodname = "initConfig")
public class MineRewardConfig extends baseConfig<MineRewardConfigObject>{


private static MineRewardConfig instance = null;

public static MineRewardConfig getInstance() {

if (instance == null)
instance = new MineRewardConfig();
return instance;

}


public static Map<Integer, MineRewardConfigObject> _ix_minerewardtype = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MineRewardConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MineRewardConfig");

for(Map e:ret)
{
put(e);
}

}

public static MineRewardConfigObject getByMinerewardtype(int minerewardtype){

return _ix_minerewardtype.get(minerewardtype);

}



public  void putToMem(Map e, MineRewardConfigObject config){

config.setMinerewardtype(MapHelper.getInt(e, "MineRewardType"));

config.setRewartype(MapHelper.getInt(e, "RewarType"));

config.setRewardid(MapHelper.getInt(e, "RewardId"));

config.setDailyrrewarlimit(MapHelper.getInt(e, "DailyRrewarLimit"));


_ix_minerewardtype.put(config.getMinerewardtype(),config);



}
}

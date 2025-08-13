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

@annationInit(value ="CrazyDuelReward", methodname = "initConfig")
public class CrazyDuelReward extends baseConfig<CrazyDuelRewardObject>{


private static CrazyDuelReward instance = null;

public static CrazyDuelReward getInstance() {

if (instance == null)
instance = new CrazyDuelReward();
return instance;

}


public static Map<Integer, CrazyDuelRewardObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (CrazyDuelReward) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"CrazyDuelReward");

for(Map e:ret)
{
put(e);
}

}

public static CrazyDuelRewardObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, CrazyDuelRewardObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setLowerlimit(MapHelper.getInt(e, "lowerLimit"));

config.setUpperlimit(MapHelper.getInt(e, "upperLimit"));

config.setReward(MapHelper.getIntArray(e, "reward"));


_ix_id.put(config.getId(),config);



}
}

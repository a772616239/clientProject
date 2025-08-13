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

@annationInit(value ="MainLineNodeShowReward", methodname = "initConfig")
public class MainLineNodeShowReward extends baseConfig<MainLineNodeShowRewardObject>{


private static MainLineNodeShowReward instance = null;

public static MainLineNodeShowReward getInstance() {

if (instance == null)
instance = new MainLineNodeShowReward();
return instance;

}


public static Map<Integer, MainLineNodeShowRewardObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MainLineNodeShowReward) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MainLineNodeShowReward");

for(Map e:ret)
{
put(e);
}

}

public static MainLineNodeShowRewardObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MainLineNodeShowRewardObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setOnshowreward(MapHelper.getInts(e, "onShowReward"));


_ix_id.put(config.getId(),config);



}
}

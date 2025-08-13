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

@annationInit(value ="FeatsRewardConfig", methodname = "initConfig")
public class FeatsRewardConfig extends baseConfig<FeatsRewardConfigObject>{


private static FeatsRewardConfig instance = null;

public static FeatsRewardConfig getInstance() {

if (instance == null)
instance = new FeatsRewardConfig();
return instance;

}


public static Map<Integer, FeatsRewardConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (FeatsRewardConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"FeatsRewardConfig");

for(Map e:ret)
{
put(e);
}

}

public static FeatsRewardConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, FeatsRewardConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setFeatsneed(MapHelper.getInt(e, "featsNeed"));

config.setType(MapHelper.getInt(e, "type"));

config.setBasicreward(MapHelper.getInt(e, "basicReward"));

config.setAdvancedreward(MapHelper.getInt(e, "advancedReward"));


_ix_id.put(config.getId(),config);



}
}

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

@annationInit(value ="FarmMineAward", methodname = "initConfig")
public class FarmMineAward extends baseConfig<FarmMineAwardObject>{


private static FarmMineAward instance = null;

public static FarmMineAward getInstance() {

if (instance == null)
instance = new FarmMineAward();
return instance;

}


public static Map<Integer, FarmMineAwardObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (FarmMineAward) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"FarmMineAward");

for(Map e:ret)
{
put(e);
}

}

public static FarmMineAwardObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, FarmMineAwardObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setType(MapHelper.getInt(e, "type"));

config.setQuality(MapHelper.getInt(e, "quality"));

config.setWeight(MapHelper.getInt(e, "weight"));

config.setReward(MapHelper.getInts(e, "reward"));

config.setRewardvue(MapHelper.getFloat(e, "rewardVue"));

config.setPetid(MapHelper.getInt(e, "petId"));

config.setPetadd(MapHelper.getInt(e, "petAdd"));


_ix_id.put(config.getId(),config);



}
}

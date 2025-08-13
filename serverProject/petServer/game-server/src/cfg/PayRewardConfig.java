/*CREATED BY TOOL*/

package cfg;
import JsonTool.readJsonFile;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import model.base.baseConfig;
import datatool.MapHelper;
import annotation.annationInit;
import common.load.ServerConfig;

@annationInit(value ="PayRewardConfig", methodname = "initConfig")
public class PayRewardConfig extends baseConfig<PayRewardConfigObject>{


private static PayRewardConfig instance = null;

public static PayRewardConfig getInstance() {

if (instance == null)
instance = new PayRewardConfig();
return instance;

}


public static Map<Integer, PayRewardConfigObject> _ix_id = new HashMap<Integer, PayRewardConfigObject>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (PayRewardConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PayRewardConfig");

for(Map e:ret)
{
put(e);
}

}

public static PayRewardConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, PayRewardConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setDiamondneed(MapHelper.getInt(e, "diamondNeed"));

config.setReward(MapHelper.getInts(e, "reward"));


_ix_id.put(config.getId(),config);



}
}

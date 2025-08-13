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

@annationInit(value ="MistLootPackCarryConfig", methodname = "initConfig")
public class MistLootPackCarryConfig extends baseConfig<MistLootPackCarryConfigObject>{


private static MistLootPackCarryConfig instance = null;

public static MistLootPackCarryConfig getInstance() {

if (instance == null)
instance = new MistLootPackCarryConfig();
return instance;

}


public static Map<Integer, MistLootPackCarryConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MistLootPackCarryConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MistLootPackCarryConfig");

for(Map e:ret)
{
put(e);
}

}

public static MistLootPackCarryConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MistLootPackCarryConfigObject config){

config.setId(MapHelper.getInt(e, "ID"));

config.setRewardtype(MapHelper.getInt(e, "RewardType"));

config.setRewardid(MapHelper.getInt(e, "RewardId"));

config.setCarrylimit(MapHelper.getIntArray(e, "CarryLimit"));

config.setNeedalchemynum(MapHelper.getInts(e, "NeedAlchemyNum"));

config.setAlchemyrewardcount(MapHelper.getInt(e, "AlchemyRewardCount"));

config.setAlchemyexhangereward(MapHelper.getIntArray(e, "AlchemyExhangeReward"));

config.setOnlyuseinmist(MapHelper.getBoolean(e, "OnlyUseInMist"));


_ix_id.put(config.getId(),config);



}
}

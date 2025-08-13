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

@annationInit(value ="AdsConfig", methodname = "initConfig")
public class AdsConfig extends baseConfig<AdsConfigObject>{


private static AdsConfig instance = null;

public static AdsConfig getInstance() {

if (instance == null)
instance = new AdsConfig();
return instance;

}


public static Map<Integer, AdsConfigObject> _ix_id = new HashMap<Integer, AdsConfigObject>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (AdsConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"AdsConfig");

for(Map e:ret)
{
put(e);
}

}

public static AdsConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, AdsConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setFreeadsdisplaytime(MapHelper.getInt(e, "FreeAdsDisplayTime"));

config.setFreeadsgifttimes(MapHelper.getInt(e, "FreeAdsGiftTimes"));

config.setFreeadsgiftreward(MapHelper.getInt(e, "FreeAdsGiftReward"));

config.setWheeladsdisplaytime(MapHelper.getInt(e, "WheelAdsDisplayTime"));

config.setWheelbonustimes(MapHelper.getInt(e, "WheelBonusTimes"));

config.setWatchwheeladstimes(MapHelper.getInt(e, "WatchWheelAdsTimes"));

config.setWheelbonuslist(MapHelper.getInt(e, "WheelBonusList"));


_ix_id.put(config.getId(),config);



}
}

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

@annationInit(value ="FarmMineConfig", methodname = "initConfig")
public class FarmMineConfig extends baseConfig<FarmMineConfigObject>{


private static FarmMineConfig instance = null;

public static FarmMineConfig getInstance() {

if (instance == null)
instance = new FarmMineConfig();
return instance;

}


public static Map<Integer, FarmMineConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (FarmMineConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"FarmMineConfig");

for(Map e:ret)
{
put(e);
}

}

public static FarmMineConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, FarmMineConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setParm(MapHelper.getStr(e, "parm"));

config.setMainmax(MapHelper.getInt(e, "mainmax"));

config.setOfferpriceconsume(MapHelper.getInts(e, "offerPriceConsume"));

config.setOfferpricitem(MapHelper.getInts(e, "offerPricItem"));

config.setAddpricepre(MapHelper.getInt(e, "addPricePre"));

config.setPricenum(MapHelper.getInt(e, "priceNum"));

config.setExtawardtime(MapHelper.getIntArray(e, "extAwardTime"));

config.setBestealsnum(MapHelper.getInt(e, "bestealsNum"));

config.setStealstimecan(MapHelper.getInt(e, "stealsTimeCan"));

config.setStealsnum(MapHelper.getInt(e, "stealsNum"));

config.setStealsvue(MapHelper.getInt(e, "stealsVue"));

config.setSpeedadd(MapHelper.getIntArray(e, "speedAdd"));

config.setSpeedaddvue(MapHelper.getInt(e, "speedAddVue"));

config.setHarvestinstime(MapHelper.getInt(e, "harvestInsTime"));

config.setHarvesttimemax(MapHelper.getInt(e, "harvestTimeMax"));

config.setTitle(MapHelper.getInts(e, "title"));


_ix_id.put(config.getId(),config);



}
}

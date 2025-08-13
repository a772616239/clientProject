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

@annationInit(value ="ZeroCostPurchaseCfg", methodname = "initConfig")
public class ZeroCostPurchaseCfg extends baseConfig<ZeroCostPurchaseCfgObject>{


private static ZeroCostPurchaseCfg instance = null;

public static ZeroCostPurchaseCfg getInstance() {

if (instance == null)
instance = new ZeroCostPurchaseCfg();
return instance;

}


public static Map<Integer, ZeroCostPurchaseCfgObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (ZeroCostPurchaseCfg) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"ZeroCostPurchaseCfg");

for(Map e:ret)
{
put(e);
}

}

public static ZeroCostPurchaseCfgObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, ZeroCostPurchaseCfgObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setConsume(MapHelper.getInts(e, "consume"));

config.setDelayreward(MapHelper.getIntArray(e, "delayReward"));

config.setInstantreward(MapHelper.getInts(e, "instantReward"));


_ix_id.put(config.getId(),config);



}
}

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

@annationInit(value ="MonthlyCardConfig", methodname = "initConfig")
public class MonthlyCardConfig extends baseConfig<MonthlyCardConfigObject>{


private static MonthlyCardConfig instance = null;

public static MonthlyCardConfig getInstance() {

if (instance == null)
instance = new MonthlyCardConfig();
return instance;

}


public static Map<Integer, MonthlyCardConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MonthlyCardConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MonthlyCardConfig");

for(Map e:ret)
{
put(e);
}

}

public static MonthlyCardConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MonthlyCardConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setInstantrewards(MapHelper.getIntArray(e, "instantRewards"));

config.setEverydayrewards(MapHelper.getIntArray(e, "everydayRewards"));

config.setSumrewards(MapHelper.getInt(e, "sumRewards"));

config.setPrice(MapHelper.getInts(e, "price"));

config.setRechargeproductid(MapHelper.getInt(e, "rechargeproductid"));


_ix_id.put(config.getId(),config);



}
}

/*CREATED BY TOOL*/

package cfg;
import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import model.base.baseConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@annationInit(value ="BuyTaskConfig", methodname = "initConfig")
public class BuyTaskConfig extends baseConfig<BuyTaskConfigObject>{


private static BuyTaskConfig instance = null;

public static BuyTaskConfig getInstance() {

if (instance == null)
instance = new BuyTaskConfig();
return instance;

}


public static Map<Integer, BuyTaskConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (BuyTaskConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"BuyTaskConfig");

for(Map e:ret)
{
put(e);
}

}

public static BuyTaskConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, BuyTaskConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setActivityid(MapHelper.getInt(e, "activityId"));

config.setTitle(MapHelper.getInt(e, "title"));

config.setLimitbuy(MapHelper.getInt(e, "limitBuy"));

config.setPrice(MapHelper.getInts(e, "price"));

config.setReward(MapHelper.getInt(e, "reward"));

config.setDiscount(MapHelper.getInt(e, "discount"));

config.setSpecialtype(MapHelper.getInt(e, "specialType"));


_ix_id.put(config.getId(),config);



}
}

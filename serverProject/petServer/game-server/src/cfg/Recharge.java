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

@annationInit(value ="Recharge", methodname = "initConfig")
public class Recharge extends baseConfig<RechargeObject>{


private static Recharge instance = null;

public static Recharge getInstance() {

if (instance == null)
instance = new Recharge();
return instance;

}


public static Map<Integer, RechargeObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (Recharge) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"Recharge");

for(Map e:ret)
{
put(e);
}

}

public static RechargeObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, RechargeObject config){

config.setId(MapHelper.getInt(e, "ID"));

config.setRechargeamount2(MapHelper.getInt(e, "RechargeAmount2"));

config.setNumberofdiamonds(MapHelper.getInt(e, "NumberOfDiamonds"));

config.setGivevipexp(MapHelper.getInt(e, "giveVipExp"));

config.setProductid(MapHelper.getInt(e, "ProductId"));

config.setGiftdiamonds(MapHelper.getInt(e, "GiftDiamonds"));

config.setRecommend(MapHelper.getBoolean(e, "recommend"));

config.setPurchaselimit(MapHelper.getBoolean(e, "purchaseLimit"));

config.setRechargetype(MapHelper.getInt(e, "rechargeType"));

config.setFirstrechargetype(MapHelper.getBoolean(e, "firstRechargeType"));

config.setBegintime(MapHelper.getInt(e, "beginTime"));

config.setEndtime(MapHelper.getInt(e, "endTime"));


_ix_id.put(config.getId(),config);



}
}

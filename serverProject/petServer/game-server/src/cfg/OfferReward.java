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

@annationInit(value ="OfferReward", methodname = "initConfig")
public class OfferReward extends baseConfig<OfferRewardObject>{


private static OfferReward instance = null;

public static OfferReward getInstance() {

if (instance == null)
instance = new OfferReward();
return instance;

}


public static Map<Integer, OfferRewardObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (OfferReward) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"OfferReward");

for(Map e:ret)
{
put(e);
}

}

public static OfferRewardObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, OfferRewardObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setBoss(MapHelper.getIntArray(e, "boss"));

config.setTime(MapHelper.getInt(e, "time"));

config.setPetreward(MapHelper.getIntArray(e, "petreward"));

config.setRunereward(MapHelper.getIntArray(e, "runereward"));

config.setGemreward(MapHelper.getIntArray(e, "gemreward"));

config.setOtherreward(MapHelper.getIntArray(e, "otherreward"));

config.setConsume(MapHelper.getInts(e, "consume"));

config.setAcceptconsume(MapHelper.getInts(e, "acceptconsume"));

config.setAcceptreward(MapHelper.getIntArray(e, "acceptreward"));

config.setReward(MapHelper.getIntArray(e, "reward"));

config.setRewardnum(MapHelper.getInt(e, "rewardNum"));

config.setPublisherrewardrate(MapHelper.getInts(e, "publisherRewardRate"));


_ix_id.put(config.getId(),config);



}
}

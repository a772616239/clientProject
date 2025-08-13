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

@annationInit(value ="WishWellConfig", methodname = "initConfig")
public class WishWellConfig extends baseConfig<WishWellConfigObject>{


private static WishWellConfig instance = null;

public static WishWellConfig getInstance() {

if (instance == null)
instance = new WishWellConfig();
return instance;

}


public static Map<Integer, WishWellConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (WishWellConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"WishWellConfig");

for(Map e:ret)
{
put(e);
}

}

public static WishWellConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, WishWellConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setStartday(MapHelper.getInt(e, "startDay"));

config.setClaimday(MapHelper.getInt(e, "claimDay"));

config.setRewardoptions(MapHelper.getInt(e, "rewardoptions"));

config.setMakeupprice(MapHelper.getInts(e, "makeupPrice"));

config.setReplenishsignprice(MapHelper.getInts(e, "replenishSignPrice"));


_ix_id.put(config.getId(),config);



}
}

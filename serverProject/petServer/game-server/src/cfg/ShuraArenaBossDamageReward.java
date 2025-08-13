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

@annationInit(value ="ShuraArenaBossDamageReward", methodname = "initConfig")
public class ShuraArenaBossDamageReward extends baseConfig<ShuraArenaBossDamageRewardObject>{


private static ShuraArenaBossDamageReward instance = null;

public static ShuraArenaBossDamageReward getInstance() {

if (instance == null)
instance = new ShuraArenaBossDamageReward();
return instance;

}


public static Map<Integer, ShuraArenaBossDamageRewardObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (ShuraArenaBossDamageReward) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"ShuraArenaBossDamageReward");

for(Map e:ret)
{
put(e);
}

}

public static ShuraArenaBossDamageRewardObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, ShuraArenaBossDamageRewardObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setDamagel(MapHelper.getInt(e, "damageL"));

config.setDamageh(MapHelper.getInt(e, "damageH"));

config.setReward(MapHelper.getIntArray(e, "reward"));


_ix_id.put(config.getId(),config);



}
}

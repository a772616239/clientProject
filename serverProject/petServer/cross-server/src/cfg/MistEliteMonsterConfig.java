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

@annationInit(value ="MistEliteMonsterConfig", methodname = "initConfig")
public class MistEliteMonsterConfig extends baseConfig<MistEliteMonsterConfigObject>{


private static MistEliteMonsterConfig instance = null;

public static MistEliteMonsterConfig getInstance() {

if (instance == null)
instance = new MistEliteMonsterConfig();
return instance;

}


public static Map<Integer, MistEliteMonsterConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MistEliteMonsterConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MistEliteMonsterConfig");

for(Map e:ret)
{
put(e);
}

}

public static MistEliteMonsterConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MistEliteMonsterConfigObject config){

config.setId(MapHelper.getInt(e, "Id"));

config.setMaxrewardtimes(MapHelper.getInt(e, "MaxRewardTimes"));

config.setRadnombattlereward(MapHelper.getIntArray(e, "RadnomBattleReward"));

config.setMustgainreward(MapHelper.getIntArray(e, "MustGainReward"));


_ix_id.put(config.getId(),config);



}
}

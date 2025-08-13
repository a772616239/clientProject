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

@annationInit(value ="MistGhostConfig", methodname = "initConfig")
public class MistGhostConfig extends baseConfig<MistGhostConfigObject>{


private static MistGhostConfig instance = null;

public static MistGhostConfig getInstance() {

if (instance == null)
instance = new MistGhostConfig();
return instance;

}


public static Map<Integer, MistGhostConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MistGhostConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MistGhostConfig");

for(Map e:ret)
{
put(e);
}

}

public static MistGhostConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MistGhostConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setGhosttouchscore(MapHelper.getInt(e, "GhostTouchScore"));

config.setGhosttouchreward(MapHelper.getInts(e, "GhostTouchReward"));


_ix_id.put(config.getId(),config);



}
}

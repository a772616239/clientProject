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

@annationInit(value ="MainLineEpisodeNodeConfig", methodname = "initConfig")
public class MainLineEpisodeNodeConfig extends baseConfig<MainLineEpisodeNodeConfigObject>{


private static MainLineEpisodeNodeConfig instance = null;

public static MainLineEpisodeNodeConfig getInstance() {

if (instance == null)
instance = new MainLineEpisodeNodeConfig();
return instance;

}


public static Map<Integer, MainLineEpisodeNodeConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MainLineEpisodeNodeConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MainLineEpisodeNodeConfig");

for(Map e:ret)
{
put(e);
}

}

public static MainLineEpisodeNodeConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MainLineEpisodeNodeConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setType(MapHelper.getInt(e, "type"));

config.setShowreward(MapHelper.getIntArray(e, "showReward"));

config.setBeforeplot(MapHelper.getInts(e, "beforePlot"));

config.setFightmakeid(MapHelper.getInt(e, "fightMakeId"));

config.setLaterplot(MapHelper.getInts(e, "laterPlot"));

config.setHelppetpool(MapHelper.getInts(e, "helpPetPool"));

config.setHelppetnum(MapHelper.getInt(e, "helpPetNum"));

config.setPlayerskill(MapHelper.getIntArray(e, "playerSkill"));


_ix_id.put(config.getId(),config);



}
}

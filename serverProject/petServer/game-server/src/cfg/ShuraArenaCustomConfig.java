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

@annationInit(value ="ShuraArenaCustomConfig", methodname = "initConfig")
public class ShuraArenaCustomConfig extends baseConfig<ShuraArenaCustomConfigObject>{


private static ShuraArenaCustomConfig instance = null;

public static ShuraArenaCustomConfig getInstance() {

if (instance == null)
instance = new ShuraArenaCustomConfig();
return instance;

}


public static Map<Integer, ShuraArenaCustomConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (ShuraArenaCustomConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"ShuraArenaCustomConfig");

for(Map e:ret)
{
put(e);
}

}

public static ShuraArenaCustomConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, ShuraArenaCustomConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setCustom(MapHelper.getInt(e, "custom"));

config.setRobotname(MapHelper.getInt(e, "robotName"));

config.setAvatar(MapHelper.getInt(e, "avatar"));

config.setReward(MapHelper.getInt(e, "reward"));

config.setScore(MapHelper.getInt(e, "score"));

config.setBuff(MapHelper.getInt(e, "buff"));


_ix_id.put(config.getId(),config);



}
}

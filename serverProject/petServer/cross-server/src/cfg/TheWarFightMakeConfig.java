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

@annationInit(value ="TheWarFightMakeConfig", methodname = "initConfig")
public class TheWarFightMakeConfig extends baseConfig<TheWarFightMakeConfigObject>{


private static TheWarFightMakeConfig instance = null;

public static TheWarFightMakeConfig getInstance() {

if (instance == null)
instance = new TheWarFightMakeConfig();
return instance;

}


public static Map<Integer, TheWarFightMakeConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (TheWarFightMakeConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"TheWarFightMakeConfig");

for(Map e:ret)
{
put(e);
}

}

public static TheWarFightMakeConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, TheWarFightMakeConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setFightmakelist(MapHelper.getInts(e, "fightMakeList"));


_ix_id.put(config.getId(),config);



}
}

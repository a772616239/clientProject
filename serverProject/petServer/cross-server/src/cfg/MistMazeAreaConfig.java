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

@annationInit(value ="MistMazeAreaConfig", methodname = "initConfig")
public class MistMazeAreaConfig extends baseConfig<MistMazeAreaConfigObject>{


private static MistMazeAreaConfig instance = null;

public static MistMazeAreaConfig getInstance() {

if (instance == null)
instance = new MistMazeAreaConfig();
return instance;

}


public static Map<Integer, MistMazeAreaConfigObject> _ix_level = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MistMazeAreaConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MistMazeAreaConfig");

for(Map e:ret)
{
put(e);
}

}

public static MistMazeAreaConfigObject getByLevel(int level){

return _ix_level.get(level);

}



public  void putToMem(Map e, MistMazeAreaConfigObject config){

config.setLevel(MapHelper.getInt(e, "level"));

config.setEnterpos(MapHelper.getInts(e, "enterPos"));

config.setMazetranspos(MapHelper.getIntArray(e, "mazeTransPos"));

config.setMazenum(MapHelper.getInts(e, "mazeNum"));

config.setTaskid(MapHelper.getInt(e, "taskId"));


_ix_level.put(config.getLevel(),config);



}
}

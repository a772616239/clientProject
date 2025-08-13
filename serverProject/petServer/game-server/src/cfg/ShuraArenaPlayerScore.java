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

@annationInit(value ="ShuraArenaPlayerScore", methodname = "initConfig")
public class ShuraArenaPlayerScore extends baseConfig<ShuraArenaPlayerScoreObject>{


private static ShuraArenaPlayerScore instance = null;

public static ShuraArenaPlayerScore getInstance() {

if (instance == null)
instance = new ShuraArenaPlayerScore();
return instance;

}


public static Map<Integer, ShuraArenaPlayerScoreObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (ShuraArenaPlayerScore) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"ShuraArenaPlayerScore");

for(Map e:ret)
{
put(e);
}

}

public static ShuraArenaPlayerScoreObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, ShuraArenaPlayerScoreObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setScore(MapHelper.getInt(e, "score"));


_ix_id.put(config.getId(),config);



}
}

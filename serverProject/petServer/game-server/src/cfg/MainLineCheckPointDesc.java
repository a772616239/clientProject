/*CREATED BY TOOL*/

package cfg;
import JsonTool.readJsonFile;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import model.base.baseConfig;
import datatool.MapHelper;
import annotation.annationInit;
import common.load.ServerConfig;

@annationInit(value ="MainLineCheckPointDesc", methodname = "initConfig")
public class MainLineCheckPointDesc extends baseConfig<MainLineCheckPointDescObject>{


private static MainLineCheckPointDesc instance = null;

public static MainLineCheckPointDesc getInstance() {

if (instance == null)
instance = new MainLineCheckPointDesc();
return instance;

}


public static Map<Integer, MainLineCheckPointDescObject> _ix_id = new HashMap<Integer, MainLineCheckPointDescObject>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MainLineCheckPointDesc) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "MainLineCheckPointDesc");

for(Map e:ret)
{
put(e);
}

}

public static MainLineCheckPointDescObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MainLineCheckPointDescObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setNodelist(MapHelper.getInts(e, "nodeList"));

config.setFinishedreward(MapHelper.getInt(e, "finishedReward"));


_ix_id.put(config.getId(),config);



}
}

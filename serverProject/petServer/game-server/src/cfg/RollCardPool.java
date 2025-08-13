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

@annationInit(value ="RollCardPool", methodname = "initConfig")
public class RollCardPool extends baseConfig<RollCardPoolObject>{


private static RollCardPool instance = null;

public static RollCardPool getInstance() {

if (instance == null)
instance = new RollCardPool();
return instance;

}


public static Map<Integer, RollCardPoolObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (RollCardPool) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"RollCardPool");

for(Map e:ret)
{
put(e);
}

}

public static RollCardPoolObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, RollCardPoolObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setReward(MapHelper.getInts(e, "reward"));

config.setGrade(MapHelper.getInt(e, "grade"));


_ix_id.put(config.getId(),config);



}
}

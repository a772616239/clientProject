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

@annationInit(value ="MainLineQuickOnHook", methodname = "initConfig")
public class MainLineQuickOnHook extends baseConfig<MainLineQuickOnHookObject>{


private static MainLineQuickOnHook instance = null;

public static MainLineQuickOnHook getInstance() {

if (instance == null)
instance = new MainLineQuickOnHook();
return instance;

}


public static Map<Integer, MainLineQuickOnHookObject> _ix_id = new HashMap<Integer, MainLineQuickOnHookObject>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MainLineQuickOnHook) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "MainLineQuickOnHook");

for(Map e:ret)
{
put(e);
}

}

public static MainLineQuickOnHookObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MainLineQuickOnHookObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setTime(MapHelper.getInt(e, "time"));

config.setConsumes(MapHelper.getInts(e, "consumes"));


_ix_id.put(config.getId(),config);



}
}

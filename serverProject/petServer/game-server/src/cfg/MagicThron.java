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

@annationInit(value ="MagicThron", methodname = "initConfig")
public class MagicThron extends baseConfig<MagicThronObject>{


private static MagicThron instance = null;

public static MagicThron getInstance() {

if (instance == null)
instance = new MagicThron();
return instance;

}


public static Map<Integer, MagicThronObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MagicThron) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MagicThron");

for(Map e:ret)
{
put(e);
}

}

public static MagicThronObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MagicThronObject config){

config.setId(MapHelper.getInt(e, "Id"));

config.setBoss(MapHelper.getInt(e, "boss"));

config.setFightpoint(MapHelper.getInts(e, "fightPoint"));


_ix_id.put(config.getId(),config);



}
}

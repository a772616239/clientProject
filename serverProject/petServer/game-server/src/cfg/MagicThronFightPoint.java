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

@annationInit(value ="MagicThronFightPoint", methodname = "initConfig")
public class MagicThronFightPoint extends baseConfig<MagicThronFightPointObject>{


private static MagicThronFightPoint instance = null;

public static MagicThronFightPoint getInstance() {

if (instance == null)
instance = new MagicThronFightPoint();
return instance;

}


public static Map<Integer, MagicThronFightPointObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MagicThronFightPoint) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MagicThronFightPoint");

for(Map e:ret)
{
put(e);
}

}

public static MagicThronFightPointObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MagicThronFightPointObject config){

config.setId(MapHelper.getInt(e, "Id"));

config.setFightmakeid(MapHelper.getInt(e, "fightmakeid"));

config.setBuff(MapHelper.getInt(e, "buff"));


_ix_id.put(config.getId(),config);



}
}

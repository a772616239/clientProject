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

@annationInit(value ="ShuraArenaConfig", methodname = "initConfig")
public class ShuraArenaConfig extends baseConfig<ShuraArenaConfigObject>{


private static ShuraArenaConfig instance = null;

public static ShuraArenaConfig getInstance() {

if (instance == null)
instance = new ShuraArenaConfig();
return instance;

}


public static Map<Integer, ShuraArenaConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (ShuraArenaConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"ShuraArenaConfig");

for(Map e:ret)
{
put(e);
}

}

public static ShuraArenaConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, ShuraArenaConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setRankrange(MapHelper.getInts(e, "rankRange"));

config.setShowsize(MapHelper.getInt(e, "showSize"));

config.setMonsterpool(MapHelper.getIntArray(e, "monsterPool"));

config.setBossawakelv(MapHelper.getInt(e, "bossAwakeLv"));

config.setBosspool(MapHelper.getInts(e, "bossPool"));


_ix_id.put(config.getId(),config);



}
}

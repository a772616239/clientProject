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

@annationInit(value ="PetEscapeBulletCfg", methodname = "initConfig")
public class PetEscapeBulletCfg extends baseConfig<PetEscapeBulletCfgObject>{


private static PetEscapeBulletCfg instance = null;

public static PetEscapeBulletCfg getInstance() {

if (instance == null)
instance = new PetEscapeBulletCfg();
return instance;

}


public static Map<Integer, PetEscapeBulletCfgObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (PetEscapeBulletCfg) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"PetEscapeBulletCfg");

for(Map e:ret)
{
put(e);
}

}

public static PetEscapeBulletCfgObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, PetEscapeBulletCfgObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setAddscore(MapHelper.getInt(e, "addScore"));


_ix_id.put(config.getId(),config);



}
}

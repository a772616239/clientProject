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

@annationInit(value ="HelpPetCfg", methodname = "initConfig")
public class HelpPetCfg extends baseConfig<HelpPetCfgObject>{


private static HelpPetCfg instance = null;

public static HelpPetCfg getInstance() {

if (instance == null)
instance = new HelpPetCfg();
return instance;

}


public static Map<String, HelpPetCfgObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (HelpPetCfg) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"HelpPetCfg");

for(Map e:ret)
{
put(e);
}

}

public static HelpPetCfgObject getById(String id){

return _ix_id.get(id);

}



public  void putToMem(Map e, HelpPetCfgObject config){

config.setId(MapHelper.getStr(e, "id"));

config.setPetcfgid(MapHelper.getInt(e, "petCfgId"));

config.setRarity(MapHelper.getInt(e, "rarity"));

config.setLevel(MapHelper.getInt(e, "level"));

config.setMainlinenode(MapHelper.getInt(e, "mainLineNode"));


_ix_id.put(config.getId(),config);



}
}

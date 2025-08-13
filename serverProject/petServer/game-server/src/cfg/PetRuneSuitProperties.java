/*CREATED BY TOOL*/

package cfg;
import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import model.base.baseConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@annationInit(value ="PetRuneSuitProperties", methodname = "initConfig")
public class PetRuneSuitProperties extends baseConfig<PetRuneSuitPropertiesObject>{


private static PetRuneSuitProperties instance = null;

public static PetRuneSuitProperties getInstance() {

if (instance == null)
instance = new PetRuneSuitProperties();
return instance;

}


public static Map<Integer, PetRuneSuitPropertiesObject> _ix_suitid = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (PetRuneSuitProperties) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"PetRuneSuitProperties");

for(Map e:ret)
{
put(e);
}

}

public static PetRuneSuitPropertiesObject getBySuitid(int suitid){

return _ix_suitid.get(suitid);

}



public  void putToMem(Map e, PetRuneSuitPropertiesObject config){

config.setSuitid(MapHelper.getInt(e, "suitId"));

config.setSuitname(MapHelper.getInt(e, "suitName"));

config.setSuitrarity(MapHelper.getInt(e, "suitRarity"));

config.setSuitproperties(MapHelper.getIntArray(e, "suitProperties"));

config.setBuffid(MapHelper.getIntArray(e, "buffID"));

config.setFightadd(MapHelper.getIntArray(e, "fightAdd"));


_ix_suitid.put(config.getSuitid(),config);



}
}

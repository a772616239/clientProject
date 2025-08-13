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

@annationInit(value ="VoidStoneConfig", methodname = "initConfig")
public class VoidStoneConfig extends baseConfig<VoidStoneConfigObject>{


private static VoidStoneConfig instance = null;

public static VoidStoneConfig getInstance() {

if (instance == null)
instance = new VoidStoneConfig();
return instance;

}


public static Map<Integer, VoidStoneConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (VoidStoneConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"VoidStoneConfig");

for(Map e:ret)
{
put(e);
}

}

public static VoidStoneConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, VoidStoneConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setRarity(MapHelper.getInt(e, "rarity"));

config.setLv(MapHelper.getInt(e, "lv"));

config.setResourcelv(MapHelper.getInt(e, "resourceLv"));

config.setNeedpetlv(MapHelper.getInt(e, "needPetLv"));

config.setProbability(MapHelper.getInt(e, "probability"));

config.setPropertytype(MapHelper.getInt(e, "propertyType"));

config.setProperties(MapHelper.getIntArray(e, "properties"));

config.setUpconsume(MapHelper.getIntArray(e, "upConsume"));

config.setChangeconsume(MapHelper.getIntArray(e, "changeConsume"));

config.setLockconsume(MapHelper.getIntArray(e, "lockConsume"));


_ix_id.put(config.getId(),config);



}
}

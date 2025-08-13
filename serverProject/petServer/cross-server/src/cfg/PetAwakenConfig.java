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

@annationInit(value ="PetAwakenConfig", methodname = "initConfig")
public class PetAwakenConfig extends baseConfig<PetAwakenConfigObject>{


private static PetAwakenConfig instance = null;

public static PetAwakenConfig getInstance() {

if (instance == null)
instance = new PetAwakenConfig();
return instance;

}


public static Map<Integer, PetAwakenConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (PetAwakenConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"PetAwakenConfig");

for(Map e:ret)
{
put(e);
}

}

public static PetAwakenConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, PetAwakenConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setAwaketype(MapHelper.getInt(e, "awakeType"));

config.setOrientation(MapHelper.getInt(e, "orientation"));

config.setUplvl(MapHelper.getInt(e, "upLvl"));

config.setPetlvl(MapHelper.getInt(e, "petLvl"));

config.setProperties(MapHelper.getIntArray(e, "properties"));

config.setNeedexp(MapHelper.getInt(e, "needExp"));


_ix_id.put(config.getId(),config);



}
}

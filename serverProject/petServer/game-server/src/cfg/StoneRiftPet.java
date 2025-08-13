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

@annationInit(value ="StoneRiftPet", methodname = "initConfig")
public class StoneRiftPet extends baseConfig<StoneRiftPetObject>{


private static StoneRiftPet instance = null;

public static StoneRiftPet getInstance() {

if (instance == null)
instance = new StoneRiftPet();
return instance;

}


public static Map<Integer, StoneRiftPetObject> _ix_pettype = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (StoneRiftPet) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"StoneRiftPet");

for(Map e:ret)
{
put(e);
}

}

public static StoneRiftPetObject getByPettype(int pettype){

return _ix_pettype.get(pettype);

}



public  void putToMem(Map e, StoneRiftPetObject config){

config.setPettype(MapHelper.getInt(e, "petType"));

config.setFunction(MapHelper.getInt(e, "function"));

config.setRitygain(MapHelper.getIntArray(e, "rityGain"));


_ix_pettype.put(config.getPettype(),config);



}
}

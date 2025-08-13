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

@annationInit(value ="MistNewObjConfig", methodname = "initConfig")
public class MistNewObjConfig extends baseConfig<MistNewObjConfigObject>{


private static MistNewObjConfig instance = null;

public static MistNewObjConfig getInstance() {

if (instance == null)
instance = new MistNewObjConfig();
return instance;

}


public static Map<Integer, MistNewObjConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MistNewObjConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MistNewObjConfig");

for(Map e:ret)
{
put(e);
}

}

public static MistNewObjConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MistNewObjConfigObject config){

config.setId(MapHelper.getInt(e, "Id"));

config.setObjtype(MapHelper.getInt(e, "objType"));

config.setInitpos(MapHelper.getInts(e, "initPos"));

config.setInitprop(MapHelper.getIntArray(e, "initProp"));

config.setRandprop(MapHelper.getInts(e, "randProp"));

config.setInitrand(MapHelper.getBoolean(e, "initRand"));

config.setRandposdata(MapHelper.getIntArray(e, "randPosData"));


_ix_id.put(config.getId(),config);



}
}

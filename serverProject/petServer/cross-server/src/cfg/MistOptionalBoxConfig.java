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

@annationInit(value ="MistOptionalBoxConfig", methodname = "initConfig")
public class MistOptionalBoxConfig extends baseConfig<MistOptionalBoxConfigObject>{


private static MistOptionalBoxConfig instance = null;

public static MistOptionalBoxConfig getInstance() {

if (instance == null)
instance = new MistOptionalBoxConfig();
return instance;

}


public static Map<Integer, MistOptionalBoxConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MistOptionalBoxConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MistOptionalBoxConfig");

for(Map e:ret)
{
put(e);
}

}

public static MistOptionalBoxConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MistOptionalBoxConfigObject config){

config.setId(MapHelper.getInt(e, "Id"));

config.setOptionallist(MapHelper.getIntArray(e, "OptionalList"));


_ix_id.put(config.getId(),config);



}
}

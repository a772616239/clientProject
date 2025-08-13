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

@annationInit(value ="MistMagicCycleConfig", methodname = "initConfig")
public class MistMagicCycleConfig extends baseConfig<MistMagicCycleConfigObject>{


private static MistMagicCycleConfig instance = null;

public static MistMagicCycleConfig getInstance() {

if (instance == null)
instance = new MistMagicCycleConfig();
return instance;

}


public static Map<Integer, MistMagicCycleConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MistMagicCycleConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MistMagicCycleConfig");

for(Map e:ret)
{
put(e);
}

}

public static MistMagicCycleConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MistMagicCycleConfigObject config){

config.setId(MapHelper.getInt(e, "Id"));

config.setLightnum(MapHelper.getInt(e, "lightNum"));

config.setCannotlightflag(MapHelper.getInts(e, "cannotLightFlag"));

config.setOperatedata(MapHelper.getIntArray(e, "operateData"));


_ix_id.put(config.getId(),config);



}
}

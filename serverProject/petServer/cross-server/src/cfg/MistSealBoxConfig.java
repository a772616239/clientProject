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

@annationInit(value ="MistSealBoxConfig", methodname = "initConfig")
public class MistSealBoxConfig extends baseConfig<MistSealBoxConfigObject>{


private static MistSealBoxConfig instance = null;

public static MistSealBoxConfig getInstance() {

if (instance == null)
instance = new MistSealBoxConfig();
return instance;

}


public static Map<Integer, MistSealBoxConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MistSealBoxConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MistSealBoxConfig");

for(Map e:ret)
{
put(e);
}

}

public static MistSealBoxConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MistSealBoxConfigObject config){

config.setId(MapHelper.getInt(e, "Id"));

config.setColumncount(MapHelper.getInt(e, "columnCount"));

config.setNeeditemlist(MapHelper.getInts(e, "needItemList"));

config.setFailedsubmitreward(MapHelper.getIntArray(e, "failedSubmitReward"));


_ix_id.put(config.getId(),config);



}
}

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

@annationInit(value ="MistCommonRewardConfig", methodname = "initConfig")
public class MistCommonRewardConfig extends baseConfig<MistCommonRewardConfigObject>{


private static MistCommonRewardConfig instance = null;

public static MistCommonRewardConfig getInstance() {

if (instance == null)
instance = new MistCommonRewardConfig();
return instance;

}


public static Map<Integer, MistCommonRewardConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MistCommonRewardConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MistCommonRewardConfig");

for(Map e:ret)
{
put(e);
}

}

public static MistCommonRewardConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MistCommonRewardConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setCommonrewardlist(MapHelper.getIntArray(e, "commonRewardList"));


_ix_id.put(config.getId(),config);



}
}

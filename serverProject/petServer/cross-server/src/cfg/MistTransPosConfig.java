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

@annationInit(value ="MistTransPosConfig", methodname = "initConfig")
public class MistTransPosConfig extends baseConfig<MistTransPosConfigObject>{


private static MistTransPosConfig instance = null;

public static MistTransPosConfig getInstance() {

if (instance == null)
instance = new MistTransPosConfig();
return instance;

}


public static Map<Integer, MistTransPosConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MistTransPosConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MistTransPosConfig");

for(Map e:ret)
{
put(e);
}

}

public static MistTransPosConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MistTransPosConfigObject config){

config.setId(MapHelper.getInt(e, "Id"));

config.setTransposlist(MapHelper.getIntArray(e, "TransPosList"));


_ix_id.put(config.getId(),config);



}
}

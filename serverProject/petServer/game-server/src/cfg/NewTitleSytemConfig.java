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

@annationInit(value ="NewTitleSytemConfig", methodname = "initConfig")
public class NewTitleSytemConfig extends baseConfig<NewTitleSytemConfigObject>{


private static NewTitleSytemConfig instance = null;

public static NewTitleSytemConfig getInstance() {

if (instance == null)
instance = new NewTitleSytemConfig();
return instance;

}


public static Map<Integer, NewTitleSytemConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (NewTitleSytemConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"NewTitleSytemConfig");

for(Map e:ret)
{
put(e);
}

}

public static NewTitleSytemConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, NewTitleSytemConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setAddproperty(MapHelper.getIntArray(e, "addProperty"));

config.setLimittime(MapHelper.getInt(e, "limitTime"));

config.setServername(MapHelper.getInt(e, "serverName"));


_ix_id.put(config.getId(),config);



}
}

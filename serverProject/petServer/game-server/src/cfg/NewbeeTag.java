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

@annationInit(value ="NewbeeTag", methodname = "initConfig")
public class NewbeeTag extends baseConfig<NewbeeTagObject>{


private static NewbeeTag instance = null;

public static NewbeeTag getInstance() {

if (instance == null)
instance = new NewbeeTag();
return instance;

}


public static Map<Integer, NewbeeTagObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (NewbeeTag) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"NewbeeTag");

for(Map e:ret)
{
put(e);
}

}

public static NewbeeTagObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, NewbeeTagObject config){

config.setId(MapHelper.getInt(e, "Id"));

config.setComment(MapHelper.getStr(e, "Comment"));

config.setJoinappsflyer(MapHelper.getBoolean(e, "joinAppsFlyer"));


_ix_id.put(config.getId(),config);



}
}

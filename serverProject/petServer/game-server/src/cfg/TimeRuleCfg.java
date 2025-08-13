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

@annationInit(value ="TimeRuleCfg", methodname = "initConfig")
public class TimeRuleCfg extends baseConfig<TimeRuleCfgObject>{


private static TimeRuleCfg instance = null;

public static TimeRuleCfg getInstance() {

if (instance == null)
instance = new TimeRuleCfg();
return instance;

}


public static Map<Integer, TimeRuleCfgObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (TimeRuleCfg) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"TimeRuleCfg");

for(Map e:ret)
{
put(e);
}

}

public static TimeRuleCfgObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, TimeRuleCfgObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setType(MapHelper.getInt(e, "type"));

config.setFather_id(MapHelper.getInt(e, "father_id"));

config.setBegin_time(MapHelper.getStr(e, "begin_time"));

config.setEnd_time(MapHelper.getStr(e, "end_time"));

config.setOpen_time(MapHelper.getStr(e, "open_time"));


_ix_id.put(config.getId(),config);



}
}

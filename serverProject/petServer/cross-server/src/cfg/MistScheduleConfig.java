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

@annationInit(value ="MistScheduleConfig", methodname = "initConfig")
public class MistScheduleConfig extends baseConfig<MistScheduleConfigObject>{


private static MistScheduleConfig instance = null;

public static MistScheduleConfig getInstance() {

if (instance == null)
instance = new MistScheduleConfig();
return instance;

}


public static Map<Integer, MistScheduleConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MistScheduleConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MistScheduleConfig");

for(Map e:ret)
{
put(e);
}

}

public static MistScheduleConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MistScheduleConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setMistlevel(MapHelper.getInt(e, "mistLevel"));

config.setScheduletype(MapHelper.getInt(e, "scheduleType"));

config.setDailystarttime(MapHelper.getInt(e, "dailyStartTime"));

config.setDuration(MapHelper.getInt(e, "duration"));

config.setInterval(MapHelper.getInt(e, "interval"));

config.setClosedsection(MapHelper.getIntArray(e, "closedSection"));

config.setRemoveclosedsectionobj(MapHelper.getBoolean(e, "removeClosedSectionObj"));

config.setInitobjdata(MapHelper.getInts(e, "initObjData"));

config.setRefreshobjdata(MapHelper.getInts(e, "refreshObjData"));


_ix_id.put(config.getId(),config);



}
}

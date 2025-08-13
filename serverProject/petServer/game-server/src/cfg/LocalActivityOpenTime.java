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

@annationInit(value ="LocalActivityOpenTime", methodname = "initConfig")
public class LocalActivityOpenTime extends baseConfig<LocalActivityOpenTimeObject>{


private static LocalActivityOpenTime instance = null;

public static LocalActivityOpenTime getInstance() {

if (instance == null)
instance = new LocalActivityOpenTime();
return instance;

}


public static Map<Integer, LocalActivityOpenTimeObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (LocalActivityOpenTime) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"LocalActivityOpenTime");

for(Map e:ret)
{
put(e);
}

}

public static LocalActivityOpenTimeObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, LocalActivityOpenTimeObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setTitle(MapHelper.getInt(e, "title"));

config.setDesc(MapHelper.getInt(e, "desc"));

config.setDetail(MapHelper.getInt(e, "detail"));

config.setIcon(MapHelper.getStr(e, "icon"));

config.setStartdistime(MapHelper.getStr(e, "startDisTime"));

config.setBegintime(MapHelper.getStr(e, "beginTime"));

config.setEndtime(MapHelper.getStr(e, "endTime"));

config.setOverdistime(MapHelper.getStr(e, "overDisTime"));

config.setMissionlist(MapHelper.getInts(e, "missionList"));

config.setTabtype(MapHelper.getInt(e, "tabType"));

config.setReddottype(MapHelper.getInt(e, "redDotType"));


_ix_id.put(config.getId(),config);



}
}

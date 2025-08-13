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

@annationInit(value ="ArenaRobotConfig", methodname = "initConfig")
public class ArenaRobotConfig extends baseConfig<ArenaRobotConfigObject>{


private static ArenaRobotConfig instance = null;

public static ArenaRobotConfig getInstance() {

if (instance == null)
instance = new ArenaRobotConfig();
return instance;

}


public static Map<Integer, ArenaRobotConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (ArenaRobotConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"ArenaRobotConfig");

for(Map e:ret)
{
put(e);
}

}

public static ArenaRobotConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, ArenaRobotConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setDan(MapHelper.getInt(e, "dan"));

config.setStartscore(MapHelper.getInt(e, "startScore"));

config.setEndscore(MapHelper.getInt(e, "endScore"));

config.setNeedcount(MapHelper.getInt(e, "needCount"));

config.setName(MapHelper.getStr(e, "name"));

config.setPetcount(MapHelper.getIntArray(e, "petCount"));

config.setPetlvrange(MapHelper.getInts(e, "petLvRange"));

config.setPetlvrange2(MapHelper.getInts(e, "petLvRange2"));

config.setPetlvrange3(MapHelper.getInts(e, "petLvRange3"));

config.setPetwakeuprange(MapHelper.getInts(e, "petWakeUprange"));

config.setExproperty(MapHelper.getIntArray(e, "exproperty"));

config.setNamestr(MapHelper.getInt(e, "nameStr"));


_ix_id.put(config.getId(),config);



}
}

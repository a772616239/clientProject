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

@annationInit(value ="StoneRiftRobot", methodname = "initConfig")
public class StoneRiftRobot extends baseConfig<StoneRiftRobotObject>{


private static StoneRiftRobot instance = null;

public static StoneRiftRobot getInstance() {

if (instance == null)
instance = new StoneRiftRobot();
return instance;

}


public static Map<Integer, StoneRiftRobotObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (StoneRiftRobot) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"StoneRiftRobot");

for(Map e:ret)
{
put(e);
}

}

public static StoneRiftRobotObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, StoneRiftRobotObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setStoneriftlevel(MapHelper.getInts(e, "stoneRiftLevel"));

config.setFactorycfg(MapHelper.getIntArray(e, "factoryCfg"));

config.setEfficiency(MapHelper.getInts(e, "efficiency"));

config.setStoremax(MapHelper.getInts(e, "storeMax"));

config.setDurable(MapHelper.getInts(e, "durable"));

config.setDefendpet(MapHelper.getIntArray(e, "defendPet"));

config.setStoreprogress(MapHelper.getInts(e, "storeProgress"));


_ix_id.put(config.getId(),config);



}
}

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

@annationInit(value ="CrossArenaRobot", methodname = "initConfig")
public class CrossArenaRobot extends baseConfig<CrossArenaRobotObject>{


private static CrossArenaRobot instance = null;

public static CrossArenaRobot getInstance() {

if (instance == null)
instance = new CrossArenaRobot();
return instance;

}


public static Map<Integer, CrossArenaRobotObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (CrossArenaRobot) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"CrossArenaRobot");

for(Map e:ret)
{
put(e);
}

}

public static CrossArenaRobotObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, CrossArenaRobotObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setRank(MapHelper.getInt(e, "rank"));

config.setHonrlv(MapHelper.getInt(e, "honrLv"));

config.setTeam(MapHelper.getInts(e, "team"));

config.setLevel(MapHelper.getInts(e, "level"));

config.setRarity(MapHelper.getInts(e, "rarity"));

config.setWinnum(MapHelper.getInt(e, "winNum"));

config.setUsetype(MapHelper.getInt(e, "useType"));

config.setDifficult(MapHelper.getInt(e, "difficult"));


_ix_id.put(config.getId(),config);



}
}

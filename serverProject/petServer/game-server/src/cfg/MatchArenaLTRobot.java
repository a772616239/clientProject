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

@annationInit(value ="MatchArenaLTRobot", methodname = "initConfig")
public class MatchArenaLTRobot extends baseConfig<MatchArenaLTRobotObject>{


private static MatchArenaLTRobot instance = null;

public static MatchArenaLTRobot getInstance() {

if (instance == null)
instance = new MatchArenaLTRobot();
return instance;

}


public static Map<Integer, MatchArenaLTRobotObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MatchArenaLTRobot) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MatchArenaLTRobot");

for(Map e:ret)
{
put(e);
}

}

public static MatchArenaLTRobotObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MatchArenaLTRobotObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setRank(MapHelper.getInt(e, "rank"));

config.setTeam(MapHelper.getInts(e, "team"));

config.setLevel(MapHelper.getInts(e, "level"));

config.setRarity(MapHelper.getInts(e, "rarity"));

config.setWinnum(MapHelper.getInt(e, "winNum"));


_ix_id.put(config.getId(),config);



}
}

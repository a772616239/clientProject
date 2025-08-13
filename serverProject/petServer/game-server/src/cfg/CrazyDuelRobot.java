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

@annationInit(value ="CrazyDuelRobot", methodname = "initConfig")
public class CrazyDuelRobot extends baseConfig<CrazyDuelRobotObject>{


private static CrazyDuelRobot instance = null;

public static CrazyDuelRobot getInstance() {

if (instance == null)
instance = new CrazyDuelRobot();
return instance;

}


public static Map<Integer, CrazyDuelRobotObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (CrazyDuelRobot) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"CrazyDuelRobot");

for(Map e:ret)
{
put(e);
}

}

public static CrazyDuelRobotObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, CrazyDuelRobotObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setLevel(MapHelper.getInt(e, "level"));

config.setStartscore(MapHelper.getInt(e, "startScore"));

config.setEndscore(MapHelper.getInt(e, "endScore"));

config.setNeedcount(MapHelper.getInt(e, "needCount"));

config.setPetcount(MapHelper.getIntArray(e, "petCount"));

config.setPetlvrange(MapHelper.getInts(e, "petLvRange"));

config.setHonrlv(MapHelper.getInt(e, "honrLv"));

config.setOpenfloor(MapHelper.getInt(e, "openFloor"));


_ix_id.put(config.getId(),config);



}
}

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

@annationInit(value ="ArenaLeague", methodname = "initConfig")
public class ArenaLeague extends baseConfig<ArenaLeagueObject>{


private static ArenaLeague instance = null;

public static ArenaLeague getInstance() {

if (instance == null)
instance = new ArenaLeague();
return instance;

}


public static Map<Integer, ArenaLeagueObject> _ix_id = new HashMap<Integer, ArenaLeagueObject>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (ArenaLeague) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"ArenaLeague");

for(Map e:ret)
{
put(e);
}

}

public static ArenaLeagueObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, ArenaLeagueObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setStartdan(MapHelper.getInt(e, "startDan"));

config.setEnddan(MapHelper.getInt(e, "endDan"));

config.setCanuseteamnumandpetcount(MapHelper.getIntArray(e, "canUseTeamNumAndPetCount"));


_ix_id.put(config.getId(),config);



}
}

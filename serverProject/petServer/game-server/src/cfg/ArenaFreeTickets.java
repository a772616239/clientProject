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

@annationInit(value ="ArenaFreeTickets", methodname = "initConfig")
public class ArenaFreeTickets extends baseConfig<ArenaFreeTicketsObject>{


private static ArenaFreeTickets instance = null;

public static ArenaFreeTickets getInstance() {

if (instance == null)
instance = new ArenaFreeTickets();
return instance;

}


public static Map<Integer, ArenaFreeTicketsObject> _ix_id = new HashMap<Integer, ArenaFreeTicketsObject>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (ArenaFreeTickets) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"ArenaFreeTickets");

for(Map e:ret)
{
put(e);
}

}

public static ArenaFreeTicketsObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, ArenaFreeTicketsObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setSendtime(MapHelper.getInts(e, "sendTime"));

config.setLvlimit(MapHelper.getInt(e, "lvLimit"));

config.setMailtemplate(MapHelper.getInt(e, "mailTemplate"));


_ix_id.put(config.getId(),config);



}
}

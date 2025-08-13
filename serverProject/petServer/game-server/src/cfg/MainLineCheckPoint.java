/*CREATED BY TOOL*/

package cfg;
import JsonTool.readJsonFile;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import model.base.baseConfig;
import datatool.MapHelper;
import annotation.annationInit;
import common.load.ServerConfig;

@annationInit(value ="MainLineCheckPoint", methodname = "initConfig")
public class MainLineCheckPoint extends baseConfig<MainLineCheckPointObject>{


private static MainLineCheckPoint instance = null;

public static MainLineCheckPoint getInstance() {

if (instance == null)
instance = new MainLineCheckPoint();
return instance;

}


public static Map<Integer, MainLineCheckPointObject> _ix_id = new HashMap<Integer, MainLineCheckPointObject>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MainLineCheckPoint) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "MainLineCheckPoint");

for(Map e:ret)
{
put(e);
}

}

public static MainLineCheckPointObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MainLineCheckPointObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setType(MapHelper.getInt(e, "type"));

config.setSubtype(MapHelper.getInt(e, "subType"));

config.setUnlocklv(MapHelper.getInt(e, "unlockLv"));

config.setMainlinemissionid(MapHelper.getInt(e, "mainLineMissionId"));

config.setIsthelastpoint(MapHelper.getBoolean(e, "isTheLastPoint"));

config.setBeforecheckpoint(MapHelper.getInt(e, "beforeCheckPoint"));

config.setAftercheckpoint(MapHelper.getInt(e, "afterCheckPoint"));

config.setNodelist(MapHelper.getInts(e, "nodeList"));

config.setCorrectorder(MapHelper.getInts(e, "correctOrder"));

config.setUnlockmistlv(MapHelper.getInt(e, "unlockMIstLv"));


_ix_id.put(config.getId(),config);



}
}

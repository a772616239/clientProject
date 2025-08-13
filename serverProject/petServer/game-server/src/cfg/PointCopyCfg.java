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

@annationInit(value ="PointCopyCfg", methodname = "initConfig")
public class PointCopyCfg extends baseConfig<PointCopyCfgObject>{


private static PointCopyCfg instance = null;

public static PointCopyCfg getInstance() {

if (instance == null)
instance = new PointCopyCfg();
return instance;

}


public static Map<Integer, PointCopyCfgObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (PointCopyCfg) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"PointCopyCfg");

for(Map e:ret)
{
put(e);
}

}

public static PointCopyCfgObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, PointCopyCfgObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setMissiontype(MapHelper.getInt(e, "missionType"));

config.setFightmakeid(MapHelper.getInt(e, "fightmakeId"));

config.setConsume(MapHelper.getInt(e, "consume"));

config.setPointtarget(MapHelper.getInt(e, "pointTarget"));

config.setReward(MapHelper.getIntArray(e, "reward"));

config.setWinunlock(MapHelper.getInt(e, "winUnlock"));


_ix_id.put(config.getId(),config);



}
}

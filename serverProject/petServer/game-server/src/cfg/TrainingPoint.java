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

@annationInit(value ="TrainingPoint", methodname = "initConfig")
public class TrainingPoint extends baseConfig<TrainingPointObject>{


private static TrainingPoint instance = null;

public static TrainingPoint getInstance() {

if (instance == null)
instance = new TrainingPoint();
return instance;

}


public static Map<Integer, TrainingPointObject> _ix_pointid = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (TrainingPoint) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"TrainingPoint");

for(Map e:ret)
{
put(e);
}

}

public static TrainingPointObject getByPointid(int pointid){

return _ix_pointid.get(pointid);

}



public  void putToMem(Map e, TrainingPointObject config){

config.setPointid(MapHelper.getInt(e, "pointId"));

config.setMapid(MapHelper.getInt(e, "mapId"));

config.setChildnode(MapHelper.getInts(e, "childNode"));

config.setPosition(MapHelper.getInts(e, "position"));

config.setIsfight(MapHelper.getInt(e, "isFight"));

config.setType(MapHelper.getInt(e, "type"));

config.setFightmakeid(MapHelper.getInts(e, "fightMakeId"));

config.setStarmax(MapHelper.getInt(e, "starMax"));

config.setBlessing(MapHelper.getInts(e, "blessing"));

config.setMolings(MapHelper.getIntArray(e, "molings"));

config.setAwards(MapHelper.getInts(e, "awards"));

config.setJifen(MapHelper.getInts(e, "jifen"));

config.setResetpos(MapHelper.getInt(e, "resetPos"));

config.setPrepoint(MapHelper.getIntArray(e, "prePoint"));

config.setLevel(MapHelper.getInt(e, "level"));

config.setPointlevel(MapHelper.getInt(e, "pointlevel"));

config.setCallcp(MapHelper.getInt(e, "callcp"));

config.setRevenge(MapHelper.getInts(e, "revenge"));

config.setPoolid(MapHelper.getInt(e, "poolId"));

config.setPointgroup(MapHelper.getInt(e, "pointGroup"));

config.setChangepoint(MapHelper.getInts(e, "changePoint"));

config.setFulishop(MapHelper.getInt(e, "fulishop"));

config.setNpc(MapHelper.getInt(e, "npc"));

config.setBloodmonster(MapHelper.getInts(e, "bloodmonster"));


_ix_pointid.put(config.getPointid(),config);



}
}

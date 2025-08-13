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

@annationInit(value ="MistWordMapInfoConfig", methodname = "initConfig")
public class MistWordMapInfoConfig extends baseConfig<MistWordMapInfoConfigObject>{


private static MistWordMapInfoConfig instance = null;

public static MistWordMapInfoConfig getInstance() {

if (instance == null)
instance = new MistWordMapInfoConfig();
return instance;

}


public static Map<Integer, MistWordMapInfoConfigObject> _ix_mapid = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MistWordMapInfoConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MistWordMapInfoConfig");

for(Map e:ret)
{
put(e);
}

}

public static MistWordMapInfoConfigObject getByMapid(int mapid){

return _ix_mapid.get(mapid);

}



public  void putToMem(Map e, MistWordMapInfoConfigObject config){

config.setMapid(MapHelper.getInt(e, "MapId"));

config.setSweepmissions(MapHelper.getIntArray(e, "sweepMissions"));

config.setTargetmission(MapHelper.getInts(e, "targetMission"));

config.setUnlockcondition(MapHelper.getInts(e, "unlockCondition"));

config.setFinishreward(MapHelper.getIntArray(e, "finishReward"));

config.setSweepreward(MapHelper.getIntArray(e, "sweepReward"));

config.setSweeprewardcount(MapHelper.getInt(e, "sweepRewardCount"));

config.setSweepconsume(MapHelper.getIntArray(e, "sweepConsume"));


_ix_mapid.put(config.getMapid(),config);



}
}

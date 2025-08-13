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

@annationInit(value ="TrainingMap", methodname = "initConfig")
public class TrainingMap extends baseConfig<TrainingMapObject>{


private static TrainingMap instance = null;

public static TrainingMap getInstance() {

if (instance == null)
instance = new TrainingMap();
return instance;

}


public static Map<Integer, TrainingMapObject> _ix_mapid = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (TrainingMap) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"TrainingMap");

for(Map e:ret)
{
put(e);
}

}

public static TrainingMapObject getByMapid(int mapid){

return _ix_mapid.get(mapid);

}



public  void putToMem(Map e, TrainingMapObject config){

config.setMapid(MapHelper.getInt(e, "mapId"));

config.setUnlock(MapHelper.getInt(e, "unlock"));

config.setOpentime(MapHelper.getInt(e, "openTime"));

config.setShop(MapHelper.getIntArray(e, "shop"));

config.setStar(MapHelper.getInt(e, "star"));

config.setRankaward(MapHelper.getInts(e, "rankAward"));

config.setScoretasklist(MapHelper.getInts(e, "scoreTaskList"));

config.setMailtempid(MapHelper.getInt(e, "mailTempId"));

config.setShops(MapHelper.getInts(e, "shops"));

config.setUnlocklevel(MapHelper.getIntArray(e, "unlockLevel"));

config.setReportcard(MapHelper.getInts(e, "reportCard"));

config.setChoice(MapHelper.getInts(e, "choice"));

config.setHelpmonsterdata(MapHelper.getInts(e, "helpMonsterData"));

config.setRankplayerbeyonddata(MapHelper.getIntArray(e, "rankPlayerBeyondData"));

config.setUnrankplayerbeyonddata1(MapHelper.getInts(e, "unrankPlayerBeyondData1"));

config.setUnrankplayerbeyonddata2(MapHelper.getInts(e, "unrankPlayerBeyondData2"));


_ix_mapid.put(config.getMapid(),config);



}
}

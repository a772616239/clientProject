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

@annationInit(value ="ArenaConfig", methodname = "initConfig")
public class ArenaConfig extends baseConfig<ArenaConfigObject>{


private static ArenaConfig instance = null;

public static ArenaConfig getInstance() {

if (instance == null)
instance = new ArenaConfig();
return instance;

}


public static Map<Integer, ArenaConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (ArenaConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"ArenaConfig");

for(Map e:ret)
{
put(e);
}

}

public static ArenaConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, ArenaConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setStartdan(MapHelper.getInt(e, "startDan"));

config.setRankingdisplaycount(MapHelper.getInt(e, "rankingDisplayCount"));

config.setDailyfreetimes(MapHelper.getInt(e, "dailyFreeTimes"));

config.setRefreshinterval(MapHelper.getInt(e, "refreshInterval"));

config.setDansettleinterval(MapHelper.getInt(e, "danSettleInterval"));

config.setDansettlehour(MapHelper.getInt(e, "danSettleHour"));

config.setChallengeconsume(MapHelper.getInts(e, "challengeConsume"));

config.setTicketprice(MapHelper.getInts(e, "ticketPrice"));

config.setSaverecordcount(MapHelper.getInt(e, "saveRecordCount"));

config.setRankrefrshtime(MapHelper.getInt(e, "rankRefrshTime"));

config.setVictoryreward(MapHelper.getInt(e, "victoryReward"));

config.setFailedreward(MapHelper.getInt(e, "failedReward"));

config.setExpectstrength(MapHelper.getInt(e, "expectStrength"));

config.setVictorycorrection(MapHelper.getInt(e, "victoryCorrection"));

config.setFailurecorrection(MapHelper.getInt(e, "failureCorrection"));

config.setAverageexpected(MapHelper.getInt(e, "averageExpected"));

config.setDailymission(MapHelper.getInts(e, "dailyMission"));

config.setChallengemission(MapHelper.getInts(e, "challengeMission"));


_ix_id.put(config.getId(),config);



}
}

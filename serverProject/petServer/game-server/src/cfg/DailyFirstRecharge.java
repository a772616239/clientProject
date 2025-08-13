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

@annationInit(value ="DailyFirstRecharge", methodname = "initConfig")
public class DailyFirstRecharge extends baseConfig<DailyFirstRechargeObject>{


private static DailyFirstRecharge instance = null;

public static DailyFirstRecharge getInstance() {

if (instance == null)
instance = new DailyFirstRecharge();
return instance;

}


public static Map<Integer, DailyFirstRechargeObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (DailyFirstRecharge) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"DailyFirstRecharge");

for(Map e:ret)
{
put(e);
}

}

public static DailyFirstRechargeObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, DailyFirstRechargeObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setSegmentdays(MapHelper.getInts(e, "segmentDays"));

config.setDailyreward(MapHelper.getInts(e, "dailyReward"));

config.setRechargecycle(MapHelper.getInt(e, "rechargeCycle"));

config.setBigrewardindex(MapHelper.getInts(e, "bigRewardIndex"));

config.setBigrewardmarqueeid(MapHelper.getInt(e, "bigRewardMarqueeId"));

config.setExtratimesday(MapHelper.getInts(e, "extraTimesDay"));

config.setTowerreward(MapHelper.getIntArray(e, "towerReward"));

config.setTowerweight(MapHelper.getIntArray(e, "towerWeight"));


_ix_id.put(config.getId(),config);



}
}

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

@annationInit(value ="DemonDescendsConfig", methodname = "initConfig")
public class DemonDescendsConfig extends baseConfig<DemonDescendsConfigObject>{


private static DemonDescendsConfig instance = null;

public static DemonDescendsConfig getInstance() {

if (instance == null)
instance = new DemonDescendsConfig();
return instance;

}


public static Map<Integer, DemonDescendsConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (DemonDescendsConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"DemonDescendsConfig");

for(Map e:ret)
{
put(e);
}

}

public static DemonDescendsConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, DemonDescendsConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setDrawuseitem(MapHelper.getInts(e, "drawUseItem"));

config.setRechargerewardneedcoupon(MapHelper.getInt(e, "rechargeRewardNeedCoupon"));

config.setEachrechargerewards(MapHelper.getInt(e, "eachRechargeRewards"));

config.setBuyupperlimit(MapHelper.getInt(e, "buyUpperLimit"));

config.setPrice(MapHelper.getInts(e, "price"));

config.setDailymission(MapHelper.getInts(e, "dailyMission"));

config.setEachdrawscore(MapHelper.getInt(e, "eachDrawScore"));

config.setRankingneedminscore(MapHelper.getInt(e, "rankingNeedMinScore"));

config.setScorerankingtemplate(MapHelper.getInt(e, "scoreRankingTemplate"));


_ix_id.put(config.getId(),config);



}
}

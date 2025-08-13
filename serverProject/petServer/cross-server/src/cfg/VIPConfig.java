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

@annationInit(value ="VIPConfig", methodname = "initConfig")
public class VIPConfig extends baseConfig<VIPConfigObject>{


private static VIPConfig instance = null;

public static VIPConfig getInstance() {

if (instance == null)
instance = new VIPConfig();
return instance;

}


public static Map<Integer, VIPConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (VIPConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"VIPConfig");

for(Map e:ret)
{
put(e);
}

}

public static VIPConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, VIPConfigObject config){

config.setId(MapHelper.getInt(e, "ID"));

config.setNeedtotalexp(MapHelper.getInt(e, "needTotalExp"));

config.setPetbagimprove(MapHelper.getInt(e, "petBagImprove"));

config.setRunebagimprove(MapHelper.getInt(e, "runeBagImprove"));

config.setLvgiftbag(MapHelper.getIntArray(e, "LvGiftBag"));

config.setGlodexaddtion(MapHelper.getInt(e, "glodExAddtion"));

config.setGoldexupperlimit(MapHelper.getInt(e, "goldExUpperLimit"));

config.setOnhookexaddtion(MapHelper.getIntArray(e, "onHookExAddtion"));

config.setPetmissiondailytimes(MapHelper.getInt(e, "petMissionDailyTimes"));

config.setPatrolreborntimes(MapHelper.getInt(e, "patrolRebornTimes"));

config.setBuyrescopylimit(MapHelper.getInt(e, "buyResCopyLimit"));

config.setQuickonhooktimes(MapHelper.getInt(e, "quickOnhookTimes"));

config.setGainmistbagextrarate(MapHelper.getInt(e, "gainMistBagExtraRate"));

config.setBoutiquemanualrefreshlimit(MapHelper.getInt(e, "boutiqueManualRefreshLimit"));

config.setBravechallengereborntimes(MapHelper.getInt(e, "braveChallengeRebornTimes"));

config.setArenaticketbuylimit(MapHelper.getInt(e, "arenaTicketBuyLimit"));

config.setGhostbusterfreecount(MapHelper.getInt(e, "ghostBusterFreeCount"));

config.setOnhooktime(MapHelper.getInt(e, "onHookTime"));

config.setRescopyexaddtion(MapHelper.getInt(e, "resCopyExAddtion"));

config.setPlusboostowertimes(MapHelper.getInt(e, "plusBoosTowerTimes"));

config.setMinedailyrewardexrate(MapHelper.getInt(e, "mineDailyRewardExRate"));

config.setMineexaddition(MapHelper.getInt(e, "mineExAddition"));

config.setBosstowerchallengetimes(MapHelper.getInt(e, "bossTowerChallengeTimes"));

config.setCommondrawspecialtimes(MapHelper.getInt(e, "commonDrawSpecialTimes"));

config.setCommonspecialdrawpool(MapHelper.getInts(e, "commonSpecialDrawPool"));

config.setFriendlimit(MapHelper.getInt(e, "friendLimit"));

config.setFrienditemgainlimit(MapHelper.getInt(e, "friendItemGainLimit"));

config.setBuylvlgiftprice(MapHelper.getIntArray(e, "buyLvlGiftPrice"));


_ix_id.put(config.getId(),config);



}
}

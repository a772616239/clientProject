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

@annationInit(value ="StoneRiftConfig", methodname = "initConfig")
public class StoneRiftConfig extends baseConfig<StoneRiftConfigObject>{


private static StoneRiftConfig instance = null;

public static StoneRiftConfig getInstance() {

if (instance == null)
instance = new StoneRiftConfig();
return instance;

}


public static Map<Integer, StoneRiftConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (StoneRiftConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"StoneRiftConfig");

for(Map e:ret)
{
put(e);
}

}

public static StoneRiftConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, StoneRiftConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setOverloadcd(MapHelper.getInt(e, "overLoadCd"));

config.setOverloadduration(MapHelper.getInt(e, "overLoadDuration"));

config.setOverloadefficiency(MapHelper.getInt(e, "overloadEfficiency"));

config.setEventduration(MapHelper.getInt(e, "eventDuration"));

config.setEventreward(MapHelper.getIntArray(e, "eventReward"));

config.setEventweight(MapHelper.getIntArray(e, "eventWeight"));

config.setBuffeffect(MapHelper.getIntArray(e, "buffEffect"));

config.setTimeitem(MapHelper.getInt(e, "timeItem"));

config.setInitdurable(MapHelper.getInt(e, "initDurable"));

config.setPlayermappool(MapHelper.getInts(e, "playerMapPool"));

config.setWorldplayericonpool(MapHelper.getInts(e, "worldPlayerIconPool"));

config.setWorldmappool(MapHelper.getInts(e, "worldMapPool"));

config.setClaiminterval(MapHelper.getInt(e, "claimInterval"));

config.setDurableefficiency(MapHelper.getIntArray(e, "durableEfficiency"));

config.setClaimbasemulticritical(MapHelper.getInt(e, "claimBaseMultiCritical"));

config.setStealbasemulticritical(MapHelper.getInt(e, "stealBaseMultiCritical"));

config.setMindurable(MapHelper.getInt(e, "minDurable"));

config.setDefendpetrarity(MapHelper.getInts(e, "defendPetRarity"));

config.setCurrencyaitemid(MapHelper.getInt(e, "currencyAItemId"));

config.setCurrencyaoutput(MapHelper.getInts(e, "currencyAOutPut"));

config.setCurrencybgainpro(MapHelper.getInt(e, "currencyBGainPro"));

config.setCurrencyboutput(MapHelper.getInts(e, "currencyBOutPut"));

config.setRefreshbuyconsume(MapHelper.getInts(e, "refreshBuyConsume"));

config.setStealbuyconsume(MapHelper.getInts(e, "stealBuyConsume"));

config.setCanstealtime(MapHelper.getInt(e, "canStealTime"));

config.setWorldmaprefreshtime(MapHelper.getInt(e, "worldMapRefreshTime"));

config.setCanstolentime(MapHelper.getInt(e, "canStolenTime"));

config.setMapsize(MapHelper.getInt(e, "mapSize"));

config.setPlayersize(MapHelper.getInt(e, "playerSize"));

config.setDefendpetneedriftlv(MapHelper.getInt(e, "defendPetNeedRiftLv"));

config.setStalrareitem(MapHelper.getIntArray(e, "stalRareItem"));


_ix_id.put(config.getId(),config);



}
}

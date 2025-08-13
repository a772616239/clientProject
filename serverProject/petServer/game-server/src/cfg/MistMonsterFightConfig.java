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

@annationInit(value ="MistMonsterFightConfig", methodname = "initConfig")
public class MistMonsterFightConfig extends baseConfig<MistMonsterFightConfigObject>{


private static MistMonsterFightConfig instance = null;

public static MistMonsterFightConfig getInstance() {

if (instance == null)
instance = new MistMonsterFightConfig();
return instance;

}


public static Map<Integer, MistMonsterFightConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MistMonsterFightConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MistMonsterFightConfig");

for(Map e:ret)
{
put(e);
}

}

public static MistMonsterFightConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MistMonsterFightConfigObject config){

config.setId(MapHelper.getInt(e, "Id"));

config.setFightmakeid(MapHelper.getInts(e, "FightMakeId"));

config.setBattlereward(MapHelper.getIntArray(e, "BattleReward"));

config.setBatterrewardobj(MapHelper.getIntArray(e, "BatterRewardObj"));

config.setBattleteamreward(MapHelper.getIntArray(e, "BattleTeamReward"));

config.setRewardjewelrycount(MapHelper.getIntArray(e, "rewardJewelryCount"));

config.setMonstertype(MapHelper.getInt(e, "monsterType"));

config.setRewardlavabadgecount(MapHelper.getIntArray(e, "rewardLavaBadgeCount"));

config.setDirectsettledecreasehp(MapHelper.getInt(e, "directSettleDecreaseHp"));

config.setDirectsettlefightpower(MapHelper.getInt(e, "directSettleFightPower"));


_ix_id.put(config.getId(),config);



}
}

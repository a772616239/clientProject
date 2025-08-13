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

@annationInit(value ="MistBattleReward", methodname = "initConfig")
public class MistBattleReward extends baseConfig<MistBattleRewardObject>{


private static MistBattleReward instance = null;

public static MistBattleReward getInstance() {

if (instance == null)
instance = new MistBattleReward();
return instance;

}


public static Map<Integer, MistBattleRewardObject> _ix_level = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MistBattleReward) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MistBattleReward");

for(Map e:ret)
{
put(e);
}

}

public static MistBattleRewardObject getByLevel(int level){

return _ix_level.get(level);

}



public  void putToMem(Map e, MistBattleRewardObject config){

config.setLevel(MapHelper.getInt(e, "Level"));

config.setBeatbossreward(MapHelper.getInt(e, "BeatBossReward"));

config.setBeatbossteamreward(MapHelper.getInt(e, "BeatBossTeamReward"));

config.setBeathiddenevilreward(MapHelper.getInt(e, "BeatHiddenEvilReward"));


_ix_level.put(config.getLevel(),config);



}
}

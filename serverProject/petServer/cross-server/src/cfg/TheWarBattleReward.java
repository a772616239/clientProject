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

@annationInit(value ="TheWarBattleReward", methodname = "initConfig")
public class TheWarBattleReward extends baseConfig<TheWarBattleRewardObject>{


private static TheWarBattleReward instance = null;

public static TheWarBattleReward getInstance() {

if (instance == null)
instance = new TheWarBattleReward();
return instance;

}


public static Map<Integer, TheWarBattleRewardObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (TheWarBattleReward) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"TheWarBattleReward");

for(Map e:ret)
{
put(e);
}

}

public static TheWarBattleRewardObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, TheWarBattleRewardObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setSurrenderrewrad(MapHelper.getIntArray(e, "surrenderRewrad"));

config.setFailedrewrad(MapHelper.getIntArray(e, "failedRewrad"));

config.setZerowinrewrad(MapHelper.getIntArray(e, "zeroWinRewrad"));

config.setOnestarwinrewrad(MapHelper.getIntArray(e, "oneStarWinRewrad"));

config.setTwostarwinrewrad(MapHelper.getIntArray(e, "twoStarWinRewrad"));

config.setThreestarwinrewradwarrewrad(MapHelper.getIntArray(e, "threeStarWinRewradwarRewrad"));


_ix_id.put(config.getId(),config);



}
}

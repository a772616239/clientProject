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

@annationInit(value ="MatchArenaConfig", methodname = "initConfig")
public class MatchArenaConfig extends baseConfig<MatchArenaConfigObject>{


private static MatchArenaConfig instance = null;

public static MatchArenaConfig getInstance() {

if (instance == null)
instance = new MatchArenaConfig();
return instance;

}


public static Map<Integer, MatchArenaConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MatchArenaConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MatchArenaConfig");

for(Map e:ret)
{
put(e);
}

}

public static MatchArenaConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MatchArenaConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setDefaultscore(MapHelper.getInt(e, "defaultScore"));

config.setRankingdisplayersize(MapHelper.getInt(e, "rankingDisplayerSize"));

config.setMedalconfig(MapHelper.getInts(e, "medalConfig"));

config.setBattlewinrewards(MapHelper.getIntArray(e, "battleWinRewards"));

config.setBattlefailedrewards(MapHelper.getIntArray(e, "battleFailedRewards"));

config.setSawin(MapHelper.getInt(e, "saWin"));

config.setSafailed(MapHelper.getInt(e, "saFailed"));

config.setSadraw(MapHelper.getInt(e, "saDraw"));

config.setConstnum(MapHelper.getInt(e, "constNum"));

config.setK(MapHelper.getInt(e, "k"));

config.setMatchrules(MapHelper.getIntArray(e, "matchRules"));

config.setRobotdelay(MapHelper.getInt(e, "robotDelay"));

config.setRobotscorediff(MapHelper.getInt(e, "robotScoreDiff"));

config.setPlayermatchcd(MapHelper.getInt(e, "playerMatchCD"));

config.setPlayermatchbattletimescd(MapHelper.getInt(e, "playerMatchBattleTimesCD"));

config.setRobotmatchcd(MapHelper.getInt(e, "robotMatchCD"));

config.setPvefightmakeid(MapHelper.getInt(e, "pveFightMakeId"));

config.setPvpfightmakeid(MapHelper.getInt(e, "pvpFightMakeId"));

config.setCrossrankingtemplate(MapHelper.getInt(e, "crossRankingTemplate"));

config.setDantemplate(MapHelper.getInt(e, "danTemplate"));


_ix_id.put(config.getId(),config);



}
}

/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import model.base.baseConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@annationInit(value ="MatchArenaLT", methodname = "initConfig")
public class MatchArenaLT extends baseConfig<MatchArenaLTObject>{


private static MatchArenaLT instance = null;

public static MatchArenaLT getInstance() {

if (instance == null)
instance = new MatchArenaLT();
return instance;

}


public static Map<Integer, MatchArenaLTObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MatchArenaLT) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MatchArenaLT");

for(Map e:ret)
{
put(e);
}

}

public static MatchArenaLTObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MatchArenaLTObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setStageltnum(MapHelper.getIntArray(e, "stageLTNum"));

config.setWinbuff(MapHelper.getInts(e, "winBuff"));

config.setMissionlist(MapHelper.getInts(e, "missionList"));

config.setWinguessnum(MapHelper.getInt(e, "winGuessNum"));

config.setExtguessnum(MapHelper.getInt(e, "extGuessNum"));

config.setWingrade(MapHelper.getInt(e, "winGrade"));

config.setFailgrade(MapHelper.getInt(e, "failGrade"));

config.setTimegrade(MapHelper.getIntArray(e, "timeGrade"));

config.setWingradeadd(MapHelper.getIntArray(e, "winGradeAdd"));

config.setBeatgradeadd(MapHelper.getIntArray(e, "beatGradeAdd"));

config.setAilimit(MapHelper.getInt(e, "aiLimit"));

config.setAimintime(MapHelper.getInt(e, "aiMinTime"));

config.setAimaxtime(MapHelper.getInt(e, "aiMaxTime"));

config.setSettlementtime(MapHelper.getInt(e, "settlementTime"));

config.setFightmakeid(MapHelper.getInt(e, "fightmakeid"));


_ix_id.put(config.getId(),config);



}
}

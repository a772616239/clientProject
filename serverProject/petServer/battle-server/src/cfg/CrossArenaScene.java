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

@annationInit(value ="CrossArenaScene", methodname = "initConfig")
public class CrossArenaScene extends baseConfig<CrossArenaSceneObject>{


private static CrossArenaScene instance = null;

public static CrossArenaScene getInstance() {

if (instance == null)
instance = new CrossArenaScene();
return instance;

}


public static Map<Integer, CrossArenaSceneObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (CrossArenaScene) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"CrossArenaScene");

for(Map e:ret)
{
put(e);
}

}

public static CrossArenaSceneObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, CrossArenaSceneObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setTablenum(MapHelper.getInt(e, "tableNum"));

config.setTablenummax(MapHelper.getInt(e, "tableNumMax"));

config.setTableaddlimit(MapHelper.getInt(e, "tableAddLimit"));

config.setQueuenum(MapHelper.getInt(e, "queueNum"));

config.setHonorlv(MapHelper.getInt(e, "honorlv"));

config.setWinrewardrate(MapHelper.getIntArray(e, "winRewardRate"));

config.setUptablexh(MapHelper.getInts(e, "upTableXH"));

config.setAireadytime(MapHelper.getInt(e, "aiReadyTime"));

config.setReadytime(MapHelper.getInt(e, "readyTime"));

config.setSettletime(MapHelper.getInt(e, "settleTime"));

config.setVstime(MapHelper.getInt(e, "vsTime"));

config.setRevoketime(MapHelper.getInt(e, "revokeTime"));

config.setFightmakeid(MapHelper.getInt(e, "fightmakeid"));

config.setZannum(MapHelper.getInt(e, "zanNum"));

config.setGuessnum(MapHelper.getInt(e, "guessNum"));

config.setAi(MapHelper.getInt(e, "ai"));

config.setAispecial(MapHelper.getInt(e, "aispecial"));

config.setAitime(MapHelper.getInts(e, "aiTime"));

config.setGrademax(MapHelper.getInt(e, "gradeMax"));

config.setGraderate(MapHelper.getInt(e, "gradeRate"));

config.setTaskgraderate(MapHelper.getInt(e, "taskGradeRate"));

config.setWingrade(MapHelper.getInt(e, "winGrade"));

config.setWingradecot(MapHelper.getInts(e, "winGradeCot"));

config.setTimegradeins(MapHelper.getInt(e, "timeGradeIns"));

config.setTimegrade(MapHelper.getInt(e, "timeGrade"));

config.setAwardgrade(MapHelper.getInts(e, "awardGrade"));

config.setBoss(MapHelper.getInt(e, "boss"));

config.setAiabilitybase(MapHelper.getInt(e, "aiAbilityBase"));


_ix_id.put(config.getId(),config);



}
}

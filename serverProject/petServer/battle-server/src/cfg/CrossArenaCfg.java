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

@annationInit(value ="CrossArenaCfg", methodname = "initConfig")
public class CrossArenaCfg extends baseConfig<CrossArenaCfgObject>{


private static CrossArenaCfg instance = null;

public static CrossArenaCfg getInstance() {

if (instance == null)
instance = new CrossArenaCfg();
return instance;

}


public static Map<Integer, CrossArenaCfgObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (CrossArenaCfg) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"CrossArenaCfg");

for(Map e:ret)
{
put(e);
}

}

public static CrossArenaCfgObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, CrossArenaCfgObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setMarqueeminwin(MapHelper.getInt(e, "marqueeMinWin"));

config.setLsgb(MapHelper.getInt(e, "lsgb"));

config.setMission(MapHelper.getInts(e, "mission"));

config.setTopgroupnum(MapHelper.getInt(e, "topgroupNum"));

config.setTopinitjf(MapHelper.getInt(e, "topInitJF"));

config.setTopwinjf(MapHelper.getInt(e, "topwinJF"));

config.setTopfailjf(MapHelper.getInt(e, "topfailJF"));

config.setTopaward(MapHelper.getInts(e, "topAward"));

config.setTopmailid(MapHelper.getInt(e, "topMailId"));

config.setWorship(MapHelper.getInts(e, "worship"));

config.setGrademailid(MapHelper.getInt(e, "gradeMailId"));

config.setAwardgrade(MapHelper.getInts(e, "awardGrade"));

config.setSerailwinrewardreduce(MapHelper.getInt(e, "serailWinRewardReduce"));

config.setWeekboxgetnum(MapHelper.getInt(e, "weekBoxGetNum"));


_ix_id.put(config.getId(),config);



}
}

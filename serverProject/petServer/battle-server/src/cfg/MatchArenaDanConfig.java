/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.base.baseConfig;

@annationInit(value ="MatchArenaDanConfig", methodname = "initConfig")
public class MatchArenaDanConfig extends baseConfig<MatchArenaDanConfigObject>{


private static MatchArenaDanConfig instance = null;

public static MatchArenaDanConfig getInstance() {

if (instance == null)
instance = new MatchArenaDanConfig();
return instance;

}


public static Map<Integer, MatchArenaDanConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MatchArenaDanConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MatchArenaDanConfig");

for(Map e:ret)
{
put(e);
}

}

public static MatchArenaDanConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MatchArenaDanConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setServername(MapHelper.getInt(e, "serverName"));

config.setNeedscore(MapHelper.getInt(e, "needScore"));

config.setMedallimit(MapHelper.getInt(e, "medalLimit"));

config.setMedalgainrate(MapHelper.getInt(e, "medalGainRate"));

config.setDanrewards(MapHelper.getIntArray(e, "danRewards"));

config.setBattlepetlimit(MapHelper.getInt(e, "battlePetLimit"));

config.setMatchaifailtimes(MapHelper.getInt(e, "matchAIfailTimes"));

config.setCanmatchrobottime(MapHelper.getInt(e, "canMatchRobotTime"));

config.setExpandmatchinterval(MapHelper.getInt(e, "expandMatchInterval"));

config.setOnceexpandscore(MapHelper.getInt(e, "onceExpandScore"));

config.setMaxscorediff(MapHelper.getInt(e, "maxScoreDiff"));


_ix_id.put(config.getId(),config);



}
}

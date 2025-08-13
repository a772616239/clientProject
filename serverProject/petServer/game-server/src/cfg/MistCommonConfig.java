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

@annationInit(value ="MistCommonConfig", methodname = "initConfig")
public class MistCommonConfig extends baseConfig<MistCommonConfigObject>{


private static MistCommonConfig instance = null;

public static MistCommonConfig getInstance() {

if (instance == null)
instance = new MistCommonConfig();
return instance;

}


public static Map<Integer, MistCommonConfigObject> _ix_mistlevel = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MistCommonConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MistCommonConfig");

for(Map e:ret)
{
put(e);
}

}

public static MistCommonConfigObject getByMistlevel(int mistlevel){

return _ix_mistlevel.get(mistlevel);

}



public  void putToMem(Map e, MistCommonConfigObject config){

config.setMistlevel(MapHelper.getInt(e, "MistLevel"));

config.setEntrancelevelsection(MapHelper.getInts(e, "EntranceLevelSection"));

config.setReviselevel(MapHelper.getInt(e, "ReviseLevel"));

config.setBossactivityrankreward(MapHelper.getIntArray(e, "BossActivityRankReward"));

config.setActivtiybosscfgid(MapHelper.getIntArray(e, "ActivtiyBossCfgId"));

config.setHiddenevilgroup(MapHelper.getInts(e, "HiddenEvilGroup"));

config.setAlchemylist(MapHelper.getInts(e, "AlchemyList"));

config.setDirectsettlefightparam1(MapHelper.getInt(e, "DirectSettleFightParam1"));

config.setDirectsettlefightparam2(MapHelper.getInt(e, "DirectSettleFightParam2"));


_ix_mistlevel.put(config.getMistlevel(),config);



}
}

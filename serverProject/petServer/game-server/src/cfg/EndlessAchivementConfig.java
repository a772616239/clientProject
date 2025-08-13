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

@annationInit(value ="EndlessAchivementConfig", methodname = "initConfig")
public class EndlessAchivementConfig extends baseConfig<EndlessAchivementConfigObject>{


private static EndlessAchivementConfig instance = null;

public static EndlessAchivementConfig getInstance() {

if (instance == null)
instance = new EndlessAchivementConfig();
return instance;

}


public static Map<Integer, EndlessAchivementConfigObject> _ix_stepid = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (EndlessAchivementConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"EndlessAchivementConfig");

for(Map e:ret)
{
put(e);
}

}

public static EndlessAchivementConfigObject getByStepid(int stepid){

return _ix_stepid.get(stepid);

}



public  void putToMem(Map e, EndlessAchivementConfigObject config){

config.setStepid(MapHelper.getInt(e, "StepId"));

config.setStartlayer(MapHelper.getInt(e, "StartLayer"));

config.setLayergap(MapHelper.getInt(e, "LayerGap"));

config.setRewardlist(MapHelper.getInts(e, "RewardList"));

config.setArtifactid(MapHelper.getInt(e, "ArtifactID"));


_ix_stepid.put(config.getStepid(),config);



}
}

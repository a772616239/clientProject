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

@annationInit(value ="MistBox", methodname = "initConfig")
public class MistBox extends baseConfig<MistBoxObject>{


private static MistBox instance = null;

public static MistBox getInstance() {

if (instance == null)
instance = new MistBox();
return instance;

}


public static Map<Integer, MistBoxObject> _ix_rewardid = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MistBox) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MistBox");

for(Map e:ret)
{
put(e);
}

}

public static MistBoxObject getByRewardid(int rewardid){

return _ix_rewardid.get(rewardid);

}



public  void putToMem(Map e, MistBoxObject config){

config.setRewardid(MapHelper.getInt(e, "RewardId"));

config.setCountshowtype(MapHelper.getInt(e, "CountShowType"));

config.setQualitylevel(MapHelper.getInt(e, "QualityLevel"));


_ix_rewardid.put(config.getRewardid(),config);



}
}

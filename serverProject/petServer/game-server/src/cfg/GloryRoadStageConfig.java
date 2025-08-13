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

@annationInit(value ="GloryRoadStageConfig", methodname = "initConfig")
public class GloryRoadStageConfig extends baseConfig<GloryRoadStageConfigObject>{


private static GloryRoadStageConfig instance = null;

public static GloryRoadStageConfig getInstance() {

if (instance == null)
instance = new GloryRoadStageConfig();
return instance;

}


public static Map<Integer, GloryRoadStageConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (GloryRoadStageConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"GloryRoadStageConfig");

for(Map e:ret)
{
put(e);
}

}

public static GloryRoadStageConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, GloryRoadStageConfigObject config){

config.setId(MapHelper.getInt(e, "Id"));

config.setServerpromotion(MapHelper.getInt(e, "serverPromotion"));

config.setServerpromotionfailed(MapHelper.getInt(e, "serverPromotionFailed"));


_ix_id.put(config.getId(),config);



}
}

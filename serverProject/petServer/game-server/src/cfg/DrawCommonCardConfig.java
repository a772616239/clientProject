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

@annationInit(value ="DrawCommonCardConfig", methodname = "initConfig")
public class DrawCommonCardConfig extends baseConfig<DrawCommonCardConfigObject>{


private static DrawCommonCardConfig instance = null;

public static DrawCommonCardConfig getInstance() {

if (instance == null)
instance = new DrawCommonCardConfig();
return instance;

}


public static Map<Integer, DrawCommonCardConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (DrawCommonCardConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"DrawCommonCardConfig");

for(Map e:ret)
{
put(e);
}

}

public static DrawCommonCardConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, DrawCommonCardConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setOdds(MapHelper.getInt(e, "odds"));

config.setQuality(MapHelper.getInt(e, "quality"));

config.setRewards(MapHelper.getInts(e, "rewards"));

config.setSelectedodds(MapHelper.getInt(e, "selectedOdds"));

config.setIscorepet(MapHelper.getBoolean(e, "isCorePet"));


_ix_id.put(config.getId(),config);



}
}

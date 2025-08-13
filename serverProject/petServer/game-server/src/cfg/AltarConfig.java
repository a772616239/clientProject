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

@annationInit(value ="AltarConfig", methodname = "initConfig")
public class AltarConfig extends baseConfig<AltarConfigObject>{


private static AltarConfig instance = null;

public static AltarConfig getInstance() {

if (instance == null)
instance = new AltarConfig();
return instance;

}


public static Map<Integer, AltarConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (AltarConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"AltarConfig");

for(Map e:ret)
{
put(e);
}

}

public static AltarConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, AltarConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setPrice(MapHelper.getInts(e, "price"));

config.setQualityweight(MapHelper.getIntArray(e, "qualityWeight"));

config.setMustgetdrawtimes(MapHelper.getInt(e, "mustGetDrawTimes"));

config.setMustgetquality(MapHelper.getInt(e, "mustGetQuality"));


_ix_id.put(config.getId(),config);



}
}

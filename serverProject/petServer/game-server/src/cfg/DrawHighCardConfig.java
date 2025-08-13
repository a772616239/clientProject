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

@annationInit(value ="DrawHighCardConfig", methodname = "initConfig")
public class DrawHighCardConfig extends baseConfig<DrawHighCardConfigObject>{


private static DrawHighCardConfig instance = null;

public static DrawHighCardConfig getInstance() {

if (instance == null)
instance = new DrawHighCardConfig();
return instance;

}


public static Map<Integer, DrawHighCardConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (DrawHighCardConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"DrawHighCardConfig");

for(Map e:ret)
{
put(e);
}

}

public static DrawHighCardConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, DrawHighCardConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setOdds(MapHelper.getInt(e, "odds"));

config.setQuality(MapHelper.getInt(e, "quality"));

config.setRewards(MapHelper.getInts(e, "rewards"));

config.setSelectedodds(MapHelper.getInt(e, "selectedOdds"));

config.setRouletterate(MapHelper.getInt(e, "rouletteRate"));

config.setIscorepet(MapHelper.getBoolean(e, "isCorePet"));


_ix_id.put(config.getId(),config);



}
}

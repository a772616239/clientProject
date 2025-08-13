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

@annationInit(value ="StoneMineLevel", methodname = "initConfig")
public class StoneMineLevel extends baseConfig<StoneMineLevelObject>{


private static StoneMineLevel instance = null;

public static StoneMineLevel getInstance() {

if (instance == null)
instance = new StoneMineLevel();
return instance;

}


public static Map<Integer, StoneMineLevelObject> _ix_level = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (StoneMineLevel) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"StoneMineLevel");

for(Map e:ret)
{
put(e);
}

}

public static StoneMineLevelObject getByLevel(int level){

return _ix_level.get(level);

}



public  void putToMem(Map e, StoneMineLevelObject config){

config.setLevel(MapHelper.getInt(e, "level"));

config.setImproveefficiency(MapHelper.getIntArray(e, "improveEfficiency"));

config.setUpconsume(MapHelper.getIntArray(e, "upConsume"));

config.setProducetime(MapHelper.getIntArray(e, "produceTime"));


_ix_level.put(config.getLevel(),config);



}
}

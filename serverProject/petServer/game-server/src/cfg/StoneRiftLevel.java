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

@annationInit(value ="StoneRiftLevel", methodname = "initConfig")
public class StoneRiftLevel extends baseConfig<StoneRiftLevelObject>{


private static StoneRiftLevel instance = null;

public static StoneRiftLevel getInstance() {

if (instance == null)
instance = new StoneRiftLevel();
return instance;

}


public static Map<Integer, StoneRiftLevelObject> _ix_level = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (StoneRiftLevel) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"StoneRiftLevel");

for(Map e:ret)
{
put(e);
}

}

public static StoneRiftLevelObject getByLevel(int level){

return _ix_level.get(level);

}



public  void putToMem(Map e, StoneRiftLevelObject config){

config.setLevel(MapHelper.getInt(e, "level"));

config.setUpexp(MapHelper.getInt(e, "upExp"));

config.setCurrencyamax(MapHelper.getInt(e, "currencyAMax"));

config.setUnlockscience(MapHelper.getInts(e, "unLockScience"));

config.setOutputup(MapHelper.getInt(e, "outputUp"));

config.setRecoveryconsume(MapHelper.getInts(e, "recoveryConsume"));


_ix_level.put(config.getLevel(),config);



}
}

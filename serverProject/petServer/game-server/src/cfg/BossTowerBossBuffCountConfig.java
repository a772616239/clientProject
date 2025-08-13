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

@annationInit(value ="BossTowerBossBuffCountConfig", methodname = "initConfig")
public class BossTowerBossBuffCountConfig extends baseConfig<BossTowerBossBuffCountConfigObject>{


private static BossTowerBossBuffCountConfig instance = null;

public static BossTowerBossBuffCountConfig getInstance() {

if (instance == null)
instance = new BossTowerBossBuffCountConfig();
return instance;

}


public static Map<Integer, BossTowerBossBuffCountConfigObject> _ix_difficult = new HashMap<Integer, BossTowerBossBuffCountConfigObject>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (BossTowerBossBuffCountConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"BossTowerBossBuffCountConfig");

for(Map e:ret)
{
put(e);
}

}

public static BossTowerBossBuffCountConfigObject getByDifficult(int difficult){

return _ix_difficult.get(difficult);

}



public  void putToMem(Map e, BossTowerBossBuffCountConfigObject config){

config.setDifficult(MapHelper.getInt(e, "difficult"));

config.setBuffcount(MapHelper.getIntArray(e, "buffCount"));


_ix_difficult.put(config.getDifficult(),config);



}
}

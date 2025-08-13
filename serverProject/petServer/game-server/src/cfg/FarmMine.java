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

@annationInit(value ="FarmMine", methodname = "initConfig")
public class FarmMine extends baseConfig<FarmMineObject>{


private static FarmMine instance = null;

public static FarmMine getInstance() {

if (instance == null)
instance = new FarmMine();
return instance;

}


public static Map<Integer, FarmMineObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (FarmMine) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"FarmMine");

for(Map e:ret)
{
put(e);
}

}

public static FarmMineObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, FarmMineObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setWight(MapHelper.getInt(e, "wight"));

config.setBaseaward(MapHelper.getInts(e, "baseAward"));

config.setExtnum(MapHelper.getInt(e, "extNum"));

config.setExtaward(MapHelper.getInts(e, "extAward"));

config.setPetadd(MapHelper.getInts(e, "petAdd"));


_ix_id.put(config.getId(),config);



}
}

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

@annationInit(value ="StoneRiftMine", methodname = "initConfig")
public class StoneRiftMine extends baseConfig<StoneRiftMineObject>{


private static StoneRiftMine instance = null;

public static StoneRiftMine getInstance() {

if (instance == null)
instance = new StoneRiftMine();
return instance;

}


public static Map<Integer, StoneRiftMineObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (StoneRiftMine) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"StoneRiftMine");

for(Map e:ret)
{
put(e);
}

}

public static StoneRiftMineObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, StoneRiftMineObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setType(MapHelper.getInt(e, "type"));

config.setOutput(MapHelper.getIntArray(e, "output"));

config.setUnlockcondition(MapHelper.getInt(e, "unlockCondition"));

config.setMaxstore(MapHelper.getInt(e, "maxStore"));

config.setUnlockconsume(MapHelper.getInts(e, "unlockConsume"));

config.setDurableconsume(MapHelper.getInt(e, "durableConsume"));

config.setExchangeexp(MapHelper.getInts(e, "exchangeExp"));

config.setWorth(MapHelper.getInt(e, "worth"));


_ix_id.put(config.getId(),config);



}
}

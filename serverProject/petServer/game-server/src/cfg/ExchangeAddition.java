/*CREATED BY TOOL*/

package cfg;
import JsonTool.readJsonFile;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import model.base.baseConfig;
import datatool.MapHelper;
import annotation.annationInit;
import common.load.ServerConfig;

@annationInit(value ="ExchangeAddition", methodname = "initConfig")
public class ExchangeAddition extends baseConfig<ExchangeAdditionObject>{


private static ExchangeAddition instance = null;

public static ExchangeAddition getInstance() {

if (instance == null)
instance = new ExchangeAddition();
return instance;

}


public static Map<Integer, ExchangeAdditionObject> _ix_index = new HashMap<Integer, ExchangeAdditionObject>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (ExchangeAddition) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "ExchangeAddition");

for(Map e:ret)
{
put(e);
}

}

public static ExchangeAdditionObject getByIndex(int index){

return _ix_index.get(index);

}



public  void putToMem(Map e, ExchangeAdditionObject config){

config.setIndex(MapHelper.getInt(e, "index"));

config.setType(MapHelper.getInt(e, "type"));

config.setCount(MapHelper.getInt(e, "count"));

config.setAddition(MapHelper.getIntArray(e, "addition"));


_ix_index.put(config.getIndex(),config);



}
}

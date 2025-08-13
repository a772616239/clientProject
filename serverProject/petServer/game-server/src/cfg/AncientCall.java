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

@annationInit(value ="AncientCall", methodname = "initConfig")
public class AncientCall extends baseConfig<AncientCallObject>{


private static AncientCall instance = null;

public static AncientCall getInstance() {

if (instance == null)
instance = new AncientCall();
return instance;

}


public static Map<Integer, AncientCallObject> _ix_id = new HashMap<Integer, AncientCallObject>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (AncientCall) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"AncientCall");

for(Map e:ret)
{
put(e);
}

}

public static AncientCallObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, AncientCallObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setType(MapHelper.getInt(e, "type"));

config.setRate(MapHelper.getInt(e, "rate"));

config.setQuality(MapHelper.getInt(e, "quality"));

config.setContant(MapHelper.getInts(e, "contant"));

config.setSelectedodds(MapHelper.getInt(e, "selectedOdds"));


_ix_id.put(config.getId(),config);



}
}

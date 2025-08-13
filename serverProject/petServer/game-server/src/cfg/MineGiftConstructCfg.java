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

@annationInit(value ="MineGiftConstructCfg", methodname = "initConfig")
public class MineGiftConstructCfg extends baseConfig<MineGiftConstructCfgObject>{


private static MineGiftConstructCfg instance = null;

public static MineGiftConstructCfg getInstance() {

if (instance == null)
instance = new MineGiftConstructCfg();
return instance;

}


public static Map<Integer, MineGiftConstructCfgObject> _ix_id = new HashMap<Integer, MineGiftConstructCfgObject>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MineGiftConstructCfg) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MineGiftConstructCfg");

for(Map e:ret)
{
put(e);
}

}

public static MineGiftConstructCfgObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MineGiftConstructCfgObject config){

config.setId(MapHelper.getInt(e, "Id"));

config.setRefreshtime(MapHelper.getInt(e, "RefreshTime"));

config.setGiftlist(MapHelper.getIntArray(e, "GiftList"));


_ix_id.put(config.getId(),config);



}
}

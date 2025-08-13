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

@annationInit(value ="BanShuConfig", methodname = "initConfig")
public class BanShuConfig extends baseConfig<BanShuConfigObject>{


private static BanShuConfig instance = null;

public static BanShuConfig getInstance() {

if (instance == null)
instance = new BanShuConfig();
return instance;

}


public static Map<Integer, BanShuConfigObject> _ix_id = new HashMap<Integer, BanShuConfigObject>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (BanShuConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"BanShuConfig");

for(Map e:ret)
{
put(e);
}

}

public static BanShuConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, BanShuConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setDrawcardlimit(MapHelper.getInt(e, "drawCardLimit"));

config.setAltarlimit(MapHelper.getInt(e, "altarLimit"));


_ix_id.put(config.getId(),config);



}
}

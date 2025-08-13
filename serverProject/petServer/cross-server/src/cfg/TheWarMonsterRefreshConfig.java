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

@annationInit(value ="TheWarMonsterRefreshConfig", methodname = "initConfig")
public class TheWarMonsterRefreshConfig extends baseConfig<TheWarMonsterRefreshConfigObject>{


private static TheWarMonsterRefreshConfig instance = null;

public static TheWarMonsterRefreshConfig getInstance() {

if (instance == null)
instance = new TheWarMonsterRefreshConfig();
return instance;

}


public static Map<Integer, TheWarMonsterRefreshConfigObject> _ix_id = new HashMap<Integer, TheWarMonsterRefreshConfigObject>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (TheWarMonsterRefreshConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"TheWarMonsterRefreshConfig");

for(Map e:ret)
{
put(e);
}

}

public static TheWarMonsterRefreshConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, TheWarMonsterRefreshConfigObject config){

config.setId(MapHelper.getInt(e, "Id"));

config.setRefreshinterval(MapHelper.getInt(e, "refreshInterval"));

config.setRefreshnum(MapHelper.getInt(e, "refreshNum"));

config.setMaxrefreshnum(MapHelper.getInt(e, "maxRefreshNum"));

config.setGorlrate(MapHelper.getInt(e, "GorlRate"));

config.setDprate(MapHelper.getInt(e, "DpRate"));

config.setMaxproducttime(MapHelper.getInt(e, "maxProductTime"));

config.setFithmakeid(MapHelper.getInt(e, "fithMakeId"));


_ix_id.put(config.getId(),config);



}
}

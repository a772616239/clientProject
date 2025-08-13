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

@annationInit(value ="GiftConfig", methodname = "initConfig")
public class GiftConfig extends baseConfig<GiftConfigObject>{


private static GiftConfig instance = null;

public static GiftConfig getInstance() {

if (instance == null)
instance = new GiftConfig();
return instance;

}


public static Map<Integer, GiftConfigObject> _ix_id = new HashMap<Integer, GiftConfigObject>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (GiftConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "GiftConfig");

for(Map e:ret)
{
put(e);
}

}

public static GiftConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, GiftConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setIcon(MapHelper.getStr(e, "icon"));

config.setDesc(MapHelper.getInt(e, "desc"));

config.setBegintime(MapHelper.getInt(e, "beginTime"));

config.setEndtime(MapHelper.getInt(e, "endTime"));

config.setDisplayendtime(MapHelper.getInt(e, "displayEndTime"));


_ix_id.put(config.getId(),config);



}
}

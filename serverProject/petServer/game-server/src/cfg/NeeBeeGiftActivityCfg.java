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

@annationInit(value ="NeeBeeGiftActivityCfg", methodname = "initConfig")
public class NeeBeeGiftActivityCfg extends baseConfig<NeeBeeGiftActivityCfgObject>{


private static NeeBeeGiftActivityCfg instance = null;

public static NeeBeeGiftActivityCfg getInstance() {

if (instance == null)
instance = new NeeBeeGiftActivityCfg();
return instance;

}


public static Map<Integer, NeeBeeGiftActivityCfgObject> _ix_id = new HashMap<Integer, NeeBeeGiftActivityCfgObject>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (NeeBeeGiftActivityCfg) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"NeeBeeGiftActivityCfg");

for(Map e:ret)
{
put(e);
}

}

public static NeeBeeGiftActivityCfgObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, NeeBeeGiftActivityCfgObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setPicture(MapHelper.getStr(e, "picture"));

config.setTitle(MapHelper.getInt(e, "title"));

config.setDesc(MapHelper.getInt(e, "desc"));

config.setEnddistime(MapHelper.getInt(e, "endDisTime"));


_ix_id.put(config.getId(),config);



}
}

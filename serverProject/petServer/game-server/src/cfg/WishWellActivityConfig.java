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

@annationInit(value ="WishWellActivityConfig", methodname = "initConfig")
public class WishWellActivityConfig extends baseConfig<WishWellActivityConfigObject>{


private static WishWellActivityConfig instance = null;

public static WishWellActivityConfig getInstance() {

if (instance == null)
instance = new WishWellActivityConfig();
return instance;

}


public static Map<Integer, WishWellActivityConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (WishWellActivityConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"WishWellActivityConfig");

for(Map e:ret)
{
put(e);
}

}

public static WishWellActivityConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, WishWellActivityConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setPicture(MapHelper.getStr(e, "picture"));

config.setTitle(MapHelper.getInt(e, "title"));

config.setDesc(MapHelper.getInt(e, "desc"));

config.setStartdistime(MapHelper.getStr(e, "startDisTime"));

config.setOverdistime(MapHelper.getStr(e, "overDisTime"));

config.setDuration(MapHelper.getInt(e, "duration"));

config.setHelp(MapHelper.getInt(e, "help"));


_ix_id.put(config.getId(),config);



}
}

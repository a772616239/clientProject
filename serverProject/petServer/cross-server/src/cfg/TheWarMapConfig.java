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

@annationInit(value ="TheWarMapConfig", methodname = "initConfig")
public class TheWarMapConfig extends baseConfig<TheWarMapConfigObject>{


private static TheWarMapConfig instance = null;

public static TheWarMapConfig getInstance() {

if (instance == null)
instance = new TheWarMapConfig();
return instance;

}


public static Map<String, TheWarMapConfigObject> _ix_mapname = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (TheWarMapConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"TheWarMapConfig");

for(Map e:ret)
{
put(e);
}

}

public static TheWarMapConfigObject getByMapname(String mapname){

return _ix_mapname.get(mapname);

}



public  void putToMem(Map e, TheWarMapConfigObject config){

config.setMapname(MapHelper.getStr(e, "mapName"));

config.setMapfilename(MapHelper.getStr(e, "mapFileName"));

config.setMaxplayercount(MapHelper.getInt(e, "maxPlayerCount"));

config.setCampnum(MapHelper.getInt(e, "campNum"));

config.setPetverifylevel(MapHelper.getInt(e, "petVerifyLevel"));

config.setInitstamina(MapHelper.getInt(e, "initStamina"));

config.setRecoverstamina(MapHelper.getInt(e, "recoverStamina"));

config.setRecoverstaminainterval(MapHelper.getInt(e, "recoverStaminaInterval"));

config.setMaxrecoverstamina(MapHelper.getInt(e, "maxRecoverStamina"));

config.setMaxenergy(MapHelper.getInt(e, "maxEnergy"));


_ix_mapname.put(config.getMapname(),config);



}
}

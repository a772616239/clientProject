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

@annationInit(value ="EndlessSpireConfig", methodname = "initConfig")
public class EndlessSpireConfig extends baseConfig<EndlessSpireConfigObject>{


private static EndlessSpireConfig instance = null;

public static EndlessSpireConfig getInstance() {

if (instance == null)
instance = new EndlessSpireConfig();
return instance;

}


public static Map<Integer, EndlessSpireConfigObject> _ix_spirelv = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (EndlessSpireConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"EndlessSpireConfig");

for(Map e:ret)
{
put(e);
}

}

public static EndlessSpireConfigObject getBySpirelv(int spirelv){

return _ix_spirelv.get(spirelv);

}



public  void putToMem(Map e, EndlessSpireConfigObject config){

config.setSpirelv(MapHelper.getInt(e, "SpireLv"));

config.setMonsterteamid(MapHelper.getInt(e, "monsterTeamId"));

config.setLvlimit(MapHelper.getInt(e, "lvLimit"));

config.setGongxun(MapHelper.getInt(e, "gongxun"));


_ix_spirelv.put(config.getSpirelv(),config);



}
}

/*CREATED BY TOOL*/

package cfg;
import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import model.base.baseConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@annationInit(value ="BossTowerBossFixBuff", methodname = "initConfig")
public class BossTowerBossFixBuff extends baseConfig<BossTowerBossFixBuffObject>{


private static BossTowerBossFixBuff instance = null;

public static BossTowerBossFixBuff getInstance() {

if (instance == null)
instance = new BossTowerBossFixBuff();
return instance;

}


public static Map<Integer, BossTowerBossFixBuffObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (BossTowerBossFixBuff) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"BossTowerBossFixBuff");

for(Map e:ret)
{
put(e);
}

}

public static BossTowerBossFixBuffObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, BossTowerBossFixBuffObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setPlayerbuff(MapHelper.getInts(e, "playerBuff"));

config.setBossbuff(MapHelper.getInts(e, "bossBuff"));


_ix_id.put(config.getId(),config);



}

public Map<Integer, BossTowerBossFixBuffObject> getConfigs(){
    return _ix_id;
}

}

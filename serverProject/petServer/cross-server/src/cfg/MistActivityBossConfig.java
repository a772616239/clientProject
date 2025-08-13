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

@annationInit(value ="MistActivityBossConfig", methodname = "initConfig")
public class MistActivityBossConfig extends baseConfig<MistActivityBossConfigObject>{


private static MistActivityBossConfig instance = null;

public static MistActivityBossConfig getInstance() {

if (instance == null)
instance = new MistActivityBossConfig();
return instance;

}


public static Map<Integer, MistActivityBossConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MistActivityBossConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MistActivityBossConfig");

for(Map e:ret)
{
put(e);
}

}

public static MistActivityBossConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MistActivityBossConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setBossunittype(MapHelper.getInt(e, "bossUnitType"));

config.setChangestagehprate(MapHelper.getInt(e, "changeStageHpRate"));

config.setDropboxlist(MapHelper.getIntArray(e, "dropBoxList"));

config.setActivtiybossindividualprops(MapHelper.getIntArray(e, "ActivtiyBossIndividualProps"));


_ix_id.put(config.getId(),config);



}
}

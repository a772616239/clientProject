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

@annationInit(value ="BuffConfig", methodname = "initConfig")
public class BuffConfig extends baseConfig<BuffConfigObject>{


private static BuffConfig instance = null;

public static BuffConfig getInstance() {

if (instance == null)
instance = new BuffConfig();
return instance;

}


public static Map<Integer, BuffConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (BuffConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"BuffConfig");

for(Map e:ret)
{
put(e);
}

}

public static BuffConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, BuffConfigObject config){

config.setId(MapHelper.getInt(e, "Id"));

config.setFixability1(MapHelper.getInt(e, "FixAbility1"));

config.setFixability2(MapHelper.getInt(e, "FixAbility2"));

config.setAbilityaddtion(MapHelper.getInt(e, "abilityAddtion"));


_ix_id.put(config.getId(),config);



}
}

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

@annationInit(value ="BuffCondition", methodname = "initConfig")
public class BuffCondition extends baseConfig<BuffConditionObject>{


private static BuffCondition instance = null;

public static BuffCondition getInstance() {

if (instance == null)
instance = new BuffCondition();
return instance;

}


public static Map<Integer, BuffConditionObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (BuffCondition) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"BuffCondition");

for(Map e:ret)
{
put(e);
}

}

public static BuffConditionObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, BuffConditionObject config){

config.setId(MapHelper.getInt(e, "Id"));

config.setPettype(MapHelper.getInt(e, "PetType"));

config.setPetclass(MapHelper.getInt(e, "PetClass"));

config.setOnbattleindex(MapHelper.getInt(e, "OnBattleIndex"));


_ix_id.put(config.getId(),config);



}
}

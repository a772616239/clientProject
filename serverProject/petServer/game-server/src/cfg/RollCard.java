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

@annationInit(value ="RollCard", methodname = "initConfig")
public class RollCard extends baseConfig<RollCardObject>{


private static RollCard instance = null;

public static RollCard getInstance() {

if (instance == null)
instance = new RollCard();
return instance;

}


public static Map<Integer, RollCardObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (RollCard) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"RollCard");

for(Map e:ret)
{
put(e);
}

}

public static RollCardObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, RollCardObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setType(MapHelper.getInt(e, "type"));

config.setGrade(MapHelper.getIntArray(e, "grade"));

config.setPool1(MapHelper.getIntArray(e, "pool1"));

config.setPool2(MapHelper.getIntArray(e, "pool2"));

config.setCost1(MapHelper.getInts(e, "cost1"));

config.setCost10(MapHelper.getInts(e, "cost10"));

config.setOthercost1(MapHelper.getInts(e, "otherCost1"));

config.setOthercost10(MapHelper.getInts(e, "otherCost10"));

config.setLuckpool(MapHelper.getInts(e, "luckpool"));


_ix_id.put(config.getId(),config);



}
}

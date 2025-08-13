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

@annationInit(value ="TrainingLuckCard", methodname = "initConfig")
public class TrainingLuckCard extends baseConfig<TrainingLuckCardObject>{


private static TrainingLuckCard instance = null;

public static TrainingLuckCard getInstance() {

if (instance == null)
instance = new TrainingLuckCard();
return instance;

}


public static Map<Integer, TrainingLuckCardObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (TrainingLuckCard) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"TrainingLuckCard");

for(Map e:ret)
{
put(e);
}

}

public static TrainingLuckCardObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, TrainingLuckCardObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setFreecd(MapHelper.getInt(e, "freecd"));

config.setLuck(MapHelper.getInt(e, "luck"));

config.setOpentime(MapHelper.getInt(e, "openTime"));

config.setNormal(MapHelper.getInt(e, "normal"));

config.setBetter(MapHelper.getInt(e, "better"));

config.setCost1(MapHelper.getInts(e, "cost1"));

config.setCost3(MapHelper.getInts(e, "cost3"));


_ix_id.put(config.getId(),config);



}
}

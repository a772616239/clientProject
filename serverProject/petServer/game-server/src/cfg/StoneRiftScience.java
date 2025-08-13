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

@annationInit(value ="StoneRiftScience", methodname = "initConfig")
public class StoneRiftScience extends baseConfig<StoneRiftScienceObject>{


private static StoneRiftScience instance = null;

public static StoneRiftScience getInstance() {

if (instance == null)
instance = new StoneRiftScience();
return instance;

}


public static Map<Integer, StoneRiftScienceObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (StoneRiftScience) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"StoneRiftScience");

for(Map e:ret)
{
put(e);
}

}

public static StoneRiftScienceObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, StoneRiftScienceObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setFunction(MapHelper.getInt(e, "function"));

config.setLevelprams(MapHelper.getInts(e, "levelPrams"));

config.setMaxlevel(MapHelper.getInt(e, "maxlevel"));

config.setPrveinfo(MapHelper.getInts(e, "prveInfo"));

config.setStudyconsume(MapHelper.getInts(e, "studyConsume"));


_ix_id.put(config.getId(),config);



}
}

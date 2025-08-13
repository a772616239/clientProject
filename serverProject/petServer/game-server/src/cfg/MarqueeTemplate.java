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

@annationInit(value ="MarqueeTemplate", methodname = "initConfig")
public class MarqueeTemplate extends baseConfig<MarqueeTemplateObject>{


private static MarqueeTemplate instance = null;

public static MarqueeTemplate getInstance() {

if (instance == null)
instance = new MarqueeTemplate();
return instance;

}


public static Map<Integer, MarqueeTemplateObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MarqueeTemplate) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MarqueeTemplate");

for(Map e:ret)
{
put(e);
}

}

public static MarqueeTemplateObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MarqueeTemplateObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setContent(MapHelper.getInt(e, "content"));

config.setRolltimes(MapHelper.getInt(e, "rollTimes"));

config.setScene(MapHelper.getInts(e, "scene"));

config.setPriority(MapHelper.getInt(e, "priority"));

config.setDuration(MapHelper.getInt(e, "duration"));


_ix_id.put(config.getId(),config);



}
}

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

@annationInit(value ="MainLineNodeOpenEpisode", methodname = "initConfig")
public class MainLineNodeOpenEpisode extends baseConfig<MainLineNodeOpenEpisodeObject>{


private static MainLineNodeOpenEpisode instance = null;

public static MainLineNodeOpenEpisode getInstance() {

if (instance == null)
instance = new MainLineNodeOpenEpisode();
return instance;

}


public static Map<Integer, MainLineNodeOpenEpisodeObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MainLineNodeOpenEpisode) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MainLineNodeOpenEpisode");

for(Map e:ret)
{
put(e);
}

}

public static MainLineNodeOpenEpisodeObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MainLineNodeOpenEpisodeObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setEpisodeid(MapHelper.getInt(e, "EpisodeId"));


_ix_id.put(config.getId(),config);



}
}

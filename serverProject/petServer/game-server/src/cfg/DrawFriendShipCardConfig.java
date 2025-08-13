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

@annationInit(value ="DrawFriendShipCardConfig", methodname = "initConfig")
public class DrawFriendShipCardConfig extends baseConfig<DrawFriendShipCardConfigObject>{


private static DrawFriendShipCardConfig instance = null;

public static DrawFriendShipCardConfig getInstance() {

if (instance == null)
instance = new DrawFriendShipCardConfig();
return instance;

}


public static Map<Integer, DrawFriendShipCardConfigObject> _ix_id = new HashMap<Integer, DrawFriendShipCardConfigObject>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (DrawFriendShipCardConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"DrawFriendShipCardConfig");

for(Map e:ret)
{
put(e);
}

}

public static DrawFriendShipCardConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, DrawFriendShipCardConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setOdds(MapHelper.getInt(e, "odds"));

config.setQuality(MapHelper.getInt(e, "quality"));

config.setRewards(MapHelper.getInts(e, "rewards"));

config.setSelectedodds(MapHelper.getInt(e, "selectedOdds"));


_ix_id.put(config.getId(),config);



}
}

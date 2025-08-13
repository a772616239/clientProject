/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import datatool.MapHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.base.baseConfig;
import petrobot.util.ServerConfig;

@annationInit(value ="MistMapConfig", methodname = "initConfig")
public class MistMapConfig extends baseConfig<MistMapConfigObject>{


private static MistMapConfig instance = null;

public static MistMapConfig getInstance() {

if (instance == null)
instance = new MistMapConfig();
return instance;

}


public static Map<Integer, MistMapConfigObject> _ix_mapid = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MistMapConfig) o;
initConfig();
}


public void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MistMapConfig");

for(Map e:ret)
{
put(e);
}

}

public static MistMapConfigObject getByMapid(int mapid){

return _ix_mapid.get(mapid);

}



public  void putToMem(Map e, MistMapConfigObject config){

config.setMapid(MapHelper.getInt(e, "MapId"));

config.setMapblock(MapHelper.getIntArray(e, "MapBlock"));

config.setMapblock1(MapHelper.getIntArray(e, "MapBlock1"));

config.setMapblock2(MapHelper.getIntArray(e, "MapBlock2"));


_ix_mapid.put(config.getMapid(),config);



}
}

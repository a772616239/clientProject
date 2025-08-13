/*CREATED BY TOOL*/

package cfg;
import JsonTool.readJsonFile;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import model.base.baseConfig;
import datatool.MapHelper;
import annotation.annationInit;
import common.load.ServerConfig;

@annationInit(value ="NoviceCredit", methodname = "initConfig")
public class NoviceCredit extends baseConfig<NoviceCreditObject>{


private static NoviceCredit instance = null;

public static NoviceCredit getInstance() {

if (instance == null)
instance = new NoviceCredit();
return instance;

}


public static Map<Integer, NoviceCreditObject> _ix_points = new HashMap<Integer, NoviceCreditObject>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (NoviceCredit) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "NoviceCredit");

for(Map e:ret)
{
put(e);
}

}

public static NoviceCreditObject getByPoints(int points){

return _ix_points.get(points);

}



public  void putToMem(Map e, NoviceCreditObject config){

config.setPoints(MapHelper.getInt(e, "points"));

config.setAward(MapHelper.getInts(e, "award"));


_ix_points.put(config.getPoints(),config);



}
}

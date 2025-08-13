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

@annationInit(value ="CrossArenaPvp", methodname = "initConfig")
public class CrossArenaPvp extends baseConfig<CrossArenaPvpObject>{


private static CrossArenaPvp instance = null;

public static CrossArenaPvp getInstance() {

if (instance == null)
instance = new CrossArenaPvp();
return instance;

}


public static Map<Integer, CrossArenaPvpObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (CrossArenaPvp) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"CrossArenaPvp");

for(Map e:ret)
{
put(e);
}

}

public static CrossArenaPvpObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, CrossArenaPvpObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setConsume(MapHelper.getIntArray(e, "consume"));

config.setPowerlimit(MapHelper.getInts(e, "powerlimit"));

config.setLevellimit(MapHelper.getInts(e, "levellimit"));

config.setPetnum(MapHelper.getInt(e, "petnum"));

config.setBlackpetnum(MapHelper.getInt(e, "blackpetnum"));

config.setTime(MapHelper.getInt(e, "time"));


_ix_id.put(config.getId(),config);



}
}

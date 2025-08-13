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

@annationInit(value ="TheWarGroupConfig", methodname = "initConfig")
public class TheWarGroupConfig extends baseConfig<TheWarGroupConfigObject>{


private static TheWarGroupConfig instance = null;

public static TheWarGroupConfig getInstance() {

if (instance == null)
instance = new TheWarGroupConfig();
return instance;

}


public static Map<Integer, TheWarGroupConfigObject> _ix_groupid = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (TheWarGroupConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"TheWarGroupConfig");

for(Map e:ret)
{
put(e);
}

}

public static TheWarGroupConfigObject getByGroupid(int groupid){

return _ix_groupid.get(groupid);

}



public  void putToMem(Map e, TheWarGroupConfigObject config){

config.setGroupid(MapHelper.getInt(e, "GroupId"));

config.setServergroupname(MapHelper.getInt(e, "ServerGroupName"));


_ix_groupid.put(config.getGroupid(),config);



}
}

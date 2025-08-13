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

@annationInit(value ="MainLineNode", methodname = "initConfig")
public class MainLineNode extends baseConfig<MainLineNodeObject>{


private static MainLineNode instance = null;

public static MainLineNode getInstance() {

if (instance == null)
instance = new MainLineNode();
return instance;

}


public static Map<Integer, MainLineNodeObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MainLineNode) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MainLineNode");

for(Map e:ret)
{
put(e);
}

}

public static MainLineNodeObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MainLineNodeObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setNodetype(MapHelper.getInt(e, "nodeType"));

config.setReward(MapHelper.getIntArray(e, "reward"));

config.setRewarplot(MapHelper.getInts(e, "rewarplot"));

config.setFightmakeid(MapHelper.getInt(e, "fightMakeId"));

config.setPrevnodeid(MapHelper.getInts(e, "prevNodeId"));

config.setAfternodeid(MapHelper.getInts(e, "afterNodeId"));

config.setParam(MapHelper.getInt(e, "param"));

config.setEnhance(MapHelper.getIntArray(e, "enhance"));

config.setWeaken(MapHelper.getIntArray(e, "weaken"));

config.setOnhookable(MapHelper.getBoolean(e, "onHookable"));

config.setOnhookresourceoutput(MapHelper.getIntArray(e, "onHookResourceOutPut"));

config.setOnhookrandompool(MapHelper.getIntArray(e, "onHookRandomPool"));


_ix_id.put(config.getId(),config);



}
}

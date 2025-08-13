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

@annationInit(value ="CrazyDuelCfg", methodname = "initConfig")
public class CrazyDuelCfg extends baseConfig<CrazyDuelCfgObject>{


private static CrazyDuelCfg instance = null;

public static CrazyDuelCfg getInstance() {

if (instance == null)
instance = new CrazyDuelCfg();
return instance;

}


public static Map<Integer, CrazyDuelCfgObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (CrazyDuelCfg) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"CrazyDuelCfg");

for(Map e:ret)
{
put(e);
}

}

public static CrazyDuelCfgObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, CrazyDuelCfgObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setBuffpool(MapHelper.getInts(e, "buffPool"));

config.setFloorbuffsize(MapHelper.getIntArray(e, "floorBuffSize"));

config.setOffertime(MapHelper.getInts(e, "offerTime"));

config.setOfferplay(MapHelper.getInt(e, "offerPlay"));

config.setMatchscorediff(MapHelper.getInt(e, "matchScoreDiff"));

config.setPlayerinitscore(MapHelper.getInt(e, "playerInitScore"));

config.setPagesize(MapHelper.getInt(e, "pageSize"));

config.setDefendablityincr(MapHelper.getIntArray(e, "defendAblityIncr"));

config.setMatchscope(MapHelper.getIntArray(e, "matchScope"));

config.setRefreshunlockgradelv(MapHelper.getInt(e, "refreshUnlockGradeLv"));


_ix_id.put(config.getId(),config);



}
}

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

@annationInit(value ="PointCopyOpenTime", methodname = "initConfig")
public class PointCopyOpenTime extends baseConfig<PointCopyOpenTimeObject>{


private static PointCopyOpenTime instance = null;

public static PointCopyOpenTime getInstance() {

if (instance == null)
instance = new PointCopyOpenTime();
return instance;

}


public static Map<Integer, PointCopyOpenTimeObject> _ix_id = new HashMap<Integer, PointCopyOpenTimeObject>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (PointCopyOpenTime) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "PointCopyOpenTime");

for(Map e:ret)
{
put(e);
}

}

public static PointCopyOpenTimeObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, PointCopyOpenTimeObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setStarttime(MapHelper.getInts(e, "startTime"));

config.setEndtime(MapHelper.getInts(e, "endTime"));

config.setFightlist(MapHelper.getInts(e, "fightList"));

config.setPointlist(MapHelper.getInts(e, "pointList"));

config.setDefaultunlockfightid(MapHelper.getInt(e, "defaultUnlockFightId"));

config.setDropticket(MapHelper.getInts(e, "dropTicket"));

config.setPointcopyticketdropodds(MapHelper.getInt(e, "pointCopyTicketDropOdds"));

config.setPointcopyticketdropinterval(MapHelper.getInt(e, "pointCopyTicketDropInterval"));


_ix_id.put(config.getId(),config);



}
}

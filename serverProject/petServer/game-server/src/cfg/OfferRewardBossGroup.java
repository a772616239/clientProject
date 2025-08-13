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

@annationInit(value ="OfferRewardBossGroup", methodname = "initConfig")
public class OfferRewardBossGroup extends baseConfig<OfferRewardBossGroupObject>{


private static OfferRewardBossGroup instance = null;

public static OfferRewardBossGroup getInstance() {

if (instance == null)
instance = new OfferRewardBossGroup();
return instance;

}


public static Map<Integer, OfferRewardBossGroupObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (OfferRewardBossGroup) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"OfferRewardBossGroup");

for(Map e:ret)
{
put(e);
}

}

public static OfferRewardBossGroupObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, OfferRewardBossGroupObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setAllboss(MapHelper.getInts(e, "allboss"));


_ix_id.put(config.getId(),config);



}
}

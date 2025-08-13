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

@annationInit(value ="OfferRewardLevelLimit", methodname = "initConfig")
public class OfferRewardLevelLimit extends baseConfig<OfferRewardLevelLimitObject>{


private static OfferRewardLevelLimit instance = null;

public static OfferRewardLevelLimit getInstance() {

if (instance == null)
instance = new OfferRewardLevelLimit();
return instance;

}


public static Map<Integer, OfferRewardLevelLimitObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (OfferRewardLevelLimit) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"OfferRewardLevelLimit");

for(Map e:ret)
{
put(e);
}

}

public static OfferRewardLevelLimitObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, OfferRewardLevelLimitObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setBoss(MapHelper.getInt(e, "boss"));

config.setBoss1(MapHelper.getIntArray(e, "boss1"));

config.setBoss2(MapHelper.getIntArray(e, "boss2"));

config.setBoss3(MapHelper.getIntArray(e, "boss3"));

config.setBoss4(MapHelper.getIntArray(e, "boss4"));


_ix_id.put(config.getId(),config);



}
}

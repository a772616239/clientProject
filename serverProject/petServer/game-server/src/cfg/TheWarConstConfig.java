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

@annationInit(value ="TheWarConstConfig", methodname = "initConfig")
public class TheWarConstConfig extends baseConfig<TheWarConstConfigObject>{


private static TheWarConstConfig instance = null;

public static TheWarConstConfig getInstance() {

if (instance == null)
instance = new TheWarConstConfig();
return instance;

}


public static Map<Integer, TheWarConstConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (TheWarConstConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"TheWarConstConfig");

for(Map e:ret)
{
put(e);
}

}

public static TheWarConstConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, TheWarConstConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setPreendtime(MapHelper.getInt(e, "preEndTime"));

config.setRechargepettime(MapHelper.getInt(e, "rechargePetTime"));

config.setDelaycleargridtime(MapHelper.getInt(e, "delayClearGridTime"));

config.setMosterfightstarrecoverenergy(MapHelper.getIntArray(e, "mosterFightStarRecoverEnergy"));

config.setPlayerfightstarrecoverenergy(MapHelper.getIntArray(e, "playerFightStarRecoverEnergy"));

config.setBuybackcost(MapHelper.getIntArray(e, "BuyBackCost"));

config.setBuystamiacost(MapHelper.getIntArray(e, "BuyStamiaCost"));

config.setBustamiavalue(MapHelper.getInt(e, "BuStamiaValue"));

config.setPetrecoverinterval(MapHelper.getInt(e, "PetRecoverInterval"));

config.setPetrecoverrate(MapHelper.getInt(e, "PetRecoverRate"));

config.setCampseasonrankmailid(MapHelper.getInt(e, "CampSeasonRankMailId"));

config.setAttackenemygridmarqueeid(MapHelper.getInt(e, "AttackEnemyGridMarqueeId"));

config.setOccupyenemygridmarqueeid(MapHelper.getInt(e, "OccupyEnemyGridMarqueeId"));

config.setMinpetremainhprate(MapHelper.getInt(e, "MinPetRemainHpRate"));


_ix_id.put(config.getId(),config);



}
}

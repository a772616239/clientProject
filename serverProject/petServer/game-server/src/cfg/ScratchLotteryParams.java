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

@annationInit(value ="ScratchLotteryParams", methodname = "initConfig")
public class ScratchLotteryParams extends baseConfig<ScratchLotteryParamsObject>{


private static ScratchLotteryParams instance = null;

public static ScratchLotteryParams getInstance() {

if (instance == null)
instance = new ScratchLotteryParams();
return instance;

}


public static Map<Integer, ScratchLotteryParamsObject> _ix_id = new HashMap<Integer, ScratchLotteryParamsObject>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (ScratchLotteryParams) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"ScratchLotteryParams");

for(Map e:ret)
{
put(e);
}

}

public static ScratchLotteryParamsObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, ScratchLotteryParamsObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setLength(MapHelper.getInt(e, "length"));

config.setWidth(MapHelper.getInt(e, "width"));

config.setNeedqualitycount(MapHelper.getInt(e, "needQualityCount"));

config.setConsume(MapHelper.getInts(e, "consume"));

config.setProgressreward(MapHelper.getIntArray(e, "progressReward"));

config.setRewardcountodds(MapHelper.getIntArray(e, "rewardCountOdds"));

config.setBasereward(MapHelper.getInt(e, "baseReward"));

config.setQualityodds(MapHelper.getIntArray(e, "qualityOdds"));

config.setOddschangequality(MapHelper.getIntArray(e, "oddsChangeQuality"));


_ix_id.put(config.getId(),config);



}
}

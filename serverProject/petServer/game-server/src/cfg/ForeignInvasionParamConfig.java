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

@annationInit(value ="ForeignInvasionParamConfig", methodname = "initConfig")
public class ForeignInvasionParamConfig extends baseConfig<ForeignInvasionParamConfigObject>{


private static ForeignInvasionParamConfig instance = null;

public static ForeignInvasionParamConfig getInstance() {

if (instance == null)
instance = new ForeignInvasionParamConfig();
return instance;

}


public static Map<Integer, ForeignInvasionParamConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (ForeignInvasionParamConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"ForeignInvasionParamConfig");

for(Map e:ret)
{
put(e);
}

}

public static ForeignInvasionParamConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, ForeignInvasionParamConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setDelaytime(MapHelper.getInts(e, "delayTime"));

config.setRefreashtime(MapHelper.getStr(e, "refreashTime"));

config.setOpenday(MapHelper.getInts(e, "openDay"));

config.setBegintime(MapHelper.getInt(e, "beginTime"));

config.setBeginmaqueeid(MapHelper.getInt(e, "beginMaqueeId"));

config.setFirststagestarttime(MapHelper.getInt(e, "firstStageStartTime"));

config.setFirststagemonstercount(MapHelper.getInt(e, "firstStageMonsterCount"));

config.setFirststagerefeashtime(MapHelper.getInt(e, "firstStageRefeashTime"));

config.setFirststagekilltargetcount(MapHelper.getInt(e, "firstStageKillTargetCount"));

config.setFirststagemaxtime(MapHelper.getInt(e, "firstStageMaxTime"));

config.setRemainmonsterrefreashinterval(MapHelper.getInt(e, "remainMonsterRefreashInterval"));

config.setFirststagetimeoverstrid(MapHelper.getInt(e, "firstStageTimeOverStrId"));

config.setFirststagefinishtargetstrid(MapHelper.getInt(e, "firstStageFinishTargetStrId"));

config.setTransitiontime(MapHelper.getInt(e, "transitionTime"));

config.setTransitionrandompool(MapHelper.getIntArray(e, "transitionRandomPool"));

config.setMaxgainrewardcount(MapHelper.getInt(e, "maxGainRewardCount"));

config.setSecondstagemaxtime(MapHelper.getInt(e, "secondStageMaxTime"));

config.setBossclonecount(MapHelper.getInt(e, "bossCloneCount"));

config.setBossbloodvolume(MapHelper.getInt(e, "bossBloodVolume"));

config.setEachbattlemaxdamage(MapHelper.getInt(e, "eachBattleMaxDamage"));

config.setBossbvrefreashinterval(MapHelper.getInt(e, "bossBVRefreashInterval"));

config.setRankingrefreashinterval(MapHelper.getInt(e, "rankingRefreashInterval"));

config.setSecondrankingcount(MapHelper.getInt(e, "secondRankingCount"));

config.setSettlerankingcount(MapHelper.getInt(e, "settleRankingCount"));

config.setBossunkilledmarqueeid(MapHelper.getInt(e, "bossUnkilledMarqueeId"));

config.setBosskilledmarqueeid(MapHelper.getInt(e, "bossKilledMarqueeId"));

config.setSettledelaytime(MapHelper.getInt(e, "settleDelayTime"));

config.setBloodvolumechangesneedcount(MapHelper.getInt(e, "bloodVolumeChangesNeedCount"));

config.setRiseratio(MapHelper.getInt(e, "riseRatio"));

config.setLowerratio(MapHelper.getInt(e, "lowerRatio"));

config.setBossminbloodvolume(MapHelper.getInt(e, "bossMinBloodVolume"));

config.setBosskilledbarrageid(MapHelper.getInt(e, "bossKilledBarrageId"));

config.setBosskillreward(MapHelper.getIntArray(e, "bossKillReward"));

config.setHelp(MapHelper.getInt(e, "help"));

config.setBossdamagebarragelimit(MapHelper.getInt(e, "bossDamageBarrageLimit"));


_ix_id.put(config.getId(),config);



}
}

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

@annationInit(value ="CrossConstConfig", methodname = "initConfig")
public class CrossConstConfig extends baseConfig<CrossConstConfigObject>{


private static CrossConstConfig instance = null;

public static CrossConstConfig getInstance() {

if (instance == null)
instance = new CrossConstConfig();
return instance;

}


public static Map<Integer, CrossConstConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (CrossConstConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"CrossConstConfig");

for(Map e:ret)
{
put(e);
}

}

public static CrossConstConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, CrossConstConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setMistteambattlebuff(MapHelper.getInt(e, "mistTeamBattleBuff"));

config.setExpectstrength(MapHelper.getInt(e, "expectStrength"));

config.setVictorycorrection(MapHelper.getInt(e, "victoryCorrection"));

config.setFailurecorrection(MapHelper.getInt(e, "failureCorrection"));

config.setAverageexpected(MapHelper.getInt(e, "averageExpected"));

config.setBattlefailedhprate(MapHelper.getInt(e, "battleFailedHpRate"));

config.setSaferegionrecoverhpinterval(MapHelper.getInt(e, "safeRegionRecoverHpInterval"));

config.setSaferegionrecoverhprate(MapHelper.getInt(e, "safeRegionRecoverHpRate"));

config.setOutersaferegionrecoverhpinterval(MapHelper.getInt(e, "outerSafeRegionRecoverHpInterval"));

config.setOutersaferegionrecoverhprate(MapHelper.getInt(e, "outerSafeRegionRecoverHpRate"));

config.setMistbattleteamrewardrate(MapHelper.getInt(e, "mistBattleTeamRewardRate"));

config.setGhostbustermaxmatchtime(MapHelper.getInt(e, "ghostBusterMaxMatchTime"));

config.setGhostbustermaxloadingtime(MapHelper.getInt(e, "ghostBusterMaxLoadingTime"));

config.setGhostbusterfighttime(MapHelper.getInt(e, "ghostBusterFightTime"));

config.setGoblingenerateodds(MapHelper.getInt(e, "goblinGenerateOdds"));

config.setTouchgoblindechp(MapHelper.getInt(e, "touchGoblinDecHp"));

config.setCreateskeletonmonsterinterval(MapHelper.getInt(e, "createSkeletonMonsterInterval"));

config.setMaxskeletonmonstercount(MapHelper.getInt(e, "maxSkeletonMonsterCount"));

config.setCreateskeletonmonstercount(MapHelper.getInt(e, "createSkeletonMonsterCount"));

config.setManeaterrebornhprate(MapHelper.getInt(e, "manEaterRebornHpRate"));

config.setCreatemaneatermonsterinterval(MapHelper.getInt(e, "createManEaterMonsterInterval"));

config.setMaxmaneatermonstercount(MapHelper.getInt(e, "maxManEaterMonsterCount"));

config.setCreatemaneatermonstercount(MapHelper.getInt(e, "createManEaterMonsterCount"));

config.setManeatermonsterrecoverhpinterval(MapHelper.getInt(e, "manEaterMonsterRecoverHpInterval"));

config.setManeatermonsterrecoverhprate(MapHelper.getInt(e, "manEaterMonsterRecoverHpRate"));

config.setLavabadgecombinenum(MapHelper.getInt(e, "lavaBadgeCombineNum"));

config.setLavabadgerandreward(MapHelper.getIntArray(e, "lavaBadgeRandReward"));


_ix_id.put(config.getId(),config);



}
}

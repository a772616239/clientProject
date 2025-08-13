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

@annationInit(value ="MailTemplateUsed", methodname = "initConfig")
public class MailTemplateUsed extends baseConfig<MailTemplateUsedObject>{


private static MailTemplateUsed instance = null;

public static MailTemplateUsed getInstance() {

if (instance == null)
instance = new MailTemplateUsed();
return instance;

}


public static Map<Integer, MailTemplateUsedObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MailTemplateUsed) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MailTemplateUsed");

for(Map e:ret)
{
put(e);
}

}

public static MailTemplateUsedObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MailTemplateUsedObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setBagfull(MapHelper.getInt(e, "bagFull"));

config.setFirstrecharge(MapHelper.getInt(e, "firstRecharge"));

config.setMistachieveintegralreward(MapHelper.getInt(e, "mistAchieveIntegralReward"));

config.setForinvranking(MapHelper.getInt(e, "forInvRanking"));

config.setNewbeewelcome(MapHelper.getInt(e, "newbeewelcome"));

config.setForinvbosskilled(MapHelper.getInt(e, "forInvBossKilled"));

config.setArenadaily(MapHelper.getInt(e, "arenaDaily"));

config.setArenaweekly(MapHelper.getInt(e, "arenaWeekly"));

config.setMonthcommoncard(MapHelper.getInt(e, "monthCommonCard"));

config.setMonthhighcard(MapHelper.getInt(e, "monthHighCard"));

config.setFriendhelpreward(MapHelper.getInt(e, "friendHelpReward"));

config.setBasicwishwell(MapHelper.getInt(e, "basicWishWell"));

config.setAdvacendwishwell(MapHelper.getInt(e, "advacendWishWell"));

config.setRankingactivity(MapHelper.getInt(e, "rankingActivity"));

config.setFailedreporter(MapHelper.getInt(e, "failedReporter"));

config.setSuccessreporter(MapHelper.getInt(e, "successReporter"));

config.setSuccessreported(MapHelper.getInt(e, "successReported"));

config.setPlayerreturn(MapHelper.getInt(e, "playerReturn"));

config.setArenamissionreissue (MapHelper.getInt(e, "arenaMissionReissue "));

config.setBossdamagerank(MapHelper.getInt(e, "bossDamageRank"));

config.setDaydayrechargeactivitysettle(MapHelper.getInt(e, "dayDayRechargeActivitySettle"));

config.setAvatarborderexpire(MapHelper.getInt(e, "avatarBorderExpire"));

config.setDailyfirstrechagerewards(MapHelper.getInt(e, "dailyFirstRechageRewards"));

config.setThewarseason(MapHelper.getInt(e, "theWarSeason"));

config.setThewarseasonunranking(MapHelper.getInt(e, "theWarSeasonUnRanking"));

config.setNewtitleexpire(MapHelper.getInt(e, "newTitleExpire"));

config.setMazemission(MapHelper.getInt(e, "mazeMission"));

config.setGhostmission(MapHelper.getInt(e, "ghostMission"));

config.setMistactivitybossreward(MapHelper.getInt(e, "mistActivityBossReward"));

config.setLtcopysettle(MapHelper.getInt(e, "ltCopySettle"));

config.setCaryzdule(MapHelper.getInt(e, "caryzDule"));

config.setWishwell(MapHelper.getInt(e, "wishWell"));

config.setMisttargetmission(MapHelper.getInt(e, "mistTargetMission"));

config.setNoreward(MapHelper.getInt(e, "noReward"));

config.setLtweeklysettle(MapHelper.getInt(e, "ltWeeklySettle"));

config.setMagicthrondailyrankreward(MapHelper.getInt(e, "magicThronDailyRankReward"));

config.setMagicthronweeklyrankreward(MapHelper.getInt(e, "magicThronWeeklyRankReward"));

config.setMagicthronbossdamagerankreward(MapHelper.getInt(e, "magicThronBossDamageRankReward"));

config.setLtserailwintaskdaily(MapHelper.getInt(e, "ltSerailWinTaskDaily"));

config.setLtserailwintaskweekly(MapHelper.getInt(e, "ltSerailWinTaskWeekly"));


_ix_id.put(config.getId(),config);



}
}

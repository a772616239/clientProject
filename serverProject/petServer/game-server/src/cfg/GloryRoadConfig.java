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

@annationInit(value ="GloryRoadConfig", methodname = "initConfig")
public class GloryRoadConfig extends baseConfig<GloryRoadConfigObject>{


private static GloryRoadConfig instance = null;

public static GloryRoadConfig getInstance() {

if (instance == null)
instance = new GloryRoadConfig();
return instance;

}


public static Map<Integer, GloryRoadConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (GloryRoadConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"GloryRoadConfig");

for(Map e:ret)
{
put(e);
}

}

public static GloryRoadConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, GloryRoadConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setOpenoffsetday(MapHelper.getInt(e, "openOffsetDay"));

config.setOpendayofweek(MapHelper.getInt(e, "openDayOfWeek"));

config.setOpentime(MapHelper.getInt(e, "openTime"));

config.setFirstbattletimeoffset(MapHelper.getInt(e, "firstBattleTimeOffset"));

config.setJoinplayercount(MapHelper.getInt(e, "joinPlayerCount"));

config.setAutobattletime(MapHelper.getInt(e, "autoBattleTime"));

config.setManualbattletime(MapHelper.getInt(e, "manualBattleTime"));

config.setManualbattlewaittime(MapHelper.getInt(e, "manualBattleWaitTime"));

config.setBattleintervalinoneday(MapHelper.getInt(e, "battleIntervalInOneDay"));

config.setBattleopenday(MapHelper.getIntArray(e, "battleOpenDay"));

config.setInactiveplayerofflinedays(MapHelper.getInt(e, "inactivePlayerOffLineDays"));

config.setGroupsize(MapHelper.getInt(e, "groupSize"));

config.setRedbagduration(MapHelper.getInt(e, "redBagDuration"));

config.setRedbagmaxcount(MapHelper.getInt(e, "redbagMaxCount"));

config.setQuizoddsupdateinterval(MapHelper.getInt(e, "quizOddsUpdateInterval"));

config.setQuizcommentcount(MapHelper.getInt(e, "quizCommentCount"));

config.setBetflymsgmaxweight(MapHelper.getInt(e, "betFlyMsgMaxWeight"));

config.setQuizsuccessmail(MapHelper.getInt(e, "quizSuccessMail"));

config.setJoinmailtemplate(MapHelper.getInt(e, "joinMailTemplate"));

config.setPromotionmailtemplate(MapHelper.getInt(e, "promotionMailTemplate"));

config.setPromotionfailedmailtemplate(MapHelper.getInt(e, "promotionFailedMailTemplate"));

config.setRankingsettlemailtemplate(MapHelper.getInt(e, "rankingSettleMailTemplate"));

config.setPvefightmakeid(MapHelper.getInt(e, "pveFightMakeId"));

config.setPvpfightmakeid(MapHelper.getInt(e, "pvpFightMakeId"));

config.setPushadvancetime(MapHelper.getInt(e, "pushAdvanceTime"));

config.setBattledirectwinpowerrate(MapHelper.getInt(e, "battleDirectWinPowerRate"));

config.setCommentsendinterval(MapHelper.getInt(e, "commentSendInterval"));

config.setInvailedbattletips(MapHelper.getInt(e, "invailedBattleTips"));


_ix_id.put(config.getId(),config);



}
}

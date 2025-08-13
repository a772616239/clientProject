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

@annationInit(value ="NewForeignInvasionConfig", methodname = "initConfig")
public class NewForeignInvasionConfig extends baseConfig<NewForeignInvasionConfigObject>{


private static NewForeignInvasionConfig instance = null;

public static NewForeignInvasionConfig getInstance() {

if (instance == null)
instance = new NewForeignInvasionConfig();
return instance;

}


public static Map<Integer, NewForeignInvasionConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (NewForeignInvasionConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"NewForeignInvasionConfig");

for(Map e:ret)
{
put(e);
}

}

public static NewForeignInvasionConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, NewForeignInvasionConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setRefreashtime(MapHelper.getStr(e, "refreashTime"));

config.setOpenday(MapHelper.getInts(e, "openDay"));

config.setBegintime(MapHelper.getInt(e, "beginTime"));

config.setStarttime(MapHelper.getInt(e, "startTime"));

config.setOpentime(MapHelper.getInt(e, "openTime"));

config.setSettledelaytime(MapHelper.getInt(e, "settleDelayTime"));

config.setRankingrefreshinterval(MapHelper.getInt(e, "rankingRefreshInterval"));

config.setRankingshowcount(MapHelper.getInt(e, "rankingShowCount"));

config.setBuildingrefreshinterval(MapHelper.getInt(e, "buildingRefreshInterval"));

config.setHelp(MapHelper.getInt(e, "help"));

config.setFreeallrewrds(MapHelper.getIntArray(e, "freeAllRewrds"));

config.setFreeallmailtemplate(MapHelper.getInt(e, "freeAllMailTemplate"));

config.setRankingmailtemplate(MapHelper.getInt(e, "rankingMailTemplate"));

config.setRestoreconsume(MapHelper.getIntArray(e, "restoreConsume"));

config.setRestorerate(MapHelper.getInts(e, "restoreRate"));

config.setBuildingfreemarquee(MapHelper.getInt(e, "buildingFreeMarquee"));

config.setFreeallmarquee(MapHelper.getInt(e, "freeAllMarquee"));

config.setNotfreeallmarquee(MapHelper.getInt(e, "notFreeAllMarquee"));

config.setFirstrankingmarquee(MapHelper.getInt(e, "firstRankingMarquee"));

config.setFirstrankinginterval(MapHelper.getInt(e, "firstRankingInterval"));

config.setPlayerrankingmarquee(MapHelper.getInt(e, "playerRankingMarquee"));

config.setCountdownmarquee(MapHelper.getInt(e, "countDownMarquee"));

config.setCountdowninterval(MapHelper.getInt(e, "countDownInterval"));


_ix_id.put(config.getId(),config);



}
}

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

@annationInit(value ="CrossArenaLvCfg", methodname = "initConfig")
public class CrossArenaLvCfg extends baseConfig<CrossArenaLvCfgObject>{


private static CrossArenaLvCfg instance = null;

public static CrossArenaLvCfg getInstance() {

if (instance == null)
instance = new CrossArenaLvCfg();
return instance;

}


public static Map<Integer, CrossArenaLvCfgObject> _ix_lv = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (CrossArenaLvCfg) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"CrossArenaLvCfg");

for(Map e:ret)
{
put(e);
}

}

public static CrossArenaLvCfgObject getByLv(int lv){

return _ix_lv.get(lv);

}



public  void putToMem(Map e, CrossArenaLvCfgObject config){

config.setLv(MapHelper.getInt(e, "lv"));

config.setGoodsdiscount(MapHelper.getIntArray(e, "goodsDiscount"));

config.setNextlvlexp(MapHelper.getInt(e, "nextLvlExp"));

config.setAddbuff(MapHelper.getInts(e, "addBuff"));

config.setCprevivetimes(MapHelper.getInt(e, "cpReviveTimes"));

config.setCpweeklybuycount(MapHelper.getInt(e, "cpWeeklyBuyCount"));

config.setFix_task(MapHelper.getInts(e, "fix_task"));

config.setScore_limit(MapHelper.getInt(e, "score_limit"));

config.setFix_week_task(MapHelper.getInts(e, "fix_week_task"));

config.setRandom_week_task(MapHelper.getInts(e, "random_week_task"));

config.setRandom_week_tasknum(MapHelper.getInt(e, "random_week_tasknum"));

config.setWeek_reward(MapHelper.getInt(e, "week_reward"));

config.setMist_callbossreduction(MapHelper.getInt(e, "mist_CallBossReduction"));

config.setMistsummonbossodds(MapHelper.getInt(e, "mistSummonBossOdds"));

config.setMistsummonbossextidlist(MapHelper.getInts(e, "mistSummonBossExtIdList"));

config.setMist_pointgoldstonereduction(MapHelper.getInt(e, "mist_PointGoldStoneReduction"));

config.setMist_pointgoldstonetip(MapHelper.getInt(e, "mist_PointGoldStoneTip"));

config.setMist_buystaminaconsume(MapHelper.getInt(e, "mist_BuyStaminaConsume"));

config.setCrazyduelcanrefresh(MapHelper.getInt(e, "crazyDuelcanRefresh"));

config.setMisthprecoverrate(MapHelper.getInt(e, "mistHpRecoverRate"));

config.setMistspeeduprate(MapHelper.getInt(e, "mistSpeedUpRate"));

config.setMistextraboxrate(MapHelper.getIntArray(e, "mistExtraBoxRate"));

config.setMistvipskilllist(MapHelper.getInts(e, "mistVipSkillList"));


_ix_lv.put(config.getLv(),config);



}
}

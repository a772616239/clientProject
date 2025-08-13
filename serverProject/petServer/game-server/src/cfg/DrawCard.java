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

@annationInit(value ="DrawCard", methodname = "initConfig")
public class DrawCard extends baseConfig<DrawCardObject>{


private static DrawCard instance = null;

public static DrawCard getInstance() {

if (instance == null)
instance = new DrawCard();
return instance;

}


public static Map<Integer, DrawCardObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (DrawCard) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"DrawCard");

for(Map e:ret)
{
put(e);
}

}

public static DrawCardObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, DrawCardObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setHighestquality(MapHelper.getInt(e, "highestQuality"));

config.setSelectedeachpettypelimit(MapHelper.getInt(e, "selectedEachPetTypeLimit"));

config.setFrienddrawcardconsume(MapHelper.getInts(e, "friendDrawCardConsume"));

config.setEachfriendexp(MapHelper.getInt(e, "eachFriendExp"));

config.setFriendshipcardweight(MapHelper.getIntArray(e, "friendShipCardWeight"));

config.setFriendoddschangequality(MapHelper.getIntArray(e, "friendOddsChangeQuality"));

config.setFriendmustquality(MapHelper.getInts(e, "friendMustQuality"));

config.setFriendpettypeodds(MapHelper.getIntArray(e, "friendPetTypeOdds"));

config.setCommonpettypeodds(MapHelper.getIntArray(e, "commonPetTypeOdds"));

config.setHighpettypeodds(MapHelper.getIntArray(e, "highPetTypeOdds"));

config.setCommonfirsttendrawsmustquality(MapHelper.getInt(e, "commonFirstTenDrawsMustQuality"));

config.setCommonmustdrawcount(MapHelper.getInt(e, "commonMustDrawCount"));

config.setCommondrawcardmust(MapHelper.getInts(e, "commonDrawCardMust"));

config.setCommoncardqualityweight(MapHelper.getIntArray(e, "commonCardQualityWeight"));

config.setCommonoddschangequality(MapHelper.getIntArray(e, "commonOddsChangeQuality"));

config.setCommomdrawcardconsumeitem(MapHelper.getInt(e, "commomDrawCardConsumeItem"));

config.setCommondrawcarddiamond(MapHelper.getInt(e, "commonDrawCardDiamond"));

config.setEachcommonexp(MapHelper.getInt(e, "eachCommonExp"));

config.setDrawcommondiamonddiscount(MapHelper.getInt(e, "drawCommonDiamondDisCount"));

config.setCommoncardcorefloortimes(MapHelper.getInt(e, "commonCardCoreFloorTimes"));

config.setOpenhighcardneedexp(MapHelper.getInts(e, "openHighCardNeedExp"));

config.setSinglehightcardrateincrease(MapHelper.getInt(e, "singleHightCardRateIncrease"));

config.setLowbookcfgid(MapHelper.getInt(e, "lowBookCfgId"));

config.setHighbookcfgid(MapHelper.getInt(e, "highBookCfgId"));

config.setUnfinishedresetfirst(MapHelper.getInts(e, "unfinishedResetFirst"));

config.setUnfinishedresetseconed(MapHelper.getInts(e, "unfinishedResetSeconed"));

config.setHighcardpoolcount(MapHelper.getInt(e, "highCardPoolCount"));

config.setDrawhighcardconsume(MapHelper.getInts(e, "drawHighCardConsume"));

config.setHighcardqualityweight(MapHelper.getIntArray(e, "highCardQualityWeight"));

config.setHighoddschangequality(MapHelper.getIntArray(e, "highOddsChangeQuality"));

config.setHighcardmustquality(MapHelper.getInts(e, "highCardMustQuality"));

config.setHighspecialdealtimes(MapHelper.getIntArray(e, "highSpecialDealTimes"));

config.setHighcardcorefloortimes(MapHelper.getInt(e, "highCardCoreFloorTimes"));

config.setCommondrawspecialodds(MapHelper.getInt(e, "commonDrawSpecialOdds"));


_ix_id.put(config.getId(),config);



}
}

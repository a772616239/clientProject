/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.base.baseConfig;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import org.apache.commons.lang.StringUtils;
import util.LogUtil;
import util.TimeUtil;

@annationInit(value = "VIPConfig", methodname = "initConfig")
public class VIPConfig extends baseConfig<VIPConfigObject> {


    private static VIPConfig instance = null;

    public static VIPConfig getInstance() {

        if (instance == null)
            instance = new VIPConfig();
        return instance;

    }


    public static Map<Integer, VIPConfigObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (VIPConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "VIPConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static VIPConfigObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, VIPConfigObject config) {

        config.setId(MapHelper.getInt(e, "ID"));

        config.setNeedtotalexp(MapHelper.getInt(e, "needTotalExp"));

        config.setPetbagimprove(MapHelper.getInt(e, "petBagImprove"));

        config.setRunebagimprove(MapHelper.getInt(e, "runeBagImprove"));

        config.setLvgiftbag(MapHelper.getIntArray(e, "LvGiftBag"));

        config.setGlodexaddtion(MapHelper.getInt(e, "glodExAddtion"));

        config.setGoldexupperlimit(MapHelper.getInt(e, "goldExUpperLimit"));

        config.setOnhookexaddtion(MapHelper.getIntArray(e, "onHookExAddtion"));

        config.setPetmissiondailytimes(MapHelper.getInt(e, "petMissionDailyTimes"));

        config.setPatrolreborntimes(MapHelper.getInt(e, "patrolRebornTimes"));

        config.setBuyrescopylimit(MapHelper.getInt(e, "buyResCopyLimit"));

        config.setQuickonhooktimes(MapHelper.getInt(e, "quickOnhookTimes"));

        config.setGainmistbagextrarate(MapHelper.getInt(e, "gainMistBagExtraRate"));

        config.setBoutiquemanualrefreshlimit(MapHelper.getInt(e, "boutiqueManualRefreshLimit"));

        config.setBravechallengereborntimes(MapHelper.getInt(e, "braveChallengeRebornTimes"));

        config.setArenaticketbuylimit(MapHelper.getInt(e, "arenaTicketBuyLimit"));

        config.setGhostbusterfreecount(MapHelper.getInt(e, "ghostBusterFreeCount"));

        config.setOnhooktime(MapHelper.getInt(e, "onHookTime"));

        config.setRescopyexaddtion(MapHelper.getInt(e, "resCopyExAddtion"));

        config.setPlusboostowertimes(MapHelper.getInt(e, "plusBoosTowerTimes"));

        config.setMinedailyrewardexrate(MapHelper.getInt(e, "mineDailyRewardExRate"));

        config.setMineexaddition(MapHelper.getInt(e, "mineExAddition"));

        config.setBosstowerchallengetimes(MapHelper.getInt(e, "bossTowerChallengeTimes"));

        config.setCommondrawspecialtimes(MapHelper.getInt(e, "commonDrawSpecialTimes"));

        config.setCommonspecialdrawpool(MapHelper.getInts(e, "commonSpecialDrawPool"));

        config.setFriendlimit(MapHelper.getInt(e, "friendLimit"));

        config.setFrienditemgainlimit(MapHelper.getInt(e, "friendItemGainLimit"));

        config.setBuylvlgiftprice(MapHelper.getIntArray(e, "buyLvlGiftPrice"));

        config.setCpdailyrevive(MapHelper.getInt(e, "cpDailyRevive"));

        config.setCpweeklybuycount(MapHelper.getInt(e, "cpWeeklyBuyCount"));

        config.setStonrriftrefreshlimitbuy(MapHelper.getInt(e, "stonrRiftRefreshLimitBuy"));

        config.setStonrriftsteallimitbuy(MapHelper.getInt(e, "stonrRiftStealLimitBuy"));


        _ix_id.put(config.getId(), config);

        if (config.getId() > maxVipLv) {
            maxVipLv = config.getId();
        }
    }

    /**
     * ======================================================
     */
    public static int maxVipLv;

    public static int getMaxExp() {
        VIPConfigObject maxVipCfg = _ix_id.get(maxVipLv);
        return maxVipCfg == null ? 0 : maxVipCfg.getNeedtotalexp();
    }

    public static int getVipExpByVipLv(int vipLv) {
        VIPConfigObject vipConfigObject = _ix_id.get(vipLv);
        return vipConfigObject == null ? 0 : vipConfigObject.getNeedtotalexp();
    }

    public static int getPetMissionDailyCount(int vipLv) {
        VIPConfigObject vipCfg = getById(vipLv);
        return vipCfg == null ? 0 : vipCfg.getPetmissiondailytimes();
    }

    public static int getBoutiqueManualRefreshLimit(int vipLv) {
        VIPConfigObject configObject = getById(vipLv);
        if (configObject == null) {
            LogUtil.error("VIPConfig.getBoutiqueManualRefreshLimit, get boutiqueManualRefreshLimit failed, vipLv = " + vipLv);
            return 0;
        }
        return configObject.getBoutiquemanualrefreshlimit();
    }

    public static long getMainLineMaxOnHookTime(String playerId) {
        if (StringUtils.isEmpty(playerId)) {
            return 0;
        }
        playerEntity player = playerCache.getByIdx(playerId);
        if (player == null) {
            LogUtil.error("getMainLineMaxOnHookTime,player is null by playerId[{}]", playerId);
            return 0;
        }
        VIPConfigObject vipConfig = VIPConfig.getById(player.getVip());
        if (vipConfig == null) {
            LogUtil.error("getMainLineMaxOnHookTime,vipConfig is null by vipLv[{}]", player.getVip());
            return 0;
        }
        return vipConfig.getOnhooktime() * TimeUtil.MS_IN_A_HOUR;
    }

    public static int getMineExRewardRate(String ownerPlayerId) {
        playerEntity player = playerCache.getByIdx(ownerPlayerId);
        if (player == null) {
            return 0;
        }
        VIPConfigObject config = VIPConfig.getById(player.getVip());
        if (config == null) {
            LogUtil.error("getMineExRewardRate error,vipConfig is null by vipLv[{}]", player.getVip());
            return 0;
        }
        return config.getMineexaddition();
    }

    public static int getMineDailyRewardExRate(String ownerPlayerId) {
        playerEntity player = playerCache.getByIdx(ownerPlayerId);
        if (player == null) {
            return 0;
        }
        VIPConfigObject config = VIPConfig.getById(player.getVip());
        if (config == null) {
            LogUtil.error("getMineDailyRewardExRate error,vipConfig is null by vipLv[{}]", player.getVip());
            return 0;
        }
        return config.getMinedailyrewardexrate();
    }
}

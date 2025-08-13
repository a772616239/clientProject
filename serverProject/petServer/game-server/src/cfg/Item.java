/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import model.base.baseConfig;
import model.itembag.ItemConst;
import model.reward.RewardUtil;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.util.CollectionUtils;
import protocol.Bag.BlindBoxReward;
import protocol.Common;
import util.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@annationInit(value = "Item", methodname = "initConfig")
public class Item extends baseConfig<ItemObject> {


    private static Item instance = null;

    public static Item getInstance() {

        if (instance == null)
            instance = new Item();
        return instance;

    }


    public static Map<Integer, ItemObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (Item) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "Item");

        for (Map e : ret) {
            put(e);
        }

    }

    public static ItemObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, ItemObject config) {

        config.setId(MapHelper.getInt(e, "ID"));

        config.setName_tips(MapHelper.getStr(e, "Name_tips"));

        config.setQuality(MapHelper.getInt(e, "Quality"));

        config.setUsable(MapHelper.getBoolean(e, "usable"));

        config.setMustreward(MapHelper.getIntArray(e, "mustReward"));

        config.setParamname(MapHelper.getStr(e, "paramName"));

        config.setParamstr(MapHelper.getIntArray(e, "paramStr"));

        config.setRandomtimes(MapHelper.getInt(e, "randomTimes"));

        config.setRandomrewards(MapHelper.getIntArray(e, "randomRewards"));

        config.setRandomtimes2(MapHelper.getInt(e, "randomTimes2"));

        config.setSalable(MapHelper.getBoolean(e, "salable"));

        config.setGainaftersell(MapHelper.getIntArray(e, "GainAfterSell"));

        config.setSpecialtype(MapHelper.getInt(e, "specialType"));

        config.setMaxownedcount(MapHelper.getInt(e, "maxOwnedCount"));

        config.setAutouse(MapHelper.getBoolean(e, "autoUse"));

        config.setUseneedlv(MapHelper.getInt(e, "useNeedLv"));

        config.setUsecostitem(MapHelper.getIntArray(e, "UseCostItem"));

        config.setUsetimeslimit(MapHelper.getInt(e, "useTimeslimit"));

        config.setDailyusetimeslimit(MapHelper.getInt(e, "dailyUseTimeslimit"));


        _ix_id.put(config.getId(), config);

        classifyBySpecialType(config);

    }

    /**
     * =======================================================
     */

    /**
     * 根据特殊类型分类
     */
    private static final Map<Integer, List<ItemObject>> specialMap = new HashMap<>();


    private static void classifyBySpecialType(ItemObject item) {
        if (item == null) {
            return;
        }

        List<ItemObject> itemObjects = specialMap.computeIfAbsent(item.getSpecialtype(), k -> new ArrayList<>());
        itemObjects.add(item);
    }

    public static List<ItemObject> getAllItemBySpecialType(int type) {
        return specialMap.get(type);
    }

    public static String getItemName(int cfgId) {
        ItemObject byId = getById(cfgId);
        if (byId == null) {
            return String.valueOf(cfgId);
        }
        return byId.getName_tips();
    }

    /**
     * 返回道具的拥有上限
     *
     * @param id
     * @return
     */
    public static long getItemOwnLimit(int id) {
        ItemObject itemCfg = getById(id);
        return itemCfg == null ? 0 : itemCfg.getMaxownedcount();
    }

    public static int getQuality(int cfgId) {
        ItemObject itemObject = Item.getById(cfgId);
        if (itemObject == null) {
            return 0;
        }
        return itemObject.getQuality();
    }
}

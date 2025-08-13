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
import model.consume.ConsumeUtil;
import protocol.Common;
import protocol.Shop;

@annationInit(value = "ShopRefreshSpend", methodname = "initConfig")
public class ShopRefreshSpend extends baseConfig<ShopRefreshSpendObject> {


    private static ShopRefreshSpend instance = null;

    public static ShopRefreshSpend getInstance() {

        if (instance == null)
            instance = new ShopRefreshSpend();
        return instance;

    }


    public static Map<Integer, ShopRefreshSpendObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (ShopRefreshSpend) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "ShopRefreshSpend");

        for (Map e : ret) {
            put(e);
        }

    }

    public static ShopRefreshSpendObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, ShopRefreshSpendObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setShoptype(MapHelper.getInt(e, "shopType"));

        config.setRefreshtimes(MapHelper.getInt(e, "refreshTimes"));

        config.setSpend(MapHelper.getInts(e, "spend"));


        _ix_id.put(config.getId(), config);

        classifyConfig(config);
    }

    /**
     * ======================================================================
     */

    //<ShopTypeEnum, <refreshTimes, ShopRefreshSpendObject>>
    private static Map<Integer, Map<Integer, ShopRefreshSpendObject>> refreshSpend = new HashMap<>();
    //<ShopTypeEnum,maxRefreshTimes>
    private static Map<Integer, Integer> maxRefreshCfg = new HashMap<>();

    private void classifyConfig(ShopRefreshSpendObject config) {
        if (config == null) {
            return;
        }

        Map<Integer, ShopRefreshSpendObject> subMap = refreshSpend.get(config.getShoptype());
        if (subMap == null) {
            subMap = new HashMap<>();
            refreshSpend.put(config.getShoptype(), subMap);
        }

        subMap.put(config.getRefreshtimes(), config);

        if (maxRefreshCfg.containsKey(config.getShoptype())) {
            if (config.getRefreshtimes() > maxRefreshCfg.get(config.getShoptype())) {
                maxRefreshCfg.put(config.getShoptype(), config.getRefreshtimes());
            }
        } else {
            maxRefreshCfg.put(config.getShoptype(), config.getRefreshtimes());
        }

    }

    public Common.Consume getRefreshSpend(Shop.ShopTypeEnum shopType, int refreshTimes) {
        if (shopType == null) {
            return null;
        }

        Map<Integer, ShopRefreshSpendObject> subMap = refreshSpend.get(shopType.getNumber());
        if (subMap == null) {
            return null;
        }

        ShopRefreshSpendObject config = subMap.get(refreshTimes);
        if (config == null) {
            config = subMap.get(maxRefreshCfg.get(shopType.getNumber()));
        }

        return ConsumeUtil.parseConsume(config.getSpend());
    }
}

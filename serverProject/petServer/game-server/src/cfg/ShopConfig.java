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
import protocol.Shop.ShopTypeEnum;

@annationInit(value = "ShopConfig", methodname = "initConfig")
public class ShopConfig extends baseConfig<ShopConfigObject> {


    private static ShopConfig instance = null;

    public static ShopConfig getInstance() {

        if (instance == null)
            instance = new ShopConfig();
        return instance;

    }


    public static Map<Integer, ShopConfigObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (ShopConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "ShopConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static ShopConfigObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, ShopConfigObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setAutorefresh(MapHelper.getInt(e, "autoRefresh"));

        config.setRefreshtime(MapHelper.getInt(e, "refreshTime"));

        config.setManualrefresh(MapHelper.getInt(e, "manualRefresh"));

        config.setRefhcountevyday(MapHelper.getInt(e, "refhCountEvyday"));

        config.setRandommincount(MapHelper.getInt(e, "randomMinCount"));

        config.setRandommaxcount(MapHelper.getInt(e, "randomMaxCount"));

        config.setRandomgroupcfg(MapHelper.getIntArray(e, "randomGroupCfg"));

        config.setNotjoinmanual(MapHelper.getInts(e, "notJoinManual"));

        config.setInitcycle(MapHelper.getInt(e, "initCycle"));

        config.setGeneralcycle(MapHelper.getInt(e, "generalCycle "));

        config.setGoodscycle(MapHelper.getIntArray(e, "goodsCycle"));


        _ix_id.put(config.getId(), config);


    }

    /**
     * ========================================================
     */

    public static int getAutoShopRefreshType(ShopTypeEnum shopType) {
        if (shopType == null) {
            return 0;
        }

        ShopConfigObject byId = getById(shopType.getNumber());
        if (byId == null) {
            return 0;
        }
        return byId.getAutorefresh();
    }

    public static int getShopManualRefreshLimit(ShopTypeEnum shopType) {
        if (shopType == null) {
            return -1;
        }

        ShopConfigObject byId = getById(shopType.getNumber());
        if (byId != null) {
            return byId.getRefhcountevyday();
        }

        return -1;
    }
}

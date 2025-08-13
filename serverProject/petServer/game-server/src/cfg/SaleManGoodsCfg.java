/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import model.base.baseConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@annationInit(value = "SaleManGoodsCfg", methodname = "initConfig")
public class SaleManGoodsCfg extends baseConfig<SaleManGoodsCfgObject> {


    private static SaleManGoodsCfg instance = null;

    public static SaleManGoodsCfg getInstance() {

        if (instance == null)
            instance = new SaleManGoodsCfg();
        return instance;

    }


    public static Map<Integer, SaleManGoodsCfgObject> _ix_id = new HashMap<Integer, SaleManGoodsCfgObject>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (SaleManGoodsCfg) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "SaleManGoodsCfg");

        for (Map e : ret) {
            put(e);
        }

    }

    public static SaleManGoodsCfgObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, SaleManGoodsCfgObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setShopshellid(MapHelper.getInt(e, "shopShellId"));

        config.setDiscount(MapHelper.getIntArray(e, "discount"));


        _ix_id.put(config.getId(), config);


    }

    public static int getAppearRateById(int configId) {
        SaleManGoodsCfgObject config = SaleManGoodsCfg.getById(configId);
        if (config == null) {
            return 0;
        }
        ShopSellObject shopSell = ShopSell.getById(config.getShopshellid());
        return shopSell == null ? 0 : shopSell.getAppearrate();
    }
}

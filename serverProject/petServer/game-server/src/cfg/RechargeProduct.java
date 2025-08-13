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
import org.apache.commons.lang.StringUtils;

@annationInit(value = "RechargeProduct", methodname = "initConfig")
public class RechargeProduct extends baseConfig<RechargeProductObject> {


    private static RechargeProduct instance = null;

    public static RechargeProduct getInstance() {

        if (instance == null)
            instance = new RechargeProduct();
        return instance;

    }


    public static Map<Integer, RechargeProductObject> _ix_id = new HashMap<>();

    public static RechargeProductObject finalRechargeProduct(String productCode) {
        if (StringUtils.isEmpty(productCode)){
            return null;
        }
     return  _ix_id.values().stream()
             .filter(item->item.getHyzproductid().equals(productCode)||item.getIosproductid().equals(productCode)||item.getGoogleproductid().equals(productCode))
             .findAny().orElse(null);
    }


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (RechargeProduct) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "RechargeProduct");

        for (Map e : ret) {
            put(e);
        }

    }

    public static RechargeProductObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, RechargeProductObject config) {

        config.setId(MapHelper.getInt(e, "ID"));

        config.setIosproductid(MapHelper.getStr(e, "iOSProductId"));

        config.setGoogleproductid(MapHelper.getStr(e, "GoogleProductId"));

        config.setHyzproductid(MapHelper.getStr(e, "hyzProductId"));

        config.setRechargescore(MapHelper.getInt(e, "rechargeScore"));

        config.setProducttype(MapHelper.getInt(e, "productType"));

        config.setSubtype(MapHelper.getInt(e, "subtype"));

        config.setReward(MapHelper.getIntArray(e, "reward"));


        _ix_id.put(config.getId(), config);


    }

    public static RechargeProductObject finalRechargeProductByGoogle(String googleId) {
        return _ix_id.values().stream().filter(item -> item.getGoogleproductid().equals(googleId)).findAny().orElse(null);

    }

    public static RechargeProductObject finalRechargeProductByIos(String IosId) {
        return _ix_id.values().stream().filter(item -> item.getIosproductid().equals(IosId)).findAny().orElse(null);

    }
}
